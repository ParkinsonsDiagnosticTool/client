package com.blackberry.dhalam.pdt;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OptionActivity extends AppCompatActivity {
    Button learningMode;
    Button diagnosticMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        getSupportActionBar().hide();

        TextView logoText = (TextView) findViewById(R.id.logoText);
        Typeface tf = Typeface.createFromAsset(this.getAssets(),
                "zwodrei_bold_demo.ttf");
        logoText.setTypeface(tf);

        learningMode = (Button) findViewById(R.id.learn);
        diagnosticMode = (Button) findViewById(R.id.diagnose);

        learningMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OptionActivity.this, TestActivity.class);
                intent.putExtra("receive", false);
                startActivity(intent);
            }
        });

        diagnosticMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OptionActivity.this, TestActivity.class);
                intent.putExtra("receive", true);
                startActivity(intent);
            }
        });

    }
}
