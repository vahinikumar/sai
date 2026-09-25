package com.example.onebuttonwhatsapp;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

public class WhatsAppCallAccessibilityService extends AccessibilityService {

    private final Handler handler = new Handler();
    private boolean callClicked = false;
    private int attempts = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        CharSequence packageName = event.getPackageName();

        if (packageName == null ||
                !packageName.toString().equals("com.whatsapp")) {
            return;
        }

        if (callClicked) {
            return;
        }

        // Try several times because WhatsApp may load the screen slowly.
        attempts = 0;
        handler.removeCallbacksAndMessages(null);

        tryClick();
    }

    private void tryClick() {

        if (callClicked || attempts >= 8) {
            return;
        }

        attempts++;

        handler.postDelayed(() -> {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root != null) {

                if (findCallButton(root)) {
                    callClicked = true;
                    return;
                }
            }

            // Try again
            tryClick();

        }, 500);
    }

    private boolean findCallButton(AccessibilityNodeInfo root) {

        // First try text/content descriptions.
        String[] labels = {
                "voice call",
                "audio call",
                "voice",
                "call",
                "phone",
                "make a call",
                "start a call"
        };

        for (String label : labels) {

            if (clickByText(root, label)) {
                return true;
            }
        }

        // Then search for WhatsApp button nodes.
        return clickButtonNode(root);
    }

    private boolean clickByText(
            AccessibilityNodeInfo node,
            String wanted) {

        if (node == null) {
            return false;
        }

        CharSequence text = node.getText();
        CharSequence description =
                node.getContentDescription();

        if (contains(text, wanted) ||
                contains(description, wanted)) {

            if (node.isClickable()) {

                if (node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK)) {

                    return true;
                }
            }

            AccessibilityNodeInfo parent =
                    node.getParent();

            if (parent != null &&
                    parent.isClickable()) {

                if (parent.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK)) {

                    return true;
                }
            }
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                if (clickByText(child, wanted)) {
                    child.recycle();
                    return true;
                }

                child.recycle();
            }
        }

        return false;
    }

    private boolean clickButtonNode(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return false;
        }

        String className =
                node.getClassName() == null
                        ? ""
                        : node.getClassName().toString();

        String viewId =
                node.getViewIdResourceName() == null
                        ? ""
                        : node.getViewIdResourceName();

        String description =
                node.getContentDescription() == null
                        ? ""
                        : node.getContentDescription()
                                .toString()
                                .toLowerCase(Locale.ROOT);

        /*
         * WhatsApp normally uses ImageButton/Button
         * for the call icon.
         *
         * We only click a button when its information
         * looks related to calling.
         */
        boolean isButton =
                className.contains("Button");

        boolean looksLikeCall =
                description.contains("call") ||
                description.contains("phone") ||
                description.contains("voice") ||
                viewId.toLowerCase(Locale.ROOT)
                        .contains("call");

        if (isButton && looksLikeCall) {

            if (node.isClickable()) {

                if (node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK)) {

                    return true;
                }
            }

            AccessibilityNodeInfo parent =
                    node.getParent();

            if (parent != null &&
                    parent.isClickable()) {

                if (parent.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK)) {

                    return true;
                }
            }
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                if (clickButtonNode(child)) {
                    child.recycle();
                    return true;
                }

                child.recycle();
            }
        }

        return false;
    }

    private boolean contains(
            CharSequence value,
            String wanted) {

        if (value == null) {
            return false;
        }

        return value.toString()
                .toLowerCase(Locale.ROOT)
                .contains(
                        wanted.toLowerCase(Locale.ROOT));
    }

    @Override
    public void onInterrupt() {
        // Nothing to do
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        callClicked = false;
        attempts = 0;
    }
}
