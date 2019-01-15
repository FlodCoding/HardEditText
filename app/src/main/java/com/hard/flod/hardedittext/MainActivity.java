package com.hard.flod.hardedittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.hard.flod.HardEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final HardEditText hardEditText = findViewById(R.id.hardText);
        final EditText editText = findViewById(R.id.normal_editText);
        final EditText hardEditText2 = findViewById(R.id.hardText2);
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hardEditText.setError("err");
                editText.setError("err");
                hardEditText2.setError("err");
            }
        });




    }
}
