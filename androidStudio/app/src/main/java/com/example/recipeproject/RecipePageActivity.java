package com.example.recipeproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class RecipePageActivity extends AppCompatActivity {

    TextView pName, pCategory, pIngredients, pContent, pRecipeNickname, pDay, textFavorite;
    TextView pageCount;
    ImageView pImage;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myDBHelper;
    String recipeName = "";
    String recipeImage = "";
    String recipeCategory = "";
    String recipeIngredients = "";
    String recipeContent = "";
    String recipeNickname = "";
    String recipeDay = "";
    int favoriteCount = 0; // favorite 카운트를 위한 변수 추가
    ImageButton bookmark, back, btnFavorite;
    // 체크 상태 변수
    private boolean isChecked = false; // 초기 체크 상태는 false
    private boolean isFavoriteChecked = false; // 좋아요 체크 상태
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipepage);

        pName = (TextView) findViewById(R.id.pRecipeName);
        pImage = (ImageView) findViewById(R.id.pRecipeImage);
        pCategory = (TextView) findViewById(R.id.pRecipeCategory);
        pIngredients = (TextView) findViewById(R.id.pRecipeIngredients);
        pContent = (TextView) findViewById(R.id.pRecipeContent);
        pRecipeNickname = (TextView) findViewById(R.id.pRecipeNickname);
        pDay = (TextView) findViewById(R.id.pDay);
        textFavorite = (TextView) findViewById(R.id.textFavorite);
        bookmark = (ImageButton) findViewById(R.id.bookmark);
        btnFavorite = (ImageButton) findViewById(R.id.btnFavorite);
        pageCount = findViewById(R.id.pageCount); // TextView 가져오기
        back = (ImageButton) findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recipeName = getIntent().getStringExtra("recipeName");

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", "");  // 저장된 아이디 불러오기

        myDBHelper = new MainActivity.MyDBHelper(getApplicationContext(), "recipe", null, 1);

        loadRecipeDetails(recipeName);
        // 페이지 진입 시 조회수 증가
        increaseViewCount(recipeName);

        sqlDB = myDBHelper.getReadableDatabase();

        String sql = "SELECT * FROM recipe WHERE recipeName='"+recipeName+"'";
        Log.d("SQL",sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);

        while(cursor.moveToNext()){
            recipeName = cursor.getString(1);
            recipeImage = cursor.getString(2);
            recipeCategory = cursor.getString(3);
            recipeIngredients = cursor.getString(4);
            recipeContent = cursor.getString(5);
            recipeDay = cursor.getString(6);
            recipeNickname = cursor.getString(7);
            favoriteCount = cursor.getInt(8);
        }
        Log.d("RecipeImageURL", recipeImage);

        cursor.close();
        sqlDB.close();

        // 이미지 URL 수정
        if (recipeImage.startsWith("/volume1/web/ys/image/")) {
            recipeImage = recipeImage.replace("/volume1/web/ys/image/", "https://ys.calab.myds.me/image/");
        }
        pName.setText(recipeName);
//        pImage.setImageURI(Uri.parse(recipeImage));
        Glide.with(this).load(recipeImage).into(pImage);
        pCategory.setText(recipeCategory);
        pIngredients.setText(recipeIngredients);
        pContent.setText(recipeContent);
        pRecipeNickname.setText(recipeNickname);
        pDay.setText(recipeDay);
        textFavorite.setText(String.valueOf(favoriteCount)); // favorite 값 표시


        // 북마크 상태 확인
        checkBookmarkStatus(nickname, recipeName);

        // 좋아요 상태 확인
        checkFavoriteStatus(nickname, recipeName);


        // 이미지 버튼 클릭 리스너 설정
        bookmark.setOnClickListener(v -> {
            // 현재 체크 상태에 따라 동작
            if (!isChecked) {
                // 북마크가 없을 때, 북마크 저장
                saveBookmark(nickname, recipeName);
                bookmark.setImageResource(R.drawable.bookmark_check); // 체크 상태 이미지로 변경
                isChecked = true; // 체크 상태 업데이트
            } else {
                // 북마크가 있을 때, 북마크 삭제
                removeBookmark(nickname, recipeName);
                bookmark.setImageResource(R.drawable.baseline_bookmark_border_24); // 기본 이미지로 변경
                isChecked = false; // 체크 상태 업데이트
            }
        });

        // 좋아요 버튼 클릭 리스너
        btnFavorite.setOnClickListener(v -> {
            if (!isFavoriteChecked) {
                favoriteCount++; // 좋아요 증가
                updateFavoriteCount(recipeName, favoriteCount);
                btnFavorite.setImageResource(R.drawable.baseline_favorite_24); // 좋아요 체크 이미지로 변경
                isFavoriteChecked = true;

                saveFavorite(nickname, recipeName); // 로컬 DB에 저장
                // 좋아요 추가 요청
                FavoriteRequest favoriteRequest = new FavoriteRequest(nickname, recipeName, response -> {
                    Log.d("Favorite", "좋아요가 서버에 저장되었습니다.");
                });
                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(favoriteRequest);
            } else {
                favoriteCount--; // 좋아요 감소
                updateFavoriteCount(recipeName, favoriteCount);
                btnFavorite.setImageResource(R.drawable.outline_favorite_border_24); // 기본 이미지로 변경
                isFavoriteChecked = false;

                removeFavorite(nickname, recipeName); // 로컬 DB에서 삭제
                // 좋아요 삭제 요청
                FavoriteDeleteRequest favoriteDeleteRequest = new FavoriteDeleteRequest(nickname, recipeName, response -> {
                    Log.d("Favorite", "좋아요가 서버에서 삭제되었습니다.");
                });
                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(favoriteDeleteRequest);
            }
            textFavorite.setText(String.valueOf(favoriteCount)); // 업데이트된 favorite 값 표시
        });

    }

    // 초기 조회수 설정 메서드
    private void loadRecipeDetails(String recipeName) {
        SQLiteDatabase db = myDBHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT count FROM recipe WHERE recipeName=?", new String[]{recipeName});
        if (cursor.moveToFirst()) {
            int currentCount = cursor.getInt(0);
            pageCount.setText("조회수 : " + currentCount);
        }

        cursor.close();
        db.close();
    }

    // 조회수 증가 메서드
    private void increaseViewCount(String recipeName) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        // 조회수 증가
        Cursor cursor = db.rawQuery("SELECT count FROM recipe WHERE recipeName=?", new String[]{recipeName});
        if (cursor.moveToFirst()) {
            int currentCount = cursor.getInt(0);
            int newCount = currentCount + 1;

            ContentValues values = new ContentValues();
            values.put("count", newCount);
            db.update("recipe", values, "recipeName=?", new String[]{recipeName});

            // TextView에 조회수 반영
            pageCount.setText("조회수 : " + newCount);

            // 서버에 조회수 업데이트 요청
            ViewCountUpdateRequest viewCountRequest = new ViewCountUpdateRequest(recipeName, newCount, response -> {
                Log.d("ViewCount", "View count updated on server.");
            });
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(viewCountRequest);
        }

        cursor.close();
        db.close();
    }

    private void updateFavoriteCount(String recipeName, int newFavoriteCount) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("favorite", newFavoriteCount);
        db.update("recipe", values, "recipeName=?", new String[]{recipeName});
        db.close();

        FavoriteUpdateRequest favoriteRequest = new FavoriteUpdateRequest(recipeName, newFavoriteCount, response -> {
            Log.d("Favorite", "Favorite count updated on server.");
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(favoriteRequest);
    }

    private void checkBookmarkStatus(String nickname, String recipeName) {
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM bookmark WHERE nickname=? AND recipeName=?";
        Cursor cursor = db.rawQuery(sql, new String[]{nickname, recipeName});
        if (cursor.getCount() > 0) {
            // 북마크가 존재하면 이미지 버튼을 체크 상태 이미지로 설정
            bookmark.setImageResource(R.drawable.bookmark_check);
            isChecked = true; // 체크 상태 업데이트
        } else {
            // 북마크가 없으면 기본 이미지로 설정
            bookmark.setImageResource(R.drawable.baseline_bookmark_border_24);
            isChecked = false; // 체크 상태 업데이트
        }
        cursor.close();
        db.close();
    }

    private void checkFavoriteStatus(String nickname, String recipeName) {
        // 좋아요 상태 확인
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM favorite WHERE nickname=? AND recipeName=?";
        Cursor cursor = db.rawQuery(sql, new String[]{nickname, recipeName});
        if (cursor.getCount() > 0) {
            btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
            isFavoriteChecked = true;
        } else {
            btnFavorite.setImageResource(R.drawable.outline_favorite_border_24);
            isFavoriteChecked = false;
        }
        cursor.close();
        db.close();
    }

    private void saveBookmark(String nickname, String recipeName) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("recipeName", recipeName);
        long newRowId = db.insert("bookmark", null, values);
        if (newRowId != -1) {
            Log.d("Bookmark", "북마크가 저장되었습니다.");
        }
        db.close();

        BookmarkRequest bookmarkRequest = new BookmarkRequest(nickname, recipeName, response -> {
            // 서버로부터의 응답 처리
            Log.d("Bookmark", "북마크가 서버에 저장되었습니다.");
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(bookmarkRequest);
    }

    private void removeBookmark(String nickname, String recipeName) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        int deletedRows = db.delete("bookmark", "nickname=? AND recipeName=?", new String[]{nickname, recipeName});
        if (deletedRows > 0) {
            Log.d("Bookmark", "북마크가 삭제되었습니다.");
        }
        db.close();

        BookmarkDeleteRequest bookmarkDeleteRequest = new BookmarkDeleteRequest(nickname, recipeName, response -> {
            // 서버로부터의 응답 처리
            Log.d("Bookmark", "서버 응답: " + response);
            Log.d("Bookmark", "북마크가 서버에서 삭제되었습니다.");
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(bookmarkDeleteRequest);
    }

    // 좋아요 저장 메서드
    private void saveFavorite(String nickname, String recipeName) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("recipeName", recipeName);
        long newRowId = db.insert("favorite", null, values); // "favorite" 테이블에 저장
        if (newRowId != -1) {
            Log.d("Favorite", "좋아요가 로컬 DB에 저장되었습니다.");
        }
        db.close();
    }

    // 좋아요 삭제 메서드
    private void removeFavorite(String nickname, String recipeName) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        int deletedRows = db.delete("favorite", "nickname=? AND recipeName=?", new String[]{nickname, recipeName});
        if (deletedRows > 0) {
            Log.d("Favorite", "좋아요가 로컬 DB에서 삭제되었습니다.");
        }
        db.close();
    }
}

class BookmarkRequest  extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/bookmarkInsert.php";
    private Map<String, String> map;

    public BookmarkRequest (String nickname, String recipeName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        // 요청에 포함할 파라미터들
        map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("recipeName", recipeName);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

class BookmarkDeleteRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/bookmarkDelete.php";
    private Map<String, String> map;

    public BookmarkDeleteRequest(String nickname, String recipeName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        // 요청에 포함할 파라미터들
        map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("recipeName", recipeName);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

class FavoriteUpdateRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/favoriteUpdate.php";
    private final Map<String, String> map;

    public FavoriteUpdateRequest(String recipeName, int favoriteCount, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        map = new HashMap<>();
        map.put("recipeName", recipeName);
        map.put("favorite", String.valueOf(favoriteCount));
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

class FavoriteRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/favoriteInsert.php";
    private final Map<String, String> map;

    public FavoriteRequest(String nickname, String recipeName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("recipeName", recipeName);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

class FavoriteDeleteRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/favoriteDelete.php";
    private final Map<String, String> map;

    public FavoriteDeleteRequest(String nickname, String recipeName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("recipeName", recipeName);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

class ViewCountUpdateRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/updateViewCount.php";
    private final Map<String, String> map;

    public ViewCountUpdateRequest(String recipeName, int viewCount, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        map = new HashMap<>();
        map.put("recipeName", recipeName);
        map.put("viewCount", String.valueOf(viewCount));
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
