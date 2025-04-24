package com.example.recipeproject;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {

    static final int SMS_SEND_PERMISSION = 1;
    EditText joinname, joinid, joinpw, joinpwcheck, joinphone, joinnickname, joinbirth, joinnumcheck;
    Button btncheck, btnok, phoneCheck, numCheck;
    ImageButton back;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper dbHelper;
    Boolean joinIDChecked = false;
    String chechNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join);

        joinname = (EditText) findViewById(R.id.joinName);
        joinid = (EditText) findViewById(R.id.joinId);
        joinpw = (EditText) findViewById(R.id.joinPw);
        joinpwcheck = (EditText) findViewById(R.id.joinPwCheck);
        joinphone = (EditText) findViewById(R.id.joinPhone);
        joinnickname = (EditText) findViewById(R.id.joinNickName);
        joinbirth = (EditText) findViewById(R.id.joinBirth);
        joinnumcheck = findViewById(R.id.joinNumCheck);

        btncheck = (Button) findViewById(R.id.btnCheck);
        btnok = (Button) findViewById(R.id.btnOk);
        phoneCheck = (Button) findViewById(R.id.btnPhoneCheck);
        numCheck = (Button) findViewById(R.id.btnNumCheck);
        back = (ImageButton) findViewById(R.id.jBack);

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

        dbHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);
        sqlDB = dbHelper.getReadableDatabase();

        ArrayList<String> phoneList = new ArrayList<>();
        String phoneSql = "SELECT phone FROM user";
        Cursor cursor = sqlDB.rawQuery(phoneSql, null);
        while (cursor.moveToNext()) {
            String phone = cursor.getString(0);
            phoneList.add(phone);
        }
        cursor.close();

        phoneCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode(); // 인증 코드 전송 메서드 호출
            }
        });

        numCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode(); // 인증 코드 확인 메서드 호출
            }
        });


        btncheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkDuplicateID();
//                ArrayList<String> idList = new ArrayList<>();
//
//                String sql = "SELECT id FROM user";
//                Cursor cursor = sqlDB.rawQuery(sql, null);
//                while (cursor.moveToNext()) {
//                    String id = cursor.getString(0);
//                    idList.add(id);
//                }
//                cursor.close();
//
//                if(!idList.contains(joinid.getText().toString())) {
//                    joinIDChecked = true;
//                    Toast.makeText(getApplicationContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(getApplicationContext(), "사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = joinid.getText().toString();
                String pw = joinpw.getText().toString();
                String pwcheck = joinpwcheck.getText().toString();
                String name = joinname.getText().toString();
                String nickname = joinnickname.getText().toString();
                String birth = joinbirth.getText().toString();
                String phone = joinphone.getText().toString();

                check_validation();


                if (id.isEmpty() || pw.isEmpty() || pwcheck.isEmpty() || name.isEmpty() || nickname.isEmpty()
                        || birth.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "빈칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (!joinIDChecked) {
                    Toast.makeText(getApplicationContext(), "아이디 중복 여부를 확인해주세요.", Toast.LENGTH_SHORT).show();
                } else if (!pw.equals(pwcheck)) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }  else if (!isValidBirth(birth)) {
                    Toast.makeText(getApplicationContext(),  "생년월일은 6자리 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (!isPhoneVerified()) {
                    Toast.makeText(getApplicationContext(), "전화번호 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    Response.Listener<String> responseListner = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean success = jsonObject.getBoolean("success");
                                if(success){
                                    dbHelper.saveUserToLocalDB(id, pw, name, nickname, birth, phone);

                                    Toast.makeText(getApplicationContext(),"회원등록 성공",Toast.LENGTH_SHORT).show();
                                    finish();
                                }else{
                                    Toast.makeText(getApplicationContext(),"회원등록 실패",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }catch (JSONException e){
                                Toast.makeText(getApplicationContext(), "JSON 파싱 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    };
                    RegisterRequest registerRequest = new RegisterRequest(id, pw, name, nickname, birth, phone, responseListner);
                    RequestQueue queue = Volley.newRequestQueue(JoinActivity.this);
                    queue.add(registerRequest);
                }

            }
        });
    }

    private boolean isValidBirth(String birth) {
        return birth.matches("^\\d{6}$"); // 6자리 숫자 정규식
    }

    private boolean isPhoneVerified() {
        return joinnumcheck.getText().toString().equals(chechNum); // 인증번호가 일치하는지 확인
    }

    void check_validation() {
        String pw = joinpw.getText().toString();

        // 비밀번호 유효성 검사: 대소문자 영문자와 숫자가 포함되어야 하며, 특수문자는 포함되지 않아야 함
        String val_alpha_numeric = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$"; // 영문자와 숫자만 포함
        Pattern pattern_alpha_numeric = Pattern.compile(val_alpha_numeric);
        Matcher matcher_alpha_numeric = pattern_alpha_numeric.matcher(pw);

        if (pw.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else if (!matcher_alpha_numeric.matches()) {
            Toast.makeText(this, "비밀번호는 영문자와 숫자를 포함해야 하며, 특수문자는 포함되지 않아야 합니다", Toast.LENGTH_SHORT).show();
        } else {
            // 회원가입 진행
            Toast.makeText(this, "회원가입 진행 중...", Toast.LENGTH_SHORT).show();
            // 여기서 회원가입 로직을 호출하거나 처리하면 됩니다.
        }
    }


    // ID 중복 확인 메서드
    private void checkDuplicateID() {
        String id = joinid.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }


        // 로컬 DB에서 ID 중복 확인
        if (dbHelper.isIDDuplicate(id)) {
            Toast.makeText(this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
        } else {
            joinIDChecked = true; // 중복되지 않으면 true로 설정
            Toast.makeText(this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
        }
    }


    // 인증 코드 전송 메서드
    private void sendVerificationCode() {
        String phone = joinphone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 인증 번호 생성
        Random random = new Random();
        chechNum = String.format("%06d", random.nextInt(999999)); // 6자리 랜덤 숫자
        sendSMS(phone, chechNum); // SMS 전송 메서드 호출
    }

    // SMS 전송 메서드
    private void sendSMS(String phone, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, "인증번호: " + message, null, null);
            Toast.makeText(this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 인증 코드 확인 메서드
    private void verifyCode() {
        String inputCode = joinnumcheck.getText().toString().trim();
        if (inputCode.isEmpty()) {
            Toast.makeText(this, "인증번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputCode.equals(chechNum)) {
            Toast.makeText(this, "인증 성공!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
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

class RegisterRequest extends StringRequest {

    final static private String URL = "https://ys.calab.myds.me/register.php";
    private Map<String, String> map;
    public RegisterRequest(String id, String pw, String name, String nickname, String birth, String phone, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("id",id);
        map.put("pw", pw);
        map.put("name",name);
        map.put("nickname",nickname);
        map.put("birth",birth);
        map.put("phone",phone);

    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}