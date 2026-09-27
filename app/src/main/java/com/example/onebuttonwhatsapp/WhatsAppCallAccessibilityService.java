package com.example.onebuttonwhatsapp.direct;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WhatsAppCallAccessibilityService
        extends AccessibilityService {

    private final Handler handler =
            new Handler();

    private boolean searching = false;
    private boolean callClicked = false;

    private int attempts = 0;

    private static final int MAX_ATTEMPTS = 40;
    private static final long RETRY_DELAY = 500;

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event) {

        if (event == null) return;

        CharSequence packageName =
                event.getPackageName();

        if (packageName == null) return;

        if (!packageName.toString()
                .equals("com.whatsapp")) {
            return;
        }

        boolean callRequested =
                getSharedPreferences(
                        "call_control",
                        MODE_PRIVATE)
                        .getBoolean(
                                "call_requested",
                                false);

        if (!callRequested) return;

        if (callClicked) return;

        if (!searching) {

            searching = true;
            callClicked = false;
            attempts = 0;

            handler.removeCallbacksAndMessages(null);

            findVoiceCallButton();
        }
    }

    private void findVoiceCallButton() {

        if (callClicked) {
            return;
        }

        if (attempts >= MAX_ATTEMPTS) {

            finishRequest();

            return;
        }

        attempts++;

        handler.postDelayed(() -> {

            if (callClicked) {
                return;
            }

            AccessibilityNodeInfo target =
                    findVoiceCallNode();

            if (target != null) {

                Rect bounds =
                        new Rect();

                target.getBoundsInScreen(bounds);

                boolean clicked =
                        target.performAction(
                                AccessibilityNodeInfo
                                        .ACTION_CLICK);

                target.recycle();

                if (clicked) {

                    callSucceeded();

                    return;
                }

                /*
                 * ACTION_CLICK failed.
                 * Use a real screen tap as fallback.
                 */

                if (!bounds.isEmpty()) {

                    float x = bounds.centerX();
                    float y = bounds.centerY();

                    tapScreen(x, y);

                    return;
                }
            }

            findVoiceCallButton();

        }, RETRY_DELAY);
    }

    private AccessibilityNodeInfo findVoiceCallNode() {

        for (android.view.accessibility
                .AccessibilityWindowInfo window
                : getWindows()) {

            AccessibilityNodeInfo root =
                    window.getRoot();

            if (root == null) {
                continue;
            }

            AccessibilityNodeInfo result =
                    searchNode(root);

            root.recycle();

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private AccessibilityNodeInfo searchNode(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        CharSequence description =
                node.getContentDescription();

        if (description != null) {

            String value =
                    description.toString()
                            .trim();

            if (value.equalsIgnoreCase(
                    "Voice call")) {

                return AccessibilityNodeInfo
                        .obtain(node);
            }
        }

        for (int i = 0;
                i < node.getChildCount();
                i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                AccessibilityNodeInfo result =
                        searchNode(child);

                child.recycle();

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private void tapScreen(
            float x,
            float y) {

        Path path =
                new Path();

        path.moveTo(x, y);

        GestureDescription.StrokeDescription
                stroke =
                new GestureDescription
                        .StrokeDescription(
                                path,
                                0,
                                100);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(stroke)
                        .build();

        dispatchGesture(
                gesture,
                new GestureResultCallback() {

                    @Override
                    public void onCompleted(
                            GestureDescription gestureDescription) {

                        callSucceeded();
                    }

                    @Override
                    public void onCancelled(
                            GestureDescription gestureDescription) {

                        findVoiceCallButton();
                    }
                },
                handler);
    }

    private void callSucceeded() {

        callClicked = true;
        searching = false;

        handler.removeCallbacksAndMessages(null);

        getSharedPreferences(
                "call_control",
                MODE_PRIVATE)
                .edit()
                .putBoolean(
                        "call_requested",
                        false)
                .apply();
    }

    private void finishRequest() {

        searching = false;
        callClicked = false;

        handler.removeCallbacksAndMessages(null);

        getSharedPreferences(
                "call_control",
                MODE_PRIVATE)
                .edit()
                .putBoolean(
                        "call_requested",
                        false)
                .apply();
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        searching = false;
        callClicked = false;
        attempts = 0;
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        searching = false;
        callClicked = false;

        super.onDestroy();
    }
}
