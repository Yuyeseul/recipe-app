package com.example.recipeproject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RefrigeratorRecipe extends AppCompatActivity {
    private RecyclerView recyclerView; // 재료를 표시할 RecyclerView
    private RefrigeratorAdapter adapter; // RecyclerView의 어댑터
    private ArrayList<IngredientData> ingredientsList; // 재료 리스트
    private static final int REQUEST_CODE_ADD_INGREDIENT = 1; // 재료 추가 요청 코드
    private MainActivity.MyDBHelper dbHelper; // 메인 데이터베이스 헬퍼
    ListView listRecipe; // 레시피 리스트를 표시할 ListView
    ArrayList<RecipeData> refrigeratorRecipeList; // 레시피 리스트
    SQLiteDatabase db; // SQLite 데이터베이스 객체
    ImageButton back; // 뒤로 가기 버튼
    private ArrayList<IngredientData> selectedIngredients; // 선택된 재료 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_refrigerator_recipe);

        // 뷰 요소 초기화
        back = (ImageButton) findViewById(R.id.btnBack);
        listRecipe = (ListView) findViewById(R.id.listRecipe);

        // 뒤로 가기 버튼 클릭 리스너 설정
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dbHelper = new MainActivity.MyDBHelper(this, "recipe", null, 1); // DBHelper 초기화
        db = dbHelper.getReadableDatabase();

        recyclerView = findViewById(R.id.btnIngredient);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(gridLayoutManager);
        ingredientsList = new ArrayList<>();
        adapter = new RefrigeratorAdapter(this, ingredientsList, 1);
        recyclerView.setAdapter(adapter);

        // 닉네임 가져오기
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", "");

        refrigeratorRecipeList = new ArrayList<>(); // 레시피 리스트 초기화
        selectedIngredients = new ArrayList<>(); // 선택된 재료 리스트 초기화

        // 사용자에 대한 재료 불러오기 및 데이터 로드
        loadIngredientsForUser(nickname);
        loadData();

        listRecipe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RecipeData selectedRecipe = (RecipeData) adapterView.getAdapter().getItem(position);
                String recipeName = selectedRecipe.recipeName;

                Intent intent = new Intent(RefrigeratorRecipe.this, RecipePageActivity.class);
                intent.putExtra("recipeName", recipeName);
                startActivity(intent);
            }
        });


        adapter.setOnItemClickListener(new RefrigeratorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                IngredientData ingredient = ingredientsList.get(position);
                ingredient.isSelected = !ingredient.isSelected; // 선택 상태 토글
                adapter.notifyItemChanged(position); // 해당 아이템만 업데이트

                // 선택 상태에 따라 로그 출력
                if (ingredient.isSelected) {
                    selectedIngredients.add(ingredient); // 목록에 추가
                    Log.d("아이템 선택", "Added to selected: " + ingredient.ingredientName);
                } else {
                    selectedIngredients.remove(ingredient); // 목록에서 제거
                    Log.d("아이템 선택", "Removed from selected: " + ingredient.ingredientName);
                }

                loadData(); // 선택된 재료에 따라 레시피 검색
            }
        });

//        // 초기 상태 설정: 재료 목록이 비어 있지 않은 경우 선택된 재료 리스트 초기화
//        if (!ingredientsList.isEmpty()) {
//            selectedIngredients.addAll(ingredientsList); // 선택된 재료 리스트에 추가
//            Log.d("초기화", "Selected ingredients initialized with ingredients list.");
//        }

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
        // 기존 리스트 초기화
        ingredientsList.clear();
        String sql = "SELECT ingredientName FROM refrigerator WHERE nickname = ? "; // 사용자 재료 조회 SQL
        Cursor cursor = null;

        cursor = db.rawQuery(sql, new String[]{nickname});

        Log.d("1", "Number of ingredients loaded: " + cursor.getCount());
        if (cursor.getCount() == 0) {
            Log.d("2", "No ingredients found for user: " + nickname);
        }

        // 재료 목록을 가져와서 리스트에 추가
        while (cursor.moveToNext()) {
            String ingredientName = cursor.getString(0);
            IngredientData data = new IngredientData(ingredientName);
            // 기본 선택 상태 설정
            data.isSelected = true; // 초기 상태는 선택됨
            ingredientsList.add(data);
            // 각 재료 이름을 로그에 출력
            Log.d("3", "Loaded ingredient: " + ingredientName);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        Log.d("4", "Ingredients List Size: " + ingredientsList.size()); // 리스트 크기 로그 출력
        selectedIngredients.addAll(ingredientsList);
    }

    public void loadData() {
//        if (selectedIngredients.isEmpty()) {
//            Log.d("MyRefrigeratorActivity", "Ingredients list is empty. No recipes will be loaded.");
//            return;  // 재료 목록이 비어있으면 레시피 검색을 중지합니다.
//        }
//
//        if (selectedIngredients == null) {
//            selectedIngredients = new ArrayList<>(ingredientsList);
//        }

//        if (selectedIngredients == null) {
//            selectedIngredients = new ArrayList<>(ingredientsList);
//        }
//
//        if (selectedIngredients.isEmpty()) {
//            Log.d("5", "Ingredients list is empty. No recipes will be loaded.");
//            return;  // 재료 목록이 비어있으면 레시피 검색을 중지합니다.
//        }
        selectedIngredients.clear(); // 선택된 재료 리스트 초기화
        for (IngredientData ingredient : ingredientsList) {
            if (ingredient.isSelected) {
                selectedIngredients.add(ingredient); // 선택된 재료만 추가
            }
        }

        // 초기 상태 설정: 재료 목록이 비어 있지 않은 경우 선택된 재료 리스트 초기화
        if (ingredientsList.isEmpty()) {
//            selectedIngredients.addAll(ingredientsList); // 선택된 재료 리스트에 추가
            Log.d("초기화", "Selected ingredients initialized with ingredients list.");
            return;
        }
// 선택된 재료의 이름을 배열로 변환
        String[] queries = new String[selectedIngredients.size()];
        for (int i = 0; i < selectedIngredients.size(); i++) {
            queries[i] = selectedIngredients.get(i).ingredientName;
        }

        searchRecipe(queries);
    }

    public void searchRecipe(String[] queries) {
        refrigeratorRecipeList.clear();
        db = dbHelper.getReadableDatabase();

        // SQL 쿼리 작성: 각 검색어에 대해 LIKE 조건을 계산하고 일치하는 개수를 기반으로 정렬
        StringBuilder sqlBuilder = new StringBuilder("SELECT *, ");
        sqlBuilder.append("(");
        for (int i = 0; i < queries.length; i++) {
            sqlBuilder.append("CASE WHEN recipeIngredients LIKE '%").append(queries[i]).append("%' THEN 1 ELSE 0 END");
            if (i < queries.length - 1) {
                sqlBuilder.append(" + ");
            }
        }
        sqlBuilder.append(") AS matchCount, ");
        // 조회수와 좋아요 수를 기반으로 정렬하는 표현식 추가
        sqlBuilder.append("((count + (2 * favorite)) / 2.0) AS sortScore "); // 정렬 기준 추가
        sqlBuilder.append("FROM recipe ");
        sqlBuilder.append("WHERE (");
        for (int i = 0; i < queries.length; i++) {
            sqlBuilder.append("recipeIngredients LIKE '%").append(queries[i]).append("%'");
            if (i < queries.length - 1) {
                sqlBuilder.append(" OR ");
            }
        }
        sqlBuilder.append(") ");
        sqlBuilder.append("ORDER BY matchCount DESC, sortScore DESC ");

        String sql = sqlBuilder.toString();
        Log.d("MyRefrigeratorActivity", "SQL Query: " + sql);

        Cursor cursor = db.rawQuery(sql, null);
        Log.d("9", "Cursor Count: " + cursor.getCount()); // Cursor의 개수 로그 출력
        while (cursor.moveToNext()) {
            RecipeData recipeData = new RecipeData();
            String recipeName = cursor.getString(1).trim();  // 레시피 이름 가져오기
            String recipeIngredients = cursor.getString(4);
            recipeData.recipeName = recipeName;
            // 일치하는 재료 계산
            for (String query : queries) {
                if (recipeIngredients.contains(query)) {
                    recipeData.ingredients.add(query); // 일치하는 재료 추가
                }
            }
//            Log.d("MatchingIngredients", "Final matching ingredients: " + matchingIngredients.toString());

            refrigeratorRecipeList.add(recipeData);
//            Log.d("10", "Recipe found: " + recipeName + ", Ingredients: " + recipeData.ingredients.toString());
        }
        cursor.close();
        db.close();

        ListAdapter listAdapter = new ListAdapter(refrigeratorRecipeList, 1);
        listRecipe.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
//        Log.d("11", "Recipe list updated with " + refrigeratorRecipeList.size() + " recipes.");
    }

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

}