package com.moushi.msdynamicdemo;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MSDynamic.activateWithUUID(this, "30042becfd0f4455be00640a50a27c6e", "activateWithUUID_Test_ID_01");
        final TextView isTwiceText = (TextView) this.findViewById(R.id.is_twice_text);

        this.findViewById(R.id.is_twice_send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MSDynamic.isTwiceActivate(getApplicationContext());
                isTwiceText.setVisibility(View.VISIBLE);
            }
        });
    }

}
