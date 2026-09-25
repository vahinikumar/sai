package com.example.onebuttonwhatsapp;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WhatsAppCallAccessibilityService extends AccessibilityService {

    private final Handler handler = new Handler();

    private boolean searching = false;
    private boolean callClicked = false;
    private int attempts = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        CharSequence packageName = event.getPackageName();

        if (packageName == null) {
            return;
        }

        // Only react to WhatsApp.
        if (!packageName.toString().equals("com.whatsapp")) {
            return;
        }

        // Check whether OUR app requested a call.
        boolean callRequested =
                getSharedPreferences(
                        "call_control",
                        MODE_PRIVATE)
                        .getBoolean("call_requested", false);

        // If user opened WhatsApp normally, do nothing.
        if (!callRequested) {
            return;
        }

        if (searching || callClicked) {
            return;
        }

        searching = true;
        attempts = 0;

        findVoiceCallButton();
    }

    private void findVoiceCallButton() {

        if (callClicked || attempts >= 10) {
            searching = false;

            // Cancel the request if we could not find the button.
            getSharedPreferences(
                    "call_control",
                    MODE_PRIVATE)
                    .edit()
                    .putBoolean("call_requested", false)
                    .apply();

            return;
        }

        attempts++;

        handler.postDelayed(() -> {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root != null) {

                if (clickVoiceCallButton(root)) {

                    callClicked = true;
                    searching = false;

                    // Important:
                    // Don't allow another automatic call.
                    getSharedPreferences(
                            "call_control",
                            MODE_PRIVATE)
                            .edit()
                            .putBoolean("call_requested", false)
                            .apply();

                    return;
                }
            }

            findVoiceCallButton();

        }, 400);
    }

    private boolean clickVoiceCallButton(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return false;
        }

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

                    if (clicked) {
                        return true;
                    }

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
    }
}
