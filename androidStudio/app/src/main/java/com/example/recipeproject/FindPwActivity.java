package com.example.recipeproject;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class FindPwActivity extends AppCompatActivity {

    static final int SMS_SEND_PERMISSION = 1;
    Button findPwInquiry, pwAuthentication, btnCheck;
    EditText pwId, pwPhone, pwNum;
    ImageButton back;
    MainActivity.MyDBHelper dbHelper;

    String chechNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_pw);

        findPwInquiry = (Button) findViewById(R.id.pwInquiry);
        pwAuthentication = (Button) findViewById(R.id.pwAuthentication);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        pwId = (EditText) findViewById(R.id.pwId);
        pwPhone = (EditText) findViewById(R.id.pwPhone);
        pwNum = (EditText) findViewById(R.id.pwNumber);
        back = (ImageButton) findViewById(R.id.btnBack);

        dbHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int permissioncheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS);
        if (permissioncheck != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
        } else {
            // SMS 권한이 이미 허용된 경우
//            Toast.makeText(getApplicationContext(), "SMS 권한이 이미 허용되었습니다.", Toast.LENGTH_SHORT).show();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        pwAuthentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNum = pwPhone.getText().toString();
                if (phoneNum.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                chechNum = numberGem(4, 1);
                editor.putString("checkNum", chechNum);
                editor.commit();
                sendSMS(phoneNum, "인증번호 : " + chechNum);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pref.getString("checkNum","").equals(pwPhone.getText().toString())){
                    Toast.makeText(getApplicationContext(),"인증 완료 되었습니다.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"인증번호가 일치하지 되었습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        findPwInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = pwId.getText().toString();
                String phoneNum = pwPhone.getText().toString();

                // 입력값 검증
                if (id.isEmpty() || phoneNum.isEmpty() || pwNum.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디, 전화번호, 인증번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 아이디와 전화번호 확인
                if (dbHelper.checkUser(id, phoneNum) && pref.getString("checkNum", "").equals(pwNum.getText().toString())) {
//                    Toast.makeText(getApplicationContext(), "인증 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FindPwActivity.this, change_password.class);
                    intent.putExtra("userId", id); // 아이디 값을 추가
                    startActivity(intent); // ChangePasswordActivity로 이동
                } else {
                    Toast.makeText(getApplicationContext(), "아이디 또는 전화번호가 일치하지 않거나 인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void sendSMS(String phoneNum, String message) {
        // PendingIntent를 Broadcast 형태로 설정하여 화면 전환을 방지
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNum, null, message, pi, null);
    }


    public static String numberGem(int len, int dupCd){
        Random rand = new Random();
        String numStr = "";
        for(int i = 0;i<len;i++){
            String ran = Integer.toString(rand.nextInt(10));
            if(dupCd==1){
                numStr += ran;
            }else if(dupCd==2){
                if(!numStr.contains(ran)){
                    numStr += ran;
                }else{
                    i-=1;
                }
            }
        }
        return numStr;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_SEND_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS 전송 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS 전송 권한이 거부되었습니다. 권한을 허용해야 SMS를 전송할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}