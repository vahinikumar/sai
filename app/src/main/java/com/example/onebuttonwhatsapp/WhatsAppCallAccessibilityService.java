package com.example.onebuttonwhatsapp.direct;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WhatsAppCallAccessibilityService
        extends AccessibilityService {

    private final Handler handler = new Handler();

    private boolean searching = false;
    private boolean callClicked = false;
    private int attempts = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

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

        /*
         * Start a fresh search for every new call request.
         */
        if (!searching) {

            searching = true;
            callClicked = false;
            attempts = 0;

            findVoiceCallButton();
        }
    }

    private void findVoiceCallButton() {

        /*
         * Give WhatsApp plenty of time to load.
         */
        if (callClicked || attempts >= 25) {

            if (!callClicked) {

                searching = false;

                getSharedPreferences(
                        "call_control",
                        MODE_PRIVATE)
                        .edit()
                        .putBoolean(
                                "call_requested",
                                false)
                        .apply();
            }

            return;
        }

        attempts++;

        handler.postDelayed(() -> {

            if (callClicked) {
                return;
            }

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root != null) {

                if (clickVoiceCallButton(root)) {

                    callClicked = true;
                    searching = false;

                    /*
                     * Only clear the request after
                     * the Voice call button was clicked.
                     */
                    getSharedPreferences(
                            "call_control",
                            MODE_PRIVATE)
                            .edit()
                            .putBoolean(
                                    "call_requested",
                                    false)
                            .apply();

                    root.recycle();

                    return;
                }

                root.recycle();
            }

            /*
             * WhatsApp may still be loading.
             * Try again.
             */
            findVoiceCallButton();

        }, 500);
    }

    private boolean clickVoiceCallButton(
            AccessibilityNodeInfo node) {

        if (node == null) return false;

        CharSequence description =
                node.getContentDescription();

        if (description != null) {

            String value =
                    description.toString()
                            .trim();

            if (value.equalsIgnoreCase(
                    "Voice call")) {

                /*
                 * First try clicking the button itself.
                 */
                if (node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK)) {

                    return true;
                }

                /*
                 * If the ImageButton itself cannot be
                 * clicked, try its clickable parent.
                 */
                AccessibilityNodeInfo parent =
                        node.getParent();

                while (parent != null) {

                    if (parent.isClickable()) {

                        boolean clicked =
                                parent.performAction(
                                        AccessibilityNodeInfo
                                                .ACTION_CLICK);

                        AccessibilityNodeInfo next =
                                parent.getParent();

                        parent.recycle();

                        if (clicked) {
                            return true;
                        }

                        parent = next;

                    } else {

                        AccessibilityNodeInfo next =
                                parent.getParent();

                        parent.recycle();

                        parent = next;
                    }
                }
            }
        }

        /*
         * Search all child nodes.
         */
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
