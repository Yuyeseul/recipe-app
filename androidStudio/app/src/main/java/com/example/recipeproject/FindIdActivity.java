package com.example.recipeproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FindIdActivity extends AppCompatActivity {

    Button findIdInquiry;
    EditText idName, idPhone;
    TextView findID, findName, text1, text2, text3;
    ImageButton back;
    MainActivity.MyDBHelper dbHelper; // 데이터베이스 헬퍼 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_id);

        findIdInquiry = findViewById(R.id.idInquiry);
        back = findViewById(R.id.btnBack);
        idName = findViewById(R.id.idName);
        idPhone = findViewById(R.id.idPhone);
        findID = findViewById(R.id.findID);
        findName = findViewById(R.id.findName);
        text1 = findViewById(R.id.textView27);
        text2 = findViewById(R.id.textView28);
        text3 = findViewById(R.id.textView30);


        // 데이터베이스 헬퍼 객체 초기화
        dbHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);

        // 화면이 다시 열릴 때 조회된 정보 초기화
        findID.setText("");
        findName.setText("");
        text1.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);
        text3.setVisibility(View.GONE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findIdInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = idName.getText().toString().trim();
                String phone = idPhone.getText().toString().trim();

                // 입력값 검증
                if (name.isEmpty()) {
                    Toast.makeText(FindIdActivity.this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone.isEmpty()) {
                    Toast.makeText(FindIdActivity.this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 로컬 데이터베이스에서 아이디 찾기
                findIdInDatabase(name, phone);
            }
        });
    }

    private void findIdInDatabase(String name, String phone) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT id FROM user WHERE name = ? AND phone = ?";
            cursor = db.rawQuery(query, new String[]{name, phone});

            if (cursor.moveToFirst()) {
                // 사용자의 아이디 찾기
                String foundId = cursor.getString(0);
                text1.setVisibility(View.VISIBLE);
                text2.setVisibility(View.VISIBLE);
                text3.setVisibility(View.VISIBLE);
                findID.setText(foundId);
                findName.setText(name); // 입력한 이름 그대로 표시
            } else {
                Toast.makeText(this, "일치하는 사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            Toast.makeText(this, "데이터베이스 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // 커서 및 데이터베이스 닫기
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}