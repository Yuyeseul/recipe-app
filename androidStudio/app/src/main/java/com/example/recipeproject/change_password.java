package com.example.recipeproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class change_password extends AppCompatActivity {
    EditText pw, pwCheck;
    Button changeButton;
    ImageButton back;
    String userId; // 아이디를 저장할 변수
    MainActivity.MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        pw = (EditText) findViewById(R.id.pw);
        pwCheck = (EditText) findViewById(R.id.pwCheck);
        changeButton =(Button)  findViewById(R.id.button);
        back = (ImageButton) findViewById(R.id.btnBack);

        // Intent로부터 아이디 값을 받기
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");


        dbHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 변경하기 버튼 클릭 리스너
        changeButton.setOnClickListener(v -> {
            String newPassword = pw.getText().toString();
            String confirmPassword = pwCheck.getText().toString();

            if (newPassword.equals(confirmPassword)) {
                // 비밀번호 변경을 서버에 요청
                sendPasswordChangeRequestToServer(userId, newPassword);
            } else {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 비밀번호 변경 완료 다이얼로그
    private void showConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("변경 완료")
                .setMessage("비밀번호가 변경되었습니다.")
                .setPositiveButton("확인", (dialog, which) -> {
                    // 로그인 화면으로 이동
                    navigateToLogin();
                })
                .setCancelable(false) // 다이얼로그 외부 터치로 닫히지 않도록 설정
                .show();
    }

    // 로컬 데이터베이스에 비밀번호 업데이트
    private boolean updatePasswordInDB(String userId, String newPassword) {
        // 데이터베이스 쓰기
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pw", newPassword); // 비밀번호 필드에 새로운 비밀번호 저장

        // 업데이트 쿼리 실행
        int rowsAffected = db.update("user", values, "id = ?", new String[]{userId});
        db.close(); // 데이터베이스 연결 종료

        return rowsAffected > 0; // 성공적으로 업데이트되었는지 확인
    }

    // 서버에 비밀번호 변경 요청 (옵션)
    private void sendPasswordChangeRequestToServer(String userId, String newPassword) {
        // 서버 URL (비밀번호 변경 PHP 스크립트 위치)
        String url = "https://ys.calab.myds.me/changePw.php"; // 여기를 실제 URL로 변경하세요.

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 서버 응답 처리
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                // 로컬 데이터베이스에 비밀번호 업데이트
                                if (updatePasswordInDB(userId, newPassword)) {
                                    // 비밀번호 변경 완료 다이얼로그 표시
                                    showConfirmationDialog();
                                } else {
                                    Toast.makeText(change_password.this, "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(change_password.this, "서버에서 비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(change_password.this, "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 오류 처리
                        Toast.makeText(change_password.this, "서버와 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // 서버에 보낼 파라미터 설정
                Map<String, String> params = new HashMap<>();
                params.put("id", userId);
                params.put("pw", newPassword);
                return params;
            }
        };

        // Volley 요청 큐에 추가
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // 로그인 화면으로 이동
    private void navigateToLogin() {
        Intent intent = new Intent(change_password.this, LoginActivity.class);
        startActivity(intent);
        finish(); // 현재 Activity 종료
    }
}