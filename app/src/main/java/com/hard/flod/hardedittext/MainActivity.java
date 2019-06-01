package com.hard.flod.hardedittext;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

                Drawable d = getResources().getDrawable(R.drawable.ic_launcher_foreground);
                d.setBounds(0, 0,
                        d.getIntrinsicWidth(), d.getIntrinsicHeight());

                Drawable d2 = getResources().getDrawable(R.drawable.ic_visibility_off_24dp);
                d2.setBounds(0, 0,
                        d2.getIntrinsicWidth(), d2.getIntrinsicHeight());
                hardEditText.setError("err", d);
                editText.setError("err", d2);
                hardEditText2.setError("err", getResources().getDrawable(R.drawable.ic_visibility_off_24dp));
            }
        });




    }
}
