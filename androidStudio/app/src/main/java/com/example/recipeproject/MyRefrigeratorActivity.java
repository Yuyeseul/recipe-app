package com.example.recipeproject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyRefrigeratorActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RefrigeratorAdapter adapter;
    private ArrayList<IngredientData> ingredientsList;

    private Button btnAdd;
    private Button btnRecipes;

    private static final int REQUEST_CODE_ADD_INGREDIENT = 1;

    private MainActivity.MyDBHelper dbHelper;

    SQLiteDatabase db;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_refrigerator);
        // 버튼 설정
        btnAdd = findViewById(R.id.btnAdd);
        btnRecipes = findViewById(R.id.btnRecipe);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("");
        back = (ImageButton) findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dbHelper = new MainActivity.MyDBHelper(this, "recipe", null, 1); // DBHelper 초기화
        db = dbHelper.getReadableDatabase();

        // RecyclerView 설정

        recyclerView = findViewById(R.id.ingredientList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        ingredientsList = new ArrayList<>();
        adapter = new RefrigeratorAdapter(this, ingredientsList, 0);
        recyclerView.setAdapter(adapter);

        // 닉네임 가져오기
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", "");

        loadIngredientsForUser(nickname);

        // RecyclerView 아이템 클릭 리스너 설정
        adapter.setOnItemClickListener(new RefrigeratorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showDeleteDialog(position, nickname); // 삭제 확인 대화상자 표시
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRefrigeratorActivity.this, AddIngredientActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_INGREDIENT);
            }
        });

        btnRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRefrigeratorActivity.this, RefrigeratorRecipe.class);
                startActivity(intent);

            }
        });
    }

    private void loadIngredientsForUser(String nickname) {
//        ingredientsList.clear(); // 기존 리스트 초기화
//        ArrayList<IngredientData> ingredients = dbHelper.getIngredientsByNickname(nickname);
//        ingredientsList.addAll(ingredients); // 데이터베이스에서 재료 가져오기
//        Log.d("MyRefrigeratorActivity", "Loaded Ingredients: " + ingredients.size()); // 로드된 재료 개수 확인
//
//        adapter.notifyDataSetChanged(); // RecyclerView 업데이트
//        // 로드된 각 재료 이름 로그 출력
//        for (IngredientData ingredient : ingredientsList) {
//            Log.d("MyRefrigeratorActivity", "Ingredient: " + ingredient.ingredientName);
//        }
        ingredientsList.clear();
        String sql = "SELECT ingredientName FROM refrigerator WHERE nickname = ?";
        Cursor cursor = null;

        cursor = db.rawQuery(sql, new String[]{nickname});

        Log.d("MyRefrigeratorActivity", "Number of ingredients loaded: " + cursor.getCount());

        while (cursor.moveToNext()) {
            String ingredientName = cursor.getString(0);
            IngredientData data = new IngredientData(ingredientName);

            ingredientsList.add(data);

            // 각 재료 이름을 로그에 출력
            Log.d("MyRefrigeratorActivity", "Loaded ingredient: " + ingredientName);
        }
        adapter.notifyDataSetChanged();
        Log.d("MyRefrigeratorActivity", "Ingredients List Size: " + ingredientsList.size()); // 리스트 크기 로그 출력
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CODE_ADD_INGREDIENT && resultCode == Activity.RESULT_OK) {
//            if (data != null) {
//                String ingredientName = data.getStringExtra("selectedIngredient");
//
//                if (ingredientName != null && !ingredientName.isEmpty()) {
//                    Log.d("MyRefrigeratorActivity", "Received Ingredient: " + ingredientName);
//
//                    SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
//                    String nickname = sharedPref.getString("nickname", "");
//
//                    IngredientData newIngredient = new IngredientData(); // IngredientData 객체 생성
//                    newIngredient.ingredientName = ingredientName; // 재료 이름 설정
//                    ingredientsList.add(newIngredient); // 리스트에 추가
//                    Log.d("MyRefrigeratorActivity", "Ingredients List Size: " + ingredientsList.size());
//
//                    adapter.notifyDataSetChanged(); // RecyclerView 업데이트
//                    for (IngredientData ingredient : ingredientsList) {
//                        Log.d("MyRefrigeratorActivity", "RecyclerView Item: " + ingredient.ingredientName);
//                    }
//                    dbHelper.addIngredient(nickname, ingredientName);  // 데이터베이스에 추가
////
////                    adapter.addIngredient(newIngredient); // 어댑터에 추가
////                    dbHelper.addIngredient(ingredientName); // 데이터베이스에 추가
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ADD_INGREDIENT && resultCode == Activity.RESULT_OK) {
            // 사용자에 대한 재료 다시 로드
            SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
            String nickname = sharedPref.getString("nickname", "");
            if (requestCode == 1 && resultCode == RESULT_OK) {
                if (db == null || !db.isOpen()) {
                    db = dbHelper.getWritableDatabase(); // 데이터베이스 열기
                }
                loadIngredientsForUser(nickname); // 사용자의 재료 불러오기
            }
            // 리스트 크기와 각 항목 출력
            Log.d("MyRefrigeratorActivity", "Ingredients List Size: " + ingredientsList.size());
            for (IngredientData ingredient : ingredientsList) {
                Log.d("MyRefrigeratorActivity", "RecyclerView Item: " + ingredient.ingredientName);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDeleteDialog(final int position, final String nickname) {
        final IngredientData ingredientToDelete = ingredientsList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("삭제 확인")
                .setMessage("해당 재료를 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        sendDeleteRequest(nickname, ingredientToDelete.ingredientName, position);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    private void sendDeleteRequest(final String nickname, final String ingredientName, final int position) {
        Log.d("MyRefrigeratorActivity", "Nickname: " + nickname);
        Log.d("MyRefrigeratorActivity", "Ingredient Name: " + ingredientName);
        String url = "http://ys.calab.myds.me/refrigeratorDelete.php"; // URL 확인
        Log.d("MyRefrigeratorActivity", "Sending DELETE request to URL: " + url);
        Log.d("MyRefrigeratorActivity", "Sending delete request for ingredient: " + ingredientName);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyRefrigeratorActivity", "Server response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    String message = jsonObject.getString("message");

                    if (success) {
                        Log.d("MyRefrigeratorActivity", "Ingredient successfully deleted from the server.");
                        adapter.removeIngredient(position);
                        dbHelper.deleteIngredient(ingredientName); // 데이터베이스에서 제거
                        Log.d("MyRefrigeratorActivity", "Ingredient deleted from the database: " + ingredientName);
                    } else {
                        Log.d("MyRefrigeratorActivity", "Failed to delete ingredient from the server. Message: " + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        IngredientDeleteRequest deleteRequest = new IngredientDeleteRequest(nickname, ingredientName, responseListener);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(deleteRequest);
    }
}

class IngredientDeleteRequest extends StringRequest {
    private static final String DELETE_REQUEST_URL = "http://ys.calab.myds.me/refrigeratorDelete.php";

    public IngredientDeleteRequest(String nickname, String ingredientName, Response.Listener<String> listener) {
        super(Request.Method.DELETE, DELETE_REQUEST_URL + "/" + nickname + "/" + ingredientName, listener, null);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return null; // 요청 본문에 파라미터가 없으므로 null 반환
    }
}
