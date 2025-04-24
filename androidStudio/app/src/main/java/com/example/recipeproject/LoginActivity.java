package com.example.recipeproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText editId, editPw;
    Button btnLogin;
    TextView join, findId, findPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        editId = (EditText) findViewById(R.id.editID); // editId에 맞는 ID 사용
        editPw = (EditText) findViewById(R.id.editPW); // editPw에 맞는 ID 사용

        btnLogin = (Button) findViewById(R.id.btnLogin);
        join = (TextView) findViewById(R.id.tJoin);
        findId = (TextView) findViewById(R.id.tFindId);
        findPw = (TextView) findViewById(R.id.tFindPw);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id = editId.getText().toString().trim();
                String pw = editPw.getText().toString().trim();

                // 입력값이 비어있는지 확인
                if (id.isEmpty() || pw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return; // 입력이 비어있으면 메서드 종료
                }

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 로그인에 성공
                                String id = jsonObject.getString("id");
                                String nickname = jsonObject.getString("nickname");
                                String name = jsonObject.getString("name");
                                String phone = jsonObject.getString("phone");
                                SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("userId", id);  // userId는 로그인 성공 시의 아이디
                                editor.putString("nickname", nickname);  // 닉네임 저장
                                editor.putString("name", name);
                                editor.putString("phone", phone);
                                editor.apply();

                                finish();

                            } else { // 로그인에 실패
                                Toast.makeText(getApplicationContext(),"로그인에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"로그인에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(id, pw, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });

        findId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FindIdActivity.class);
                startActivity(intent);
            }
        });

        findPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FindPwActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        // 앱 종료
        super.onBackPressed();
        finishAffinity();
    }
}



class LoginRequest extends StringRequest {
    final static private String URL = "https://ys.calab.myds.me/login.php";
    private Map<String, String> map;

    public LoginRequest(String id, String pw, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("id", id);
        map.put("pw", pw);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}