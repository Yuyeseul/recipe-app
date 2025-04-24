package com.example.recipeproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;//툴바
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchActivity extends AppCompatActivity {

    ImageButton back;
    ListView searchList;
    SearchView searchView;
    MainActivity.MyDBHelper myDBHelper;
    ArrayList<RecipeData> recipeDataList;
    ArrayList<RecipeData> filteredDataList; //필터링 데이터
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.tSearchActivity);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("");

        back = (ImageButton) findViewById(R.id.btnBack);
        searchList = (ListView) findViewById(R.id.searchListView);
        searchView = (SearchView) findViewById(R.id.search);

        // 필터링된 데이터 리스트 초기화
        filteredDataList = new ArrayList<>();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        loadData(); // 데이터 로드 메서드 호출

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText); // 검색어에 따라 데이터 필터링
                return true;
            }
        });

        // ListView 클릭 리스너 설정
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RecipeData selectedRecipe = (RecipeData) adapterView.getAdapter().getItem(position);
                String recipeName = selectedRecipe.recipeName;

                Intent intent = new Intent(SearchActivity.this, RecipePageActivity.class);
                intent.putExtra("recipeName", recipeName);
                startActivity(intent);
            }
        });

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    private void loadData() {
        recipeDataList = new ArrayList<>();
        filteredDataList = new ArrayList<>(); // 필터링된 데이터 리스트 초기화
        myDBHelper = new MainActivity.MyDBHelper(this, "recipe", null, 1);
        sqlDB = myDBHelper.getReadableDatabase();

        String sql = "SELECT * FROM recipe";
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            RecipeData recipeData = new RecipeData();
            String recipeName = cursor.getString(1);
            recipeData.recipeName = recipeName;
            recipeDataList.add(recipeData);
        }
        cursor.close();
        sqlDB.close();

        // 가나다 순으로 정렬
        Collections.sort(recipeDataList, new Comparator<RecipeData>() {
            @Override
            public int compare(RecipeData r1, RecipeData r2) {
                return r1.recipeName.compareTo(r2.recipeName);
            }
        });

        // 필터링된 데이터 리스트에 원본 데이터 복사
        filteredDataList.addAll(recipeDataList);

        ListAdapter listAdapter = new ListAdapter(filteredDataList, 0); // 필터링된 리스트로 어댑터 설정
        searchList.setAdapter(listAdapter);
    }


    private void filterData(String searchText) {
        filteredDataList.clear(); // 필터링된 데이터 리스트 초기화
        if (searchText.isEmpty()) {
            filteredDataList.addAll(recipeDataList); // 검색어가 없으면 전체 데이터 표시
        } else {
            for (RecipeData recipe : recipeDataList) {
                if (recipe.recipeName.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredDataList.add(recipe); // 검색어가 포함된 레시피 추가
                }
            }
        }

        // 어댑터 데이터 갱신
        ListAdapter adapter = (ListAdapter) searchList.getAdapter();
        adapter.notifyDataSetChanged();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
////        getMenuInflater().inflate(R.menu.toolbar2_menu, menu);
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//        int itemId = item.getItemId();
//
//        if(itemId == R.id.btnBack){
//            finish();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}