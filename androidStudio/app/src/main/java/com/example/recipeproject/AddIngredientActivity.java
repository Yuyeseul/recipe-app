package com.example.recipeproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class AddIngredientActivity extends AppCompatActivity {

    EditText editTextSearch;
    Button buttonSearch, add;
    ListView resultList;
    IngredientAdapter adapter;
    ArrayList<IngredientData> searchResults;
    ArrayList<IngredientData> allIngredients;
    MainActivity.MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    IngredientData selectedIngredient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_search);

        editTextSearch = findViewById(R.id.editSearch);
        add = findViewById(R.id.add);
        resultList = findViewById(R.id.searchList);

        // DBHelper 초기화
        myDBHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);

        // 모든 재료 가져오기
        allIngredients = getAllIngredients();
        searchResults = new ArrayList<>(allIngredients);

        Log.d("DEBUG", "Ingredients size: " + searchResults.size());


        // 어댑터 설정
        adapter = new IngredientAdapter(this, searchResults);
        resultList.setAdapter(adapter);

        // 데이터가 리스트뷰에 들어가는지 로그로 확인
//        if (searchResults.size() > 0) {
//            for (IngredientData ingredient : searchResults) {
//                Log.d("DEBUG", "Ingredient: " + ingredient.ingredientName);
//            }
//        } else {
//            Log.d("DEBUG", "No ingredients found.");
//        }

        // ListView 아이템 클릭 시 선택된 재료 저장
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIngredient = searchResults.get(position); // 선택된 재료 저장
                Log.d("DEBUG", "Selected Ingredient: " + selectedIngredient.ingredientName);
                adapter.toggleSelection(position);
            }
        });

//        // 검색 버튼 클릭 리스너
//        buttonSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String query = editTextSearch.getText().toString().trim();
//                if (!query.isEmpty()) {
//                    performSearch(query);
//                }
//            }
//        });

        // 추가 버튼 클릭 리스너
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedIngredient != null) {

                    Log.d("AddIngredientActivity", "Selected Ingredient: " + selectedIngredient.ingredientName);

                    //        다른 액티비티에서 아이디 사용
                    SharedPreferences sharedPref = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
                    String nickname = sharedPref.getString("nickname", "");  // 저장된 아이디 불러오기

                    String ingredientName = selectedIngredient.ingredientName;

                    // 선택된 재료를 DB에 추가
                    myDBHelper.addIngredient(nickname, ingredientName);
                    Log.d("AddIngredientActivity", "Added ingredient: " + selectedIngredient.ingredientName);

                    // 추가 후 리스트를 갱신
                    allIngredients = getAllIngredients();
                    filterData(editTextSearch.getText().toString());


                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("AddIngredientActivity", "Server response: " + response); // 서버 응답 로그 추가

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean success = jsonObject.getBoolean("success");

                                if (success) {
                                    Log.d("AddIngredientActivity", "Ingredient successfully added to the server.");
                                    Toast.makeText(getApplicationContext(), "재료가 냉장고에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("AddIngredientActivity", "Failed to add ingredient to the server.");
                                    Toast.makeText(getApplicationContext(), "서버에 재료를 추가하지 못했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AddIngredientActivity", "JSON 파싱 오류: " + e.getMessage());
                            }
                        }
                    };

                    // IngredientRequest 객체 생성
                    SaveRequest saveRequest = new SaveRequest(nickname, ingredientName, responseListener);

                    // 요청 큐에 추가
                    RequestQueue queue = Volley.newRequestQueue(AddIngredientActivity.this);
                    queue.add(saveRequest);


                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedIngredient", selectedIngredient.ingredientName);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();

                } else {
                    Log.d("AddIngredientActivity", "No ingredient selected to add.");
                }
            }
        });

        // 텍스트 변경 리스너 설정
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 데이터베이스에서 모든 재료를 가져오는 메서드
    private ArrayList<IngredientData> getAllIngredients() {
        ArrayList<IngredientData> ingredientsList = new ArrayList<>();
        Cursor cursor = myDBHelper.getReadableDatabase().rawQuery("SELECT * FROM ingredients", null);
        while (cursor.moveToNext()) {
            IngredientData ingredientData = new IngredientData();
            String ingredientName = cursor.getString(1);
            ingredientData.ingredientName = ingredientName;
            ingredientsList.add(ingredientData);
        }
        cursor.close();
        return ingredientsList;
    }

    // 검색어에 따라 재료 필터링
    private void filterData(String searchText) {
        searchResults.clear();
        if (searchText.isEmpty()) {
            searchResults.addAll(allIngredients);
        } else {
            for (IngredientData ingredient : allIngredients) {
                if (ingredient.ingredientName.toLowerCase().contains(searchText.toLowerCase())) {
                    searchResults.add(ingredient);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // 검색 수행
    private void performSearch(String query) {
        ArrayList<IngredientData> results = myDBHelper.searchIngredients(query);
        searchResults.clear();
        searchResults.addAll(results);
        adapter.notifyDataSetChanged();
    }
}

class SaveRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/save.php";
    private Map<String, String> map;

    public SaveRequest(String nickname, String ingredientName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        // 요청에 포함할 파라미터들
        map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("ingredientName", ingredientName);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
