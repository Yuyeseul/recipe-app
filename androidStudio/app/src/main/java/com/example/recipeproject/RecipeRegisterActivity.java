package com.example.recipeproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class RecipeRegisterActivity extends AppCompatActivity {

    Spinner spinner;
    EditText editTitle, editIngredients, editContents;
    Button btnImage;
    ImageView imageView;
    final int GET_GALLERY_IMAGE = 200;
    String selectedImagePath;
    ImageButton back;
    private static final int PICK_IMAGE_REQUEST = 1;

    private String nickname;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_register);

        Toolbar toolbar = findViewById(R.id.tRecipeRegister);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        SharedPreferences sharedPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        nickname = sharedPref.getString("nickname", "");

        spinner = (Spinner) findViewById(R.id.spinner);
        editTitle = (EditText) findViewById(R.id.editTitle);
        editContents = (EditText) findViewById(R.id.editContents);
        editIngredients = (EditText) findViewById(R.id.editIngredients);
        btnImage = (Button) findViewById(R.id.btnImage);
        imageView = (ImageView) findViewById(R.id.imageView2);

        back = (ImageButton) findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //        다른 액티비티에서 아이디 사용


        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.register) {

            registerRecipe(nickname);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectedImagePath = getRealPathFromURI(imageUri);
            imageView.setImageURI(imageUri); // 이미지 미리 보기
        }
    }

    // URI를 실제 경로로 변환하는 메서드
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    private void registerRecipe(String nickname){

        String recipeName = editTitle.getText().toString();
        String recipeCategory = spinner.getSelectedItem().toString();
        String recipeIngredients = editIngredients.getText().toString();
        String recipeContent = editContents.getText().toString();
        // 기본값 설정
        int favorite = 0;
        int count = 0;

        // 현재 시간 가져오기
        String dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 입력 값 확인
        if (recipeName.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(recipeIngredients.isEmpty()){
            Toast.makeText(this, "재료를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(recipeContent.isEmpty()){
            Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        sendPostToServer(recipeName, imageUri, recipeCategory, recipeIngredients, recipeContent, dayTime, nickname, favorite, count);

        Log.e("RecipeRegister", "recipeName: " + recipeName);
        Log.e("RecipeRegister", "imageUri: " + imageUri);
        Log.e("RecipeRegister", "recipeCategory: " + recipeCategory);
        Log.e("RecipeRegister", "recipeIngredients: " + recipeIngredients);
        Log.e("RecipeRegister", "recipeContent: " + recipeContent);
        Log.e("RecipeRegister", "dayTime: " + dayTime);
        Log.e("RecipeRegister", "nickname: " + nickname);
        Log.e("RecipeRegister", "favorite: " + favorite);
        Log.e("RecipeRegister", "count: " + count);

    }

    private void sendPostToServer(String recipeName, Uri imageUri, String recipeCategory, String recipeIngredients,String recipeContent, String dayTime, String nickname, int favorite, int count) {
        Map<String, String> params = new HashMap<>();
        params.put("recipeName", recipeName);
        params.put("recipeCategory", recipeCategory);
        params.put("recipeIngredients", recipeIngredients);
        params.put("recipeContent", recipeContent);
        params.put("dayTime", dayTime);
        params.put("nickname", nickname);
        params.put("favorite", String.valueOf(favorite)); // favorite 기본값 0
        params.put("count", String.valueOf(count));       // count 기본값 0

        // 로그 추가
        Log.e("RecipeRegister", "Sending Params: " + params.toString());

        Map<String, MultipartRequest.DataPart> byteData = new HashMap<>();

        // 단일 이미지 처리
        String fileName = "image.jpg"; // 파일 이름 지정
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null) {
            mimeType = "image/jpeg"; // 기본 MIME 타입 설정
        }

        byte[] fileData = getFileDataFromUri(imageUri);
        if (fileData != null) {
            byteData.put("image", new MultipartRequest.DataPart(fileName, fileData, mimeType));
            Log.e("RecipeRegister", "Image added: " + fileName + ", Size: " + fileData.length);
        } else {
            Log.e("RecipeRegister", "Failed to get file data from URI: " + imageUri.toString());
        }

        // byteData의 사이즈 확인
        Log.e("RecipeRegister", "Total images: " + byteData.size());

        MultipartRequest multipartRequest = new MultipartRequest(
                "https://ys.calab.myds.me/recipeInsert.php",
                response -> {
                    Log.e("MultipartRequest", "Server response: " + response);
                    Toast.makeText(RecipeRegisterActivity.this, "레시피가 저장되었습니다.", Toast.LENGTH_SHORT).show();

                    finish();
                },
                error -> {
                    Log.e("MultipartRequest", "Error response: " + error.getMessage());
                    Toast.makeText(RecipeRegisterActivity.this, "오류 발생: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                },
                new HashMap<>(),
                params,
                byteData
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(multipartRequest);
    }

    private byte[] getFileDataFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                byte[] data = IOUtils.toByteArray(inputStream);
                Log.e("MultipartRequest", "Image data size: " + data.length);
                return data;
            } else {
                Log.e("MultipartRequest", "InputStream is null for URI: " + uri.toString());
            }
        } catch (IOException e) {
            Log.e("MultipartRequest", "Error reading file from URI: " + uri.toString(), e);
        }
        return null;
    }

}

class MultipartRequest extends Request<String> {
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String lineEnd = "\r\n";
    private final Response.Listener<String> listener;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final Map<String, DataPart> byteData;

    public MultipartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener,
                            Map<String, String> headers, Map<String, String> params, Map<String, DataPart> byteData) {
        super(Method.POST, url, errorListener);
        this.listener = listener;
        this.headers = headers;
        this.params = params;
        this.byteData = byteData;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() {
        try {
            return params != null ? params : super.getParams();
        } catch (AuthFailureError e) {
            e.printStackTrace(); // 예외 발생 시 로그 출력
            return null; // 기본값 반환
        }
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // 사용자 ID 로그 추가
            if (params != null && params.containsKey("nickname")) {
                Log.e("MultipartRequest", "User nickname: " + params.get("nickname"));
            } else {
                Log.e("MultipartRequest", "User nickname not found in params");
            }

            // Add parameters
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    Log.e("MultipartRequest", "Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    bos.write(("--" + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd).getBytes());
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd).getBytes());
                    bos.write(lineEnd.getBytes());
                    bos.write(entry.getValue().getBytes());
                    bos.write(lineEnd.getBytes());
                }
            } else {
                Log.e("MultipartRequest", "Params are null");
            }

            // Add a single file
            if (byteData != null && !byteData.isEmpty()) { // byteData가 비어있지 않은지 확인
                // 첫 번째 파일만 추가 (단일 이미지 업로드)
                Map.Entry<String, DataPart> entry = byteData.entrySet().iterator().next();
                bos.write(("--" + boundary + lineEnd).getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + entry.getValue().getFileName() + "\"" + lineEnd).getBytes());
                bos.write(("Content-Type: " + entry.getValue().getContentType() + lineEnd).getBytes());
                bos.write(lineEnd.getBytes());
                bos.write(entry.getValue().getByteData()); // 파일 데이터 추가
                bos.write(lineEnd.getBytes());
            }

            // 바운더리 종료
            bos.write(("--" + boundary + "--" + lineEnd).getBytes());
            return bos.toByteArray();
        } catch (IOException e) {
            Log.e("MultipartRequest", "IOException: " + e.getMessage());
            return null;
        }
    }



    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // 예외 발생 시 로그 출력
            return Response.error(new VolleyError("Unsupported Encoding")); // 에러 처리
        }
    }


    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] byteData;
        private final String contentType;

        public DataPart(String fileName, byte[] byteData, String contentType) {
            this.fileName = fileName;
            this.byteData = byteData;
            this.contentType = contentType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getByteData() {
            return byteData;
        }

        public String getContentType() {
            return contentType;
        }
    }
}

