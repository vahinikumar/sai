package com.example.onebuttonwhatsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final String WHATSAPP_NUMBER = "91XXXXXXXXXX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("Grandma's WhatsApp Call");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        Button callButton = new Button(this);
        callButton.setText("CALL ME ON WHATSAPP");
        callButton.setTextSize(22);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        250);

        buttonParams.setMargins(0, 40, 0, 20);

        root.addView(callButton, buttonParams);

        Button diagnosticButton = new Button(this);
        diagnosticButton.setText("SHOW WHATSAPP BUTTON INFO");
        diagnosticButton.setTextSize(18);

        root.addView(diagnosticButton);

        callButton.setOnClickListener(v -> startWhatsAppCall());

        diagnosticButton.setOnClickListener(v -> showDiagnosticInfo());

        setContentView(root);
    }

    private void startWhatsAppCall() {

        if (WHATSAPP_NUMBER.contains("X")) {
            return;
        }

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

    private void showDiagnosticInfo() {

        String data = "";

        try {

            FileInputStream fis =
                    openFileInput(
                            "whatsapp_buttons.txt");

            byte[] bytes =
                    new byte[fis.available()];

            fis.read(bytes);
            fis.close();

            data = new String(
                    bytes,
                    StandardCharsets.UTF_8);

        } catch (Exception e) {

            data = "No diagnostic information found yet.";
        }

        TextView info = new TextView(this);

        info.setText(data);
        info.setTextSize(12);
        info.setPadding(10, 20, 10, 20);

        setContentView(info);
    }
}
