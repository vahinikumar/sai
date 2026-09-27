package com.example.onebuttonwhatsapp.direct;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String WHATSAPP_NUMBER = "917993365553";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(15, 15, 15, 15);

        Button callButton = new Button(this);

        callButton.setBackgroundResource(
                R.drawable.grandma_call);

        callButton.setText("");
        callButton.setGravity(Gravity.CENTER);
        callButton.setPadding(0, 0, 0, 0);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);

        root.addView(
                callButton,
                buttonParams);

        callButton.setOnClickListener(
                v -> startWhatsAppCall());

        setContentView(root);
    }

    private void startWhatsAppCall() {

        getSharedPreferences(
                "call_control",
                MODE_PRIVATE)
                .edit()
                .putBoolean(
                        "call_requested",
                        true)
                .apply();

        Uri uri = Uri.parse(
                "https://wa.me/" +
                WHATSAPP_NUMBER);

        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW,
                        uri);

        intent.setPackage("com.whatsapp");

        try {

            startActivity(intent);

            // Close our app after WhatsApp is launched.
            finish();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
