package rs.pedjaapps.eventlogger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 4.11.14. 14.42.
 * This class is part of the event-logger
 * Copyright © 2014 ${OWNER}
 */
public class LockActivity extends AbsActivity implements View.OnClickListener
{
    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btnDel, btnOk;
    TextView tvPass;

    String pass = "";
    boolean showPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(!SettingsManager.isPro() || !SettingsManager.isPinEnabled())
        {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_lock);

        btn0 = (Button)findViewById(R.id.btn0);
        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button)findViewById(R.id.btn2);
        btn3 = (Button)findViewById(R.id.btn3);
        btn4 = (Button)findViewById(R.id.btn4);
        btn5 = (Button)findViewById(R.id.btn5);
        btn6 = (Button)findViewById(R.id.btn6);
        btn7 = (Button)findViewById(R.id.btn7);
        btn8 = (Button)findViewById(R.id.btn8);
        btn9 = (Button)findViewById(R.id.btn9);
        btnDel = (Button)findViewById(R.id.btnDel);
        btnOk = (Button)findViewById(R.id.btnOk);

        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btnDel.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        tvPass = (TextView)findViewById(R.id.tvPass);
        tvPass.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn0:
                if(pass.length() < 10)pass += "0";
                break;
            case R.id.btn1:
                if(pass.length() < 10)pass += "1";
                break;
            case R.id.btn2:
                if(pass.length() < 10)pass += "2";
                break;
            case R.id.btn3:
                if(pass.length() < 10)pass += "3";
                break;
            case R.id.btn4:
                if(pass.length() < 10)pass += "4";
                break;
            case R.id.btn5:
                if(pass.length() < 10)pass += "5";
                break;
            case R.id.btn6:
                if(pass.length() < 10)pass += "6";
                break;
            case R.id.btn7:
                if(pass.length() < 10)pass += "7";
                break;
            case R.id.btn8:
                if(pass.length() < 10)pass += "8";
                break;
            case R.id.btn9:
                if(pass.length() < 10)pass += "9";
                break;
            case R.id.btnDel:
                if(pass.length() > 0)pass = pass.substring(0, pass.length() - 1);
                break;
            case R.id.btnOk:
                unlock();
                break;
            case R.id.tvPass:
                togglePasswordVisibility();
                break;
        }
        if (v.getId() != R.id.tvPass)
        {

            tvPass.setText(generateStars());
            if(BuildConfig.DEBUG)Log.d(Constants.LOG_TAG, pass);
        }
    }

    private String generateStars()
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < pass.length(); i++)
        {
            builder.append(Constants.STAR);
        }
        return builder.toString();
    }

    private void togglePasswordVisibility()
    {
        if(showPassword)
        {
            tvPass.setText(pass);
        }
        else
        {
            tvPass.setText(generateStars());
        }
        showPassword = !showPassword;
    }

    private void unlock()
    {
        if(SettingsManager.isPinValid(pass))
        {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else
        {
            Utility.showToast(this, R.string.invalid_pin);
            pass = "";
            tvPass.setText("");
        }
    }
}
