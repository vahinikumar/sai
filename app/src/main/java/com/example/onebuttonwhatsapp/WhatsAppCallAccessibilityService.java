package com.example.onebuttonwhatsapp;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WhatsAppCallAccessibilityService extends AccessibilityService {

    private final Handler handler = new Handler();
    private boolean checking = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        CharSequence packageName =
                event.getPackageName();

        // Only work when WhatsApp is active.
        if (packageName == null ||
                !packageName.toString().equals("com.whatsapp")) {
            return;
        }

        if (checking) {
            return;
        }

        checking = true;

        // Give WhatsApp a short time to finish loading.
        handler.postDelayed(() -> {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root != null) {

                StringBuilder result =
                        new StringBuilder();

                result.append(
                        "WHATSAPP BUTTON INFORMATION\n\n");

                collectWhatsAppNodes(root, result);

                getSharedPreferences(
                        "diagnostic",
                        MODE_PRIVATE)
                        .edit()
                        .putString(
                                "buttons",
                                result.toString())
                        .apply();
            }

            checking = false;

        }, 800);
    }

    private void collectWhatsAppNodes(
            AccessibilityNodeInfo node,
            StringBuilder result) {

        if (node == null) {
            return;
        }

        String className = "";
        String text = "";
        String description = "";
        String viewId = "";

        if (node.getClassName() != null) {
            className =
                    node.getClassName().toString();
        }

        if (node.getText() != null) {
            text =
                    node.getText().toString();
        }

        if (node.getContentDescription() != null) {
            description =
                    node.getContentDescription()
                            .toString();
        }

        if (node.getViewIdResourceName() != null) {
            viewId =
                    node.getViewIdResourceName();
        }

        // Record clickable elements and buttons.
        if (node.isClickable() ||
                className.contains("Button")) {

            result.append("CLASS: ")
                    .append(className)
                    .append("\n");

            result.append("TEXT: ")
                    .append(text)
                    .append("\n");

            result.append("DESCRIPTION: ")
                    .append(description)
                    .append("\n");

            result.append("VIEW ID: ")
                    .append(viewId)
                    .append("\n");

            result.append("--------------------\n");
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                collectWhatsAppNodes(
                        child,
                        result);

                child.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        checking = false;
    }
}
