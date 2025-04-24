package com.example.recipeproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recipeproject.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    FragmentHome fragmentHome;
    FragmentRecipe fragmentRecipe;
    FragmentRecommend fragmentRecommend;
    FragmentMypage fragmentMypage;
    Handler handler = new Handler();
    SQLiteDatabase sqlDB;
    MyDBHelper myDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fragmentHome = new FragmentHome();
        fragmentRecipe = new FragmentRecipe();
        fragmentRecommend = new FragmentRecommend();
        fragmentMypage = new FragmentMypage();

//        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
//        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);

        getSupportFragmentManager().beginTransaction().replace(R.id.viewLayout,fragmentHome).commit();

        NavigationBarView navigationBarView = findViewById(R.id.menu_navi);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.navi_home){
                    getSupportFragmentManager().beginTransaction().replace(R.id.viewLayout,fragmentHome).commit();
                    return true;
                }
                else if (itemId == R.id.navi_recipe){
                    getSupportFragmentManager().beginTransaction().replace(R.id.viewLayout,fragmentRecipe).commit();
                    return true;
                }
                else if(itemId == R.id.navi_recommend){
                    getSupportFragmentManager().beginTransaction().replace(R.id.viewLayout,fragmentRecommend).commit();
                    return true;
                }
                else if(itemId == R.id.navi_myPage){
                    getSupportFragmentManager().beginTransaction().replace(R.id.viewLayout,fragmentMypage).commit();
                    return true;
                }

                return false;
            }
        });

        myDBHelper = new MyDBHelper(this, "recipe", null, 1);
        sqlDB = myDBHelper.getWritableDatabase();
        myDBHelper.onUpgrade(sqlDB, 1, 2);
        sqlDB.close();

        final String userUrl = "https://ys.calab.myds.me/userJson.php";
        final String recipeUrl = "https://ys.calab.myds.me/recipeJson.php";
        final String ingredientsUrl = "https://ys.calab.myds.me/ingredientsJson.php";
        final String refrigeratorUrl = "https://ys.calab.myds.me/refrigeratorJson.php";
        final String shoppingUrl = "https://ys.calab.myds.me/shoppingJson.php";
        final String bookmarkUrl = "https://ys.calab.myds.me/bookmarkJson.php";
        final String favoriteUrl = "https://ys.calab.myds.me/favoriteJson.php";

        new Thread(new Runnable() {
            @Override
            public void run() {
                requestUrl(userUrl);
                requestUrl(recipeUrl);
                requestUrl(ingredientsUrl);
                requestUrl(refrigeratorUrl);
                requestUrl(shoppingUrl);
                requestUrl(bookmarkUrl);
                requestUrl(favoriteUrl);
            }
        }).start();

    }

    public static class MyDBHelper extends SQLiteOpenHelper{

        public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS user (id VARCHAR(30) PRIMARY KEY, " +
                    "pw VARCHAR(30), name VARCHAR(30), nickname VARCHAR(30), birth VARCHAR(30), phone VARCHAR(30));");
            Log.d("MyDBHelper", "user table created");
            db.execSQL("CREATE TABLE IF NOT EXISTS recipe (no INTEGER PRIMARY KEY AUTOINCREMENT, recipeName TEXT, recipeImage TEXT, " +
                    "recipeCategory TEXT, recipeIngredients TEXT, recipeContent TEXT, dayTime DATETIME, nickname VARCHAR(30), favorite INTEGER, count INTEGER);");
            Log.d("MyDBHelper", "recipe table created");
            db.execSQL("CREATE TABLE IF NOT EXISTS ingredients (no INTEGER PRIMARY KEY, ingredientName VARCHAR(20));");
            Log.d("MyDBHelper", "ingredients table created");
            db.execSQL("CREATE TABLE IF NOT EXISTS refrigerator (" +
                    "nickname VARCHAR(30) NOT NULL, " +
                    "ingredientName VARCHAR(20) NOT NULL, " +
                    "PRIMARY KEY (ingredientName, nickname));");
            Log.d("MyDBHelper", "refrigerator table created");
            db.execSQL("CREATE TABLE IF NOT EXISTS shopping (" +
                    "nickname VARCHAR(30) NOT NULL, " +
                    "shoppingName VARCHAR(30) NOT NULL, " +
                    "PRIMARY KEY (nickname, shoppingName));");
            Log.d("MyDBHelper", "shopping table created");
            db.execSQL("CREATE TABLE IF NOT EXISTS bookmark (" +
                    "nickname VARCHAR(30) NOT NULL, " +
                    "recipeName TEXT NOT NULL, " +
                    "PRIMARY KEY (nickname, recipeName));");
            Log.d("MyDBHelper", "bookmark table created");
            db.execSQL("CREATE TABLE IF NOT EXISTS favorite (" +
                    "nickname VARCHAR(30) NOT NULL, " +
                    "recipeName TEXT NOT NULL, " +
                    "PRIMARY KEY (nickname, recipeName));");
            Log.d("MyDBHelper", "favorite table created");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS user");
            db.execSQL("DROP TABLE IF EXISTS recipe");
            db.execSQL("DROP TABLE IF EXISTS ingredients");
            db.execSQL("DROP TABLE IF EXISTS refrigerator");
            db.execSQL("DROP TABLE IF EXISTS shopping");
            db.execSQL("DROP TABLE IF EXISTS bookmark");
            db.execSQL("DROP TABLE IF EXISTS favorite");
            onCreate(db);
        }

//        public ArrayList<IngredientData> getIngredientsByNickname(String nickname) {
//            ArrayList<IngredientData> ingredientList = new ArrayList<>();
//            SQLiteDatabase db = this.getReadableDatabase();
//
//            // SQL 쿼리 작성
//            String sql = "SELECT ingredientName FROM refrigerator WHERE nickname = ?";
//            Cursor cursor = db.rawQuery(sql, new String[]{nickname});
//
//            while (cursor.moveToNext()) {
//                IngredientData ingredientData = new IngredientData();
//                ingredientData.ingredientName = cursor.getString(0); // 첫 번째 컬럼 (ingredientName)
//                ingredientList.add(ingredientData);
//            }
//
//            cursor.close(); // 커서 닫기
//            return ingredientList; // 검색 결과 리스트 반환
//        }

        public boolean checkUser(String id, String phone) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM user WHERE id = ? AND phone = ?", new String[]{id, phone});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        }

        // ID 중복 확인 메서드
        public boolean isIDDuplicate(String id) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM user WHERE id = ?", new String[]{id});
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count > 0;
        }

        public String getNicknameById(String userId) {
            String nickname = null;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT nickname FROM user WHERE id = ?", new String[]{userId});

            if (cursor.moveToFirst()) {
                nickname = cursor.getString(0);
            }
            cursor.close();
            db.close();
            return nickname;
        }

        // 닉네임 업데이트 메서드
        public void updateNickname(String id, String oldNickname, String newNickname) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nickname", newNickname);
            // user 테이블에서 닉네임 업데이트 (아이디와 이전 닉네임이 일치할 때)
            db.update("user", values, "id = ? AND nickname = ?", new String[]{id, oldNickname});

            // 다른 테이블에서도 닉네임 업데이트 (이전 닉네임이 일치할 때)
            db.update("bookmark", values, "nickname = ?", new String[]{oldNickname});
            db.update("favorite", values, "nickname = ?", new String[]{oldNickname});
            db.update("recipe", values, "nickname = ?", new String[]{oldNickname});
            db.update("refrigerator", values, "nickname = ?", new String[]{oldNickname});
            db.update("shopping", values, "nickname = ?", new String[]{oldNickname});
            db.close(); // 데이터베이스 닫기
        }

        public boolean isNicknameAvailable(String nickname) {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = this.getReadableDatabase();
                cursor = db.query("user", new String[]{"nickname"}, "nickname = ?", new String[]{nickname}, null, null, null);
                boolean available = cursor.getCount() == 0; // 결과가 없으면 사용 가능
                return available;
            } catch (Exception e) {
                e.printStackTrace(); // 예외 로그
                return false; // 예외 발생 시 사용 불가로 처리
            } finally {
                if (cursor != null) {
                    cursor.close(); // 커서 닫기
                }
                if (db != null) {
                    db.close(); // 데이터베이스 닫기
                }
            }
        }

        public void deleteUser(String nickname) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("user", "nickname = ?", new String[]{nickname});
            db.close();
        }


        public ArrayList<IngredientData> getIngredientsByNickname(String nickname) {
            ArrayList<IngredientData> ingredientList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            // SQL 쿼리 작성
            String sql = "SELECT ingredientName FROM refrigerator WHERE nickname = ?";
            Cursor cursor = null;

            try {
                cursor = db.rawQuery(sql, new String[]{nickname});

                if (cursor != null && cursor.moveToFirst()) { // 커서가 null이 아니고 첫 번째 항목으로 이동 가능하면
                    do {
                        IngredientData ingredientData = new IngredientData();
                        ingredientData.ingredientName = cursor.getString(0); // 첫 번째 컬럼 (ingredientName)
                        ingredientList.add(ingredientData);
                        Log.d("MyDBHelper", "Loaded Ingredient: " + ingredientData.ingredientName); // 각 재료 이름 로그 출력
                    } while (cursor.moveToNext()); // 다음 항목으로 이동
                }else {
                    Log.d("MyDBHelper", "No ingredients found for nickname: " + nickname); // 닉네임에 대한 재료가 없을 경우 로그 출력
                }
            } catch (Exception e) {
                Log.e("MyDBHelper", "Error loading ingredients: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close(); // 커서 닫기
                }
                db.close(); // 데이터베이스 닫기
            }

            Log.d("MyDBHelper", "Total ingredients loaded: " + ingredientList.size()); // 로드된 재료 개수 로그 출력
            return ingredientList; // 검색 결과 리스트 반환
        }



        public ArrayList<IngredientData> searchIngredients(String query) {
            ArrayList<IngredientData> ingredientList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = null;

            try {
                // 검색 쿼리 작성 (LIKE를 사용하여 검색)
                String sql = "SELECT * FROM ingredients WHERE ingredientName LIKE ?";
                cursor = db.rawQuery(sql, new String[]{"%" + query + "%"});

                while (cursor.moveToNext()) {
                    IngredientData ingredientData = new IngredientData();
                    ingredientData.ingredientName = cursor.getString(1);
                    ingredientList.add(ingredientData);
                }
            } catch (Exception e) {
                Log.e("MyDBHelper", "Error searching ingredients: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
            return ingredientList; // 검색 결과 리스트 반환
        }

        public void addIngredient(String nickname, String ingredientName) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nickname", nickname); // 추가: nickname
            values.put("ingredientName", ingredientName);

            // 중복 체크: 같은 nickname과 ingredientName 조합이 있는지 확인
            Cursor cursor = db.query("refrigerator", null, "nickname=? AND ingredientName=?",
                    new String[]{nickname, ingredientName}, null, null, null);
            if (cursor.getCount() > 0) {
                Log.d("MyDBHelper", "Ingredient already exists: " + ingredientName);
            } else {
                long result = db.insert("refrigerator", null, values);
                if (result == -1) {
                    Log.d("MyDBHelper", "Failed to add ingredient: " + ingredientName);
                } else {
                    Log.d("MyDBHelper", "Ingredient added: " + ingredientName);
                }
            }
            cursor.close();
            db.close();

//            long result = db.insert("refrigerator", null, values);
//            if (result == -1) {
//                Log.d("MyDBHelper", "Failed to add ingredient: " + ingredientName);
//            } else {
//                Log.d("MyDBHelper", "Ingredient added: " + ingredientName);
//            }
//            db.close();
        }

        public void addShopping(String nickname, String shoppingName) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nickname", nickname); // 추가: nickname
            values.put("shoppingName", shoppingName);
            long result = db.insert("shopping", null, values);
            if (result == -1) {
                Log.d("MyDBHelper", "Failed to add ingredient: " + shoppingName);
            } else {
                Log.d("MyDBHelper", "Ingredient added: " + shoppingName);
            }
            db.close();
        }

        public void deleteShopping(String nickname, String itemName) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("shopping", "nickname = ? AND shoppingName = ?", new String[]{nickname, itemName});
            db.close();
        }

        public void deleteIngredient(String ingredientName) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("refrigerator", "ingredientName = ?", new String[]{ingredientName});
            db.close();
        }

        public List<RecipeData> getBookmarks(String nickname) {
            List<RecipeData> bookmarks = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT recipeName FROM bookmark WHERE nickname = ?", new String[]{nickname});

            if (cursor.moveToFirst()) {
                do {
                    String recipeName = cursor.getString(0);
                    bookmarks.add(new RecipeData(recipeName));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return bookmarks;
        }

        public void deleteBookmark(String nickname, String recipeName) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("bookmark", "nickname = ? AND recipeName = ?", new String[]{nickname, recipeName});
            Log.d("MyDBHelper", "Bookmark deleted: " + recipeName);
            db.close();
        }
        public void saveUserToLocalDB(String id, String pw, String name, String nickname, String birth, String phone) {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "INSERT INTO user (id, pw, name, nickname, birth, phone) VALUES (?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new Object[]{id, pw, name, nickname, birth, phone});
            db.close();
        }

    }

    public void requestUrl(String userStr){
        StringBuilder output = new StringBuilder();

        try{
            URL url = new URL(userStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(connection != null){
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line = null;
                while (true){
                    line = reader.readLine();
                    if(line == null)break;
                    output.append(line + "\n");
                }
                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            println("에러 발생 : " + e.toString());
        }
        println(output.toString());
    }

    public void println(final String data){
        handler.post(new Runnable() {
            @Override
            public void run() {
                userJsonParsing(data);
                recipeJsonParsing(data);
                ingredientParsing(data);
                refrigeratorParsing(data);
                shoppingJsonParsing(data);
                bookmarkJsonParsing(data);
                favoriteJsonParsing(data);
            }
        });
    }

    private void userJsonParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("user");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                String id = userObject.getString("id");
                String pw = userObject.getString("pw");
                String name = userObject.getString("name");
                String nickname = userObject.getString("nickname");
                String birth = userObject.getString("birth");
                String phone = userObject.getString("phone");

                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("pw", pw);
                values.put("name", name);
                values.put("nickname", nickname);
                values.put("birth", birth);
                values.put("phone", phone);

                sqlDB.insert("user", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void recipeJsonParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("recipe");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                int no = userObject.getInt("no");
                String recipeName = userObject.getString("recipeName");
                String recipeImage = userObject.getString("recipeImage");
                String recipeCategory = userObject.getString("recipeCategory");
                String recipeIngredients = userObject.getString("recipeIngredients");
                String recipeContent = userObject.getString("recipeContent");
                String dayTime = userObject.getString("dayTime");
                String nickname = userObject.getString("nickname");
                String favorite = userObject.getString("favorite");
                String count = userObject.getString("count");

                ContentValues values = new ContentValues();
                values.put("no", no);
                values.put("recipeName", recipeName);
                values.put("recipeImage", recipeImage);
                values.put("recipeCategory", recipeCategory);
                values.put("recipeIngredients", recipeIngredients);
                values.put("recipeContent", recipeContent);
                values.put("dayTime", dayTime);
                values.put("nickname", nickname);
                values.put("favorite", favorite);
                values.put("count", count);

                sqlDB.insert("recipe", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void ingredientParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("ingredients");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                int no = userObject.getInt("no");
                String ingredientName = userObject.getString("ingredientName");

                ContentValues values = new ContentValues();
                values.put("no", no);
                values.put("ingredientName", ingredientName);

                sqlDB.insert("ingredients", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void refrigeratorParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("refrigerator");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                String nickname = userObject.getString("nickname");
                String ingredientName = userObject.getString("ingredientName");

                ContentValues values = new ContentValues();
                values.put("nickname", nickname);
                values.put("ingredientName", ingredientName);

                sqlDB.insert("refrigerator", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void shoppingJsonParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("shopping");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                String nickname = userObject.getString("nickname");
                String shoppingName = userObject.getString("shoppingName");

                ContentValues values = new ContentValues();
                values.put("nickname", nickname);
                values.put("shoppingName", shoppingName);

                sqlDB.insert("shopping", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void bookmarkJsonParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("bookmark");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                String nickname = userObject.getString("nickname");
                String recipeName = userObject.getString("recipeName");

                ContentValues values = new ContentValues();
                values.put("nickname", nickname);
                values.put("recipeName", recipeName);

                sqlDB.insert("bookmark", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void favoriteJsonParsing(String json){
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray= jsonObject.getJSONArray("favorite");
            sqlDB = myDBHelper.getWritableDatabase();

            for(int i =0;i<jsonArray.length();i++){
                JSONObject userObject = jsonArray.getJSONObject(i);

                String nickname = userObject.getString("nickname");
                String recipeName = userObject.getString("recipeName");

                ContentValues values = new ContentValues();
                values.put("nickname", nickname);
                values.put("recipeName", recipeName);

                sqlDB.insert("favorite", null, values);

            }
            sqlDB.close();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

}
