package com.example.recipeproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class ScrapListActivity extends AppCompatActivity {
    ImageButton back;
    RecyclerView ingredientList;
    ScrapListAdapter scrapListAdapter;
    List<RecipeData> scrapRecipes = new ArrayList<>(); // 북마크된 레시피 목록
    MainActivity.MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scraplist);

        back = (ImageButton) findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", "");  // 저장된 아이디 불러오기

        // 리사이클러뷰 초기화
        ingredientList = findViewById(R.id.ingredientList);
        ingredientList.setLayoutManager(new LinearLayoutManager(this));

        // DB에서 북마크된 레시피 가져오기
        dbHelper = new MainActivity.MyDBHelper(this, "recipe", null, 1);
        scrapRecipes = dbHelper.getBookmarks(nickname);

        // 어댑터 설정
        scrapListAdapter = new ScrapListAdapter(scrapRecipes, dbHelper, nickname);
        ingredientList.setAdapter(scrapListAdapter);
    }

}

class ScrapListAdapter extends RecyclerView.Adapter<ScrapListAdapter.ViewHolder> {

    private List<RecipeData> scrapRecipes;
    private MainActivity.MyDBHelper dbHelper;
    private String nickname;

    public ScrapListAdapter(List<RecipeData> scrapRecipes, MainActivity.MyDBHelper dbHelper, String nickname) {
        this.scrapRecipes = scrapRecipes;
        this.dbHelper = dbHelper;
        this.nickname = nickname;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scrap_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeData recipe = scrapRecipes.get(position);
        holder.scrapName.setText(recipe.getRecipeName());

        // 북마크 이미지 초기화
        holder.scrapMark.setImageResource(R.drawable.bookmark_check); // 기본 이미지 설정
        holder.scrapMark.setSelected(true); // 기본적으로 체크 상태로 설정

        // 북마크 이미지 버튼 클릭 시 북마크 해제
        holder.scrapMark.setOnClickListener(view -> {
            // 서버와 로컬 DB에서 북마크 삭제
            dbHelper.deleteBookmark(nickname, recipe.getRecipeName());
            deleteBookmarkFromServer(nickname, recipe.getRecipeName(), holder.itemView.getContext());

            // 목록에서 제거 후 UI 업데이트
            scrapRecipes.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return scrapRecipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView scrapName;
        ImageButton scrapMark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            scrapName = itemView.findViewById(R.id.scrapName);
            scrapMark = itemView.findViewById(R.id.scrapMark);
        }
    }

    private void deleteBookmarkFromServer(String nickname, String recipeName, Context context) {
        BookmarkDeleteRequest deleteRequest = new BookmarkDeleteRequest(nickname, recipeName, response -> {
            Log.d("Bookmark", "서버 응답: " + response);
        });
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(deleteRequest);
    }
}