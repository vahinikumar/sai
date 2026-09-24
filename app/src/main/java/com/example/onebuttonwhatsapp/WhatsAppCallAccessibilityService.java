package com.example.onebuttonwhatsapp;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.Locale;

public class WhatsAppCallAccessibilityService extends AccessibilityService {

    private final Handler handler = new Handler();
    private boolean callClicked = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence pkg = event.getPackageName();
        if (pkg == null || !pkg.toString().equals("com.whatsapp")) return;

        if (callClicked) return;

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return;

            if (clickCallButton(root)) {
                callClicked = true;
            }
        }, 700);
    }

    private boolean clickCallButton(AccessibilityNodeInfo root) {
        // Try common WhatsApp labels/content descriptions.
        String[] labels = {
                "voice call",
                "call",
                "audio call",
                "voice"
        };

        for (String label : labels) {
            if (clickMatchingNode(root, label)) {
                return true;
            }
        }
        return false;
    }

    private boolean clickMatchingNode(AccessibilityNodeInfo node, String wanted) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();

        if (contains(text, wanted) || contains(desc, wanted)) {
            if (node.isClickable() && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true;
            }

            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null && parent.isClickable()
                    && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (clickMatchingNode(child, wanted)) {
                    return true;
                }
                child.recycle();
            }
        }
        return false;
    }

    private boolean contains(CharSequence value, String wanted) {
        return value != null &&
                value.toString().toLowerCase(Locale.ROOT)
                        .contains(wanted.toLowerCase(Locale.ROOT));
    }

    @Override
    public void onInterrupt() {
        // Nothing to clean up.
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        callClicked = false;
    }
}
