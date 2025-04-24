package com.example.recipeproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    Button modify_ok, logout, btnDelete;
    EditText userNickname;
    TextView userName, userPhone;
    ImageButton back;
    MainActivity.MyDBHelper dbHelper;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);


        dbHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);

        modify_ok = (Button) findViewById(R.id.btnModify);
        userNickname = (EditText) findViewById(R.id.userNickname);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);
        logout = (Button) findViewById(R.id.btnLogout);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        back = (ImageButton) findViewById(R.id.btnBack);

        sharedPref = this.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", "");  // 저장된 아이디 불러오기
        String name = sharedPref.getString("name", "");
        String phone = sharedPref.getString("phone", "");

        userName.setText(name);
        userPhone.setText(phone);
        userNickname.setText(nickname);

        // 닉네임 입력 시 사용 가능 여부 확인
        userNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nicknameInput = s.toString();
                if (nicknameInput.isEmpty()) {
                    userNickname.setError(null); // 에러 메시지 초기화
                    modify_ok.setEnabled(false); // 수정 버튼 비활성화
                    findViewById(R.id.textView14).setVisibility(View.GONE); // 경고 메시지 숨김
                } else {
                    if (dbHelper.isNicknameAvailable(nicknameInput)) { // 닉네임이 사용 가능할 경우
                        userNickname.setError(null); // 에러 메시지 초기화
                        modify_ok.setEnabled(true); // 수정 버튼 활성화
                        findViewById(R.id.textView14).setVisibility(View.GONE); // 경고 메시지 숨김
                    } else {
                        modify_ok.setEnabled(false); // 수정 버튼 비활성화
                        findViewById(R.id.textView14).setVisibility(View.VISIBLE); // 경고 메시지 보임
                        // 경고 메시지 설정
                        ((TextView) findViewById(R.id.textView14)).setText("사용할 수 없는 닉네임입니다."); // 원하는 경고 메시지로 변경
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        modify_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newNickname = userNickname.getText().toString();
                String oldNickname = sharedPref.getString("nickname", "");
                String id = sharedPref.getString("userId", "");

                // 수정된 닉네임을 SharedPreferences에 저장
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("nickname", newNickname);
                editor.apply();

                dbHelper.updateNickname(id, oldNickname, newNickname);

                // 서버에 닉네임 업데이트 요청
                updateNicknameOnServer(oldNickname, newNickname);

                // 에디트텍스트에 새로운 닉네임 표시
                userNickname.setText(newNickname);

                findViewById(R.id.textView14).setVisibility(View.GONE); // 경고 메시지 숨김
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 경고 다이얼로그 표시
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("탈퇴 확인")
                        .setMessage("정말로 탈퇴하시겠습니까? 탈퇴하면 복구할 수 없습니다.")
                        .setPositiveButton("확인", (dialog, which) -> {
                            // 1. 로컬 DB에서 계정 삭제
                            String nicknameToDelete = sharedPref.getString("nickname", "");
                            dbHelper.deleteUser(nicknameToDelete);
                            deleteUserFromServer(nicknameToDelete);

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.apply();

                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });
    }

    // 서버에 닉네임 업데이트 요청을 보내는 메서드
    private void updateNicknameOnServer(String oldNickname, String newNickname) {
        String url = "http://ys.calab.myds.me/userUpdateNickname.php"; // 서버 URL로 변경

        String id = sharedPref.getString("userId", "");  // 저장된 아이디 불러오기
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // 서버에서 성공적으로 닉네임이 업데이트되었을 때 로컬 DB 및 SharedPreferences 업데이트
                            dbHelper.updateNickname(id, oldNickname, newNickname);

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("nickname", newNickname);
                            editor.apply();

                            Toast.makeText(ProfileActivity.this, "닉네임이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = jsonResponse.optString("error", "서버 오류가 발생했습니다.");
                            Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ProfileActivity.this, "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ProfileActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id); // userId 추가
                params.put("nickname", newNickname); // 새 닉네임
                params.put("old_nickname", oldNickname); // 이전 닉네임 추가
                Log.d("UpdateNicknameParams", "userId: " + id + ", oldNickname: " + oldNickname + ", newNickname: " + newNickname); // 로그 추가
                return params;
            }

        };

        // 요청 큐에 추가
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // 서버에 사용자 삭제 요청을 보내는 메서드
    private void deleteUserFromServer(String nickname) {
        // 서버 URL 설정
        String url = "http://ys.calab.myds.me/userDelete.php";

        // 서버에 요청 보내기
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        // 서버에서 성공적으로 삭제된 경우
                        Toast.makeText(ProfileActivity.this, "계정이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "서버 오류가 발생했습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ProfileActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nickname", nickname); // 닉네임을 서버에 전송
                return params;
            }
        };

        // 요청 큐에 추가
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}