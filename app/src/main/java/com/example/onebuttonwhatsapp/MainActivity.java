package com.example.onebuttonwhatsapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // CHANGE THIS to the user's WhatsApp number, including country code.
    // India example: 919876543210 (no +, spaces, or dashes).
    private static final String WHATSAPP_NUMBER = "917993365553";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("Grandma's WhatsApp Call");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Button callButton = new Button(this);
        callButton.setText(getString(R.string.call_me));
        callButton.setTextSize(24);
        callButton.setAllCaps(false);
        callButton.setMinHeight(220);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 320);
        buttonParams.setMargins(0, 60, 0, 30);
        root.addView(callButton, buttonParams);

        TextView help = new TextView(this);
        help.setText("First enable the app in Accessibility settings.");
        help.setTextSize(16);
        help.setGravity(Gravity.CENTER);
        root.addView(help);

        callButton.setOnClickListener(v -> startWhatsAppCall());

        setContentView(root);
    }

    private void startWhatsAppCall() {
        if (WHATSAPP_NUMBER.contains("X")) {
            Toast.makeText(this, "Set WHATSAPP_NUMBER in MainActivity.java first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Open the WhatsApp chat for this number.
        // The AccessibilityService then looks for WhatsApp's voice-call control.
        Uri uri = Uri.parse("https://wa.me/" + WHATSAPP_NUMBER);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp is not installed.", Toast.LENGTH_LONG).show();
        }
    }

    public void openAccessibilitySettings(View view) {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }
}
