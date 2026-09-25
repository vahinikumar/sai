package com.example.onebuttonwhatsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String WHATSAPP_NUMBER = "917993365553";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startWhatsAppCall();

        finish();
    }

    private void startWhatsAppCall() {

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
