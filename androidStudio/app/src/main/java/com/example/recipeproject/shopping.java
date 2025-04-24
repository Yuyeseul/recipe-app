package com.example.recipeproject;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class shopping extends AppCompatActivity {
    Button btnAdd, btnAddRefrigerator;
    ImageButton back;
    RecyclerView shoppingList;
    EditText editMemo;
    private ShoppingAdapter shoppingAdapter;
    private ArrayList<ShoppingItem> shoppingItems;

    private MainActivity.MyDBHelper dbHelper;

    SQLiteDatabase db;
    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        editMemo = findViewById(R.id.editMemo);
        btnAdd = findViewById(R.id.btnAdd);
        btnAddRefrigerator = findViewById(R.id.btnAddRefrigerator);
        shoppingList = findViewById(R.id.shoppingList);

        shoppingItems = new ArrayList<>();
        shoppingAdapter = new ShoppingAdapter(shoppingItems);
        shoppingList.setLayoutManager(new LinearLayoutManager(this));
        shoppingList.setAdapter(shoppingAdapter);

        back = (ImageButton) findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dbHelper = new MainActivity.MyDBHelper(this, "recipe", null, 1); // DBHelper 초기화
        db = dbHelper.getReadableDatabase(); // 데이터베이스 열기

        // 닉네임 가져오기
        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        nickname = sharedPref.getString("nickname", "");

        loadShoppingList(nickname);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = editMemo.getText().toString();
                if (!itemName.isEmpty()) {
                    addShoppingItem(nickname, itemName);
                    editMemo.setText("");
                } else {
                    Toast.makeText(shopping.this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddRefrigerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCheckedItemsToRefrigerator();
            }
        });


    }

    private void loadShoppingList(String nickname) {
        // PHP에서 쇼핑리스트를 불러오는 코드 작성 (추후 구현)
        shoppingItems.clear();
        // 데이터베이스를 열기 전에 체크
        if (db == null || !db.isOpen()) {
            db = dbHelper.getReadableDatabase(); // 데이터베이스 열기
        }

        String sql = "SELECT shoppingName FROM shopping WHERE nickname = ?";
        Cursor cursor = null;
        cursor = db.rawQuery(sql, new String[]{nickname});
        while (cursor.moveToNext()) {
            String shoppingName = cursor.getString(0);
            ShoppingItem data = new ShoppingItem(shoppingName);

            shoppingItems.add(data);

            // 각 재료 이름을 로그에 출력
            Log.d("MyRefrigeratorActivity", "Loaded ingredient: " + shoppingName);
        }
        cursor.close(); // 커서 닫기
        shoppingAdapter.notifyDataSetChanged();
        Log.d("MyRefrigeratorActivity", "Ingredients List Size: " + shoppingItems.size()); // 리스트 크기 로그 출력
    }

    private void addShoppingItem(String nickname, String itemName) {
        // PHP로 데이터베이스에 항목 추가 요청
        // 서버의 응답을 확인하여 RecyclerView 업데이트
        dbHelper.addShopping(nickname, itemName);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("shopping", "Server response: " + response); // 서버 응답 로그 추가

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        Log.d("shopping", "shoppingItem successfully added to the server.");
//                        Toast.makeText(getApplicationContext(), "쇼핑아이템이 서버에 추가되었습니다.", Toast.LENGTH_SHORT).show();

                        // 쇼핑 리스트를 새로 고침하여 업데이트된 아이템을 반영
                        loadShoppingList(nickname);
                    } else {
                        Log.d("shopping", "Failed to add shoppingItem to the server.");
                        Toast.makeText(getApplicationContext(), "서버에 쇼핑아이템을 추가하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("shopping", "JSON 파싱 오류: " + e.getMessage());
                }
            }
        };

        ShoppingRequest shoppingRequest = new ShoppingRequest(nickname, itemName, responseListener);
// 요청 큐에 추가
        RequestQueue queue = Volley.newRequestQueue(shopping.this);
        queue.add(shoppingRequest);

    }

    public void deleteShoppingItem(int position, String itemName) {
        // PHP로 데이터베이스에서 항목 삭제 요청
        // 서버의 응답을 확인하여 RecyclerView에서 삭제

        dbHelper.deleteShopping(nickname, itemName); // DBHelper의 삭제 메서드 호출

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("shopping", "Server response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    String message = jsonObject.getString("message");

                    if (success) {
                        Log.d("shopping", "Ingredient successfully deleted from the server.");
                        shoppingItems.remove(position); // 쇼핑 리스트에서 항목 제거
                        shoppingAdapter.notifyItemRemoved(position);
//                        dbHelper.deleteIngredient(itemName); // 데이터베이스에서 제거
                        Log.d("shopping", "Ingredient deleted from the database: " + itemName);
                    } else {
                        Log.d("shopping", "Failed to delete ingredient from the server. Message: " + message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        ShoppingDeleteRequest deleteRequest = new ShoppingDeleteRequest(nickname, itemName, responseListener);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(deleteRequest);
    }

    private void moveCheckedItemsToRefrigerator() {
        // 체크된 항목을 냉장고 테이블에 추가
        ArrayList<ShoppingItem> checkedItems = new ArrayList<>();

        for (ShoppingItem item : shoppingItems) {
            if (item.isChecked()) {
                checkedItems.add(item);
            }
        }
        if (checkedItems.isEmpty()) {
            Toast.makeText(this, "체크된 항목이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        for (ShoppingItem item : checkedItems) {
            String itemName = item.getShoppingName();
            // 냉장고에 추가하는 서버 요청
            addToRefrigerator(nickname, itemName);
        }
        // 체크된 아이템을 쇼핑 리스트에서 제거
        for (int i = shoppingItems.size() - 1; i >= 0; i--) { // 역순으로 제거
            ShoppingItem item = shoppingItems.get(i);
            if (item.isChecked()) {
                deleteShoppingItem(i, item.getShoppingName()); // DB에서 삭제
            }
        }
        shoppingAdapter.notifyDataSetChanged();
        Toast.makeText(this, "체크된 항목이 냉장고에 추가되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void addToRefrigerator(String nickname, String itemName) {

        dbHelper.addIngredient(nickname, itemName);
        // PHP로 냉장고에 아이템 추가 요청
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("shopping", "Server response for refrigerator: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        Log.d("shopping", "Item successfully added to the refrigerator.");
                    } else {
                        Log.d("shopping", "Failed to add item to the refrigerator.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        // 냉장고 추가 요청을 위한 클래스를 생성
        RefrigeratorAddRequest refrigeratorRequest = new RefrigeratorAddRequest(nickname, itemName, responseListener);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(refrigeratorRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close(); // 액티비티가 파괴될 때 데이터베이스 닫기
        }
    }
}

class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    private ArrayList<ShoppingItem> shoppingItems;

    public ShoppingAdapter(ArrayList<ShoppingItem> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        ShoppingItem item = shoppingItems.get(position);
        holder.shoppingName.setText(item.getShoppingName());

        // CheckBox의 상태 설정
        holder.shoppingCheckBox.setOnCheckedChangeListener(null); // 기존 리스너 제거
        holder.shoppingCheckBox.setChecked(item.isChecked()); // 체크 상태 설정

        holder.shoppingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 항목 삭제
                int position = holder.getAdapterPosition(); // 현재 항목의 포지션
                String itemName = item.getShoppingName(); // 삭제할 항목 이름

                // 쇼핑 리스트에서 삭제 및 DB에서 삭제
                ((shopping) v.getContext()).deleteShoppingItem(position, itemName);

            }
        });

        holder.shoppingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> item.setChecked(isChecked));
    }

    @Override
    public int getItemCount() {
        return shoppingItems.size();
    }

    public static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView shoppingName;
        CheckBox shoppingCheckBox;
        Button shoppingDelete;

        public ShoppingViewHolder(View itemView) {
            super(itemView);
            shoppingName = itemView.findViewById(R.id.shoppingName);
            shoppingCheckBox = itemView.findViewById(R.id.shoppingCheckBox);
            shoppingDelete = itemView.findViewById(R.id.shoppingDelete);

        }
    }
}
class ShoppingItem {
    private String shoppingName;
    private boolean isChecked;

    // 쇼핑 항목 이름만 받는 생성자
    public ShoppingItem(String shoppingName) {
        this.shoppingName = shoppingName;
        this.isChecked = false;  // 기본값 설정
    }

    public ShoppingItem(String shoppingName, boolean isChecked) {
        this.shoppingName = shoppingName;
        this.isChecked = isChecked;
    }

    public String getShoppingName() {
        return shoppingName;
    }

    public void setShoppingName(String shoppingName) {
        this.shoppingName = shoppingName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

class ShoppingRequest extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/shoppingInsert.php";
    private Map<String, String> map;

    public ShoppingRequest(String nickname, String shoppingName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        // 요청에 포함할 파라미터들
        map = new HashMap<>();
        map.put("nickname", nickname);
        map.put("shoppingName", shoppingName);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

class ShoppingDeleteRequest extends StringRequest {
    private static final String DELETE_REQUEST_URL = "http://ys.calab.myds.me/shoppingDelete.php";

    public ShoppingDeleteRequest(String nickname, String shoppingName, Response.Listener<String> listener) {
        super(Request.Method.DELETE, DELETE_REQUEST_URL + "/" + nickname + "/" + shoppingName, listener, null);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return null; // 요청 본문에 파라미터가 없으므로 null 반환
    }
}

class RefrigeratorAddRequest  extends StringRequest {
    final static private String URL = "http://ys.calab.myds.me/save.php";
    private Map<String, String> map;

    public RefrigeratorAddRequest (String nickname, String ingredientName, Response.Listener<String> listener) {
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

