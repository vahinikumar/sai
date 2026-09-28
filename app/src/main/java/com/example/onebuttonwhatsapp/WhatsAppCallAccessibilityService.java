package com.example.onebuttonwhatsapp.direct;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WhatsAppCallAccessibilityService extends AccessibilityService {

    private final Handler handler = new Handler();

    private boolean searching = false;
    private boolean callClicked = false;
    private int attempts = 0;

    private boolean monitoringCall = false;
    private boolean callActive = false;
    private int monitorAttempts = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) return;

        CharSequence packageName = event.getPackageName();

        if (packageName == null) return;

        if (!packageName.toString().equals("com.whatsapp")) return;

        boolean callRequested =
                getSharedPreferences(
                        "call_control",
                        MODE_PRIVATE)
                        .getBoolean(
                                "call_requested",
                                false);

        if (!callRequested) return;

        if (searching) return;

        callClicked = false;
        attempts = 0;
        searching = true;
        handler.removeCallbacksAndMessages(null);

        findVoiceCallButton();
    }

    private void findVoiceCallButton() {

        if (callClicked || attempts >= 10) {

            searching = false;

            getSharedPreferences(
                    "call_control",
                    MODE_PRIVATE)
                    .edit()
                    .putBoolean(
                            "call_requested",
                            false)
                    .apply();

            return;
        }

        attempts++;

        handler.postDelayed(() -> {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root != null) {

                boolean clicked = clickVoiceCallButton(root);
                root.recycle();

                if (clicked) {

                    callClicked = true;
                    searching = false;

                    getSharedPreferences(
                            "call_control",
                            MODE_PRIVATE)
                            .edit()
                            .putBoolean(
                                    "call_requested",
                                    false)
                            .apply();

                    startCallMonitoring();
                    return;
                }
            }

            findVoiceCallButton();

        }, 400);
    }

    private boolean clickVoiceCallButton(
            AccessibilityNodeInfo node) {

        if (node == null) return false;

        CharSequence description =
                node.getContentDescription();

        if (description != null &&
                description.toString()
                        .trim()
                        .equalsIgnoreCase("Voice call")) {

            if (node.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK)) {

                return true;
            }

            AccessibilityNodeInfo parent =
                    node.getParent();

            while (parent != null) {

                if (parent.isClickable()) {

                    boolean clicked =
                            parent.performAction(
                                    AccessibilityNodeInfo.ACTION_CLICK);

                    parent.recycle();

                    if (clicked) return true;

                    break;
                }

                AccessibilityNodeInfo next =
                        parent.getParent();

                parent.recycle();

                parent = next;
            }
        }

        for (int i = 0;
                i < node.getChildCount();
                i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                if (clickVoiceCallButton(child)) {

                    child.recycle();
                    return true;
                }

                child.recycle();
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        searching = false;
        callClicked = false;
        attempts = 0;
        monitoringCall = false;
        callActive = false;
        monitorAttempts = 0;
    }

    private void startCallMonitoring() {
        monitoringCall = true;
        callActive = false;
        monitorAttempts = 0;
        handler.postDelayed(this::monitorCall, 500);
    }

    private void monitorCall() {
        if (!monitoringCall) return;

        if (!callActive) {
            if (isCallActive()) {
                callActive = true;
                monitorAttempts = 0;
                handler.postDelayed(this::monitorCall, 500);
            } else {
                monitorAttempts++;
                if (monitorAttempts >= 40) {
                    monitoringCall = false;
                    callActive = false;
                    return;
                }
                handler.postDelayed(this::monitorCall, 500);
            }
        } else {
            if (isCallActive()) {
                handler.postDelayed(this::monitorCall, 500);
            } else {
                if (!isScreenInteractive()) {
                    handler.postDelayed(this::monitorCall, 500);
                    return;
                }

                handler.postDelayed(() -> {
                    if (!monitoringCall || !callActive) return;

                    if (!isCallActive() && isScreenInteractive()) {
                        monitoringCall = false;
                        callActive = false;
                        performGlobalAction(GLOBAL_ACTION_HOME);
                    } else {
                        handler.postDelayed(this::monitorCall, 500);
                    }
                }, 500);
            }
        }
    }

    private boolean isCallActive() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        try {
            return isCallScreenPresent(root);
        } finally {
            root.recycle();
        }
    }

    private boolean isCallScreenPresent(AccessibilityNodeInfo node) {
        if (node == null) return false;

        CharSequence description = node.getContentDescription();
        if (description != null && description.toString().toLowerCase().contains("end call")) {
            return true;
        }

        CharSequence text = node.getText();
        if (text != null && text.toString().toLowerCase().contains("end call")) {
            return true;
        }

        String viewId = node.getViewIdResourceName();
        if (viewId != null) {
            String id = viewId.toLowerCase();
            if (id.contains("end_call") || id.contains("call_end")) {
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (isCallScreenPresent(child)) {
                    child.recycle();
                    return true;
                }
                child.recycle();
            }
        }

        return false;
    }

    private boolean isScreenInteractive() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm == null || pm.isInteractive();
    }
}
