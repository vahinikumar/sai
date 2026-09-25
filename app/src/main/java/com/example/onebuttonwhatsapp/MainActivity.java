package com.example.onebuttonwhatsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String WHATSAPP_NUMBER = "91";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(25, 25, 25, 25);

        TextView title = new TextView(this);
        title.setText("📞\nCALL GRANDMA");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        Button callButton = new Button(this);
        callButton.setText("📞  CALL ME\n       ON WHATSAPP");
        callButton.setTextSize(30);
        callButton.setGravity(Gravity.CENTER);
        callButton.setAllCaps(false);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500);

        buttonParams.setMargins(10, 50, 10, 10);

        root.addView(callButton, buttonParams);

        callButton.setOnClickListener(v -> startWhatsAppCall());

        setContentView(root);
    }

    private void startWhatsAppCall() {

        // Tell the Accessibility Service that WE requested the call.
        getSharedPreferences("call_control", MODE_PRIVATE)
                .edit()
                .putBoolean("call_requested", true)
                .apply();

        Uri uri = Uri.parse(
                "https://wa.me/" + WHATSAPP_NUMBER);

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri);

        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (Exception e) {
        }
    }
}
