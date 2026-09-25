package com.example.onebuttonwhatsapp;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

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
                findButtons(root);
            }

            checking = false;

        }, 1500);
    }

    private void findButtons(AccessibilityNodeInfo node) {

        if (node == null) {
            return;
        }

        String className = "";
        String text = "";
        String description = "";
        String viewId = "";

        if (node.getClassName() != null) {
            className = node.getClassName().toString();
        }

        if (node.getText() != null) {
            text = node.getText().toString();
        }

        if (node.getContentDescription() != null) {
            description =
                    node.getContentDescription().toString();
        }

        if (node.getViewIdResourceName() != null) {
            viewId =
                    node.getViewIdResourceName();
        }

        if (node.isClickable() ||
                className.contains("Button")) {

            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);

            String info =
                    "CLASS: " + className +
                    "\nTEXT: " + text +
                    "\nDESC: " + description +
                    "\nID: " + viewId +
                    "\nBOUNDS: " + bounds;

            android.util.Log.d(
                    "WHATSAPP_BUTTON",
                    info
            );

            // Show possible call-related buttons
            String all = (
                    text + " " +
                    description + " " +
                    viewId
            ).toLowerCase();

            if (all.contains("call") ||
                    all.contains("phone") ||
                    all.contains("voice")) {

                Toast.makeText(
                        this,
                        "CALL BUTTON FOUND:\n" + info,
                        Toast.LENGTH_LONG
                ).show();
            }
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {
                findButtons(child);
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
