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

        if (event == null || event.getPackageName() == null) {
            return;
        }

        if (!event.getPackageName().toString().equals("com.whatsapp")) {
            return;
        }

        if (checking) {
            return;
        }

        checking = true;

        handler.postDelayed(() -> {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root != null) {

                StringBuilder result =
                        new StringBuilder();

                result.append(
                        "WHATSAPP BUTTON INFORMATION\n\n");

                collectNodes(root, result);

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

        }, 1500);
    }

    private void collectNodes(
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
                collectNodes(child, result);
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
    }
}
