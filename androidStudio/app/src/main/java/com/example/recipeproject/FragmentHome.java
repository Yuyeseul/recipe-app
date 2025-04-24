package com.example.recipeproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {
    Chip search;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private MainActivity.MyDBHelper dbHelper;
    SQLiteDatabase sqlDB;
    Button[] btn = new Button[10];
    Integer[] btn_id = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        search = (Chip) view.findViewById(R.id.chip);
        recyclerView = (RecyclerView) view.findViewById(R.id.recipeView);
        for(int i=0;i<8;i++){
            btn[i] = (Button) view.findViewById(btn_id[i]);
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", "");  // 저장된 아이디 불러오기

        // 레시피 가져오기 및 RecyclerView 설정
        List<RecipeData> recipeDataList = fetchRandomRecipes();
        recipeAdapter = new RecipeAdapter(recipeDataList, new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecipeData selectedRecipe) {
                Intent intent = new Intent(getActivity(), RecipePageActivity.class);
                intent.putExtra("recipeName", selectedRecipe.getRecipeName());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(recipeAdapter);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        for (int i = 0; i < 8; i++) {
            final int index = i;
            btn[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String category = btn[index].getText().toString();
                    recipeDataList.clear();
                    recipeDataList.addAll(fetchRandomCategoryRecipes(category)); // 카테고리에 해당하는 레시피를 추가
                    recipeAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 레시피 가져오기 및 RecyclerView 갱신
        List<RecipeData> recipeDataList = fetchRandomRecipes();
        recipeAdapter.updateRecipes(recipeDataList); // 어댑터에 새로운 데이터 전달
    }

    private List<RecipeData> fetchRandomRecipes() {
        List<RecipeData> recipeList = new ArrayList<>();
        dbHelper = new MainActivity.MyDBHelper(getActivity(), "recipe", null, 1);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM recipe ORDER BY RANDOM() LIMIT 10", null);

        while (cursor.moveToNext()) {
            RecipeData recipeData = new RecipeData();
            recipeData.setRecipeName(cursor.getString(1)); // Assuming recipe name is in column 1
            recipeData.setRecipeImage(cursor.getString(2)); // Assuming image URL is in column 2
            recipeList.add(recipeData);
        }
        cursor.close();
        return recipeList;
    }

    private List<RecipeData> fetchRandomCategoryRecipes(String category) {
        List<RecipeData> recipeList = new ArrayList<>();
        dbHelper = new MainActivity.MyDBHelper(getActivity(), "recipe", null, 1);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM recipe WHERE recipeCategory LIKE ? ORDER BY RANDOM() LIMIT 10",
                new String[]{"%" + category + "%"} // 카테고리에 맞는 레시피를 랜덤으로 가져오기
        );

        while (cursor.moveToNext()) {
            RecipeData recipeData = new RecipeData();
            recipeData.setRecipeName(cursor.getString(1)); // Assuming recipe name is in column 1
            recipeData.setRecipeImage(cursor.getString(2)); // Assuming image URL is in column 2
            recipeList.add(recipeData);
        }
        cursor.close();
        return recipeList;
    }

}

class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private List<RecipeData> recipes;
    private OnItemClickListener onItemClickListener;

    public RecipeAdapter(List<RecipeData> recipes, OnItemClickListener onItemClickListener) {
        this.recipes = recipes;
        this.onItemClickListener = onItemClickListener; // 클릭 리스너 초기화
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeData recipe = recipes.get(position);
        holder.recipeName.setText(recipe.getRecipeName());

        // 이미지 로드 (Glide 라이브러리 사용)
        Glide.with(holder.itemView.getContext())
                .load(recipe.getRecipeImage())
                .into(holder.recipeImage);

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public interface OnItemClickListener {
        void onItemClick(RecipeData recipe);
    }

    public void updateRecipes(List<RecipeData> newRecipes) {
        this.recipes.clear(); // 기존 데이터 삭제
        this.recipes.addAll(newRecipes); // 새 데이터 추가
        notifyDataSetChanged(); // 데이터 변경 알림
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName;
        ImageView recipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeImage = itemView.findViewById(R.id.recipeImage);
        }
    }
}