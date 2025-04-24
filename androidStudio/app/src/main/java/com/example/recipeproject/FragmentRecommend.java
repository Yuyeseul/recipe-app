package com.example.recipeproject;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.recipeproject.R.id.btnNameSearch;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FragmentRecommend extends Fragment {
    Button btnName;
    Button btnCamera;
    Button btnReset;
    Button getImage;
    TextView textClass, textViewImage, textCategory, Category, Class;
    ListView recommendListview;
    RecommendListAdapter adapter;
    ArrayList<RecipeData> recipeArrayList = new ArrayList<>();
    MainActivity.MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    RecyclerView recyclerView;
    LinearLayout recipePage;

    Interpreter tfLite;
    private static final int IMAGE_SIZE = 224;

    private String lastPredictedLabel;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int REQUEST_CODE = 0;

    private String[] labels = {
            "가지", "간장", "감자", "계란", "고구마", "고등어", "고추", "고추장", "김치", "꽃게", "닭고기",
            "당근", "콩", "돼지고기", "된장", "두부", "딸기", "떡", "레몬", "마늘", "만두", "망고", "무",
            "바나나", "바지락", "밥", "배", "배추", "브로콜리", "비트", "사과", "상추", "새우", "생강",
            "석류", "소고기", "소시지", "수박", "숙주", "순무", "시금치", "아보카도", "양배추",
            "양송이버섯", "양파", "어묵", "오렌지", "오이", "옥수수", "완두콩", "우유", "조개",
            "차돌박이", "체리", "콩나물", "키위", "토마토", "대파", "파인애플", "파파야", "팽이버섯",
            "포도", "피망", "호박"
    };

    private int pageNumber = 0;  // 현재 페이지 번호
    private static final int PAGE_SIZE = 5;  // 한 번에 가져올 레시피 개수

    private String[] currentQueries;

    private static final String TAG = "FragmentRecommend";
    ArrayList<Uri> uriList = new ArrayList<>();     // 이미지의 uri를 담을 ArrayList 객체
    MultiImageAdapter imageAdapter;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        btnName = (Button) view.findViewById(R.id.btnNameSearch);
        btnCamera = (Button) view.findViewById(R.id.btnCameraSearch);
        recommendListview = (ListView) view.findViewById(R.id.recommendList);
        adapter = new RecommendListAdapter(recipeArrayList);
        textClass = (TextView) view.findViewById(R.id.textClass);
        textViewImage  = (TextView) view.findViewById(R.id.textViewImage);
        textCategory  = (TextView) view.findViewById(R.id.textCategory);
        Category  = (TextView) view.findViewById(R.id.Category);
        Class  = (TextView) view.findViewById(R.id.Class);
        btnReset = (Button) view.findViewById(R.id.btnReset);
        recyclerView = (RecyclerView) view.findViewById(R.id.imageRecycler);
        getImage = (Button) view.findViewById(R.id.btnGetImage);
        myDBHelper = new MainActivity.MyDBHelper(getActivity(), "recipe", null, 1);
        recipePage = (LinearLayout) view.findViewById(R.id.recipePage);

        // 버튼의 초기 색상 지정
        btnName.setBackgroundColor(getResources().getColor(R.color.pink)); // 초기 색상
        btnCamera.setBackgroundColor(getResources().getColor(R.color.pink)); // 초기 색상


        recipePage.setVisibility(View.GONE);
        textViewImage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textClass.setVisibility(View.GONE);
        textCategory.setVisibility(View.GONE);
        Category.setVisibility(View.GONE);
        Class.setVisibility(View.GONE);

        try {
            tfLite = new Interpreter(loadModelFile(getActivity()));
        } catch (IOException e) {
            Log.e("ModelLoadError", "모델 파일을 로드하는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "모델 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LinearLayout linearLayout = (LinearLayout) View.inflate(getActivity(), R.layout.activity_name_search, null);
                new AlertDialog.Builder(getActivity()).setView(linearLayout)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.e("확인", "확인누름");
                                uriList.clear();
                                textClass.setText("");
                                pageNumber = 0;
                                btnName.setBackgroundColor(getResources().getColor(R.color.main));
                                btnCamera.setBackgroundColor(getResources().getColor(R.color.pink));
                                recipePage.setVisibility(View.VISIBLE);
                                textViewImage.setVisibility(View.VISIBLE);
                                textCategory.setVisibility(View.VISIBLE);
                                Category.setVisibility(View.VISIBLE);
                                Class.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);

                                EditText word = (EditText) linearLayout.findViewById(R.id.searchword);
                                Spinner spinner = (Spinner) linearLayout.findViewById(R.id.spinner2);

                                String value = word.getText().toString();
                                textClass.setText("입력한 재료 : " + value);
                                String[] values = value.split(",");
                                for (int j = 0; j < values.length; j++) {
                                    values[j] = values[j].trim();  // 각 단어 앞뒤의 공백 제거
                                }
                                // Spinner에서 선택한 카테고리 받아오기
                                String selectedCategory = spinner.getSelectedItem().toString();

                                // 입력한 재료가 비어있지 않은 경우에만 검색 실행
                                if (value.trim().isEmpty()) {
                                    Toast.makeText(getContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                                } else {
                                    pageNumber = 0;
                                    searchRecipe(values, selectedCategory);
                                }

                                dialogInterface.dismiss();

                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                textClass.setText("");
                pageNumber = 0;

                classifySelectedImage();
                Toast.makeText(getContext(), "카메라", Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // 선택된 카테고리를 가져옵니다.
                String selectedCategory = textCategory.getText().toString();
                pageNumber++;
                searchRecipe(currentQueries, selectedCategory);
            }
        });

        recommendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RecipeData selectedRecipe = (RecipeData) adapterView.getAdapter().getItem(position);
                String recipeName = selectedRecipe.recipeName;

                Intent intent = new Intent(getActivity(), RecipePageActivity.class);
                intent.putExtra("recipeName", recipeName);
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) { // 어떤 이미지도 선택하지 않은 경우
            Toast.makeText(getActivity(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
        } else { // 이미지를 하나라도 선택한 경우

            // 기존 리스트 초기화
            uriList.clear();
            textViewImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (data.getClipData() == null) { // 이미지를 하나만 선택한 경우
                Log.e("single choice: ", String.valueOf(data.getData()));
                Uri imageUri = data.getData();
                uriList.add(imageUri);

                imageAdapter = new MultiImageAdapter(uriList, getActivity());
                recyclerView.setAdapter(imageAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
            } else { // 이미지를 여러장 선택한 경우
                ClipData clipData = data.getClipData();
                Log.e("clipData", String.valueOf(clipData.getItemCount()));

                if (clipData.getItemCount() > 10) { // 선택한 이미지가 11장 이상인 경우
                    Toast.makeText(getActivity(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                } else { // 선택한 이미지가 1장 이상 10장 이하인 경우
                    Log.e(TAG, "multiple choice");

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri(); // 선택한 이미지들의 uri를 가져온다.
                        try {
                            uriList.add(imageUri); // uri를 list에 담는다.
                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }

                    imageAdapter = new MultiImageAdapter(uriList, getActivity());
                    recyclerView.setAdapter(imageAdapter); // 리사이클러뷰에 어댑터 세팅
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true)); // 리사이클러뷰 수평 스크롤 적용
                }
            }
        }
    }

    public void searchRecipe(String[] queries, String category) {
        recipeArrayList.clear();
        sqlDB = myDBHelper.getReadableDatabase();

        currentQueries = queries;

        // SQL 쿼리 작성: 각 검색어에 대해 LIKE 조건을 계산하고 일치하는 개수를 기반으로 정렬
        StringBuilder sqlBuilder = new StringBuilder("SELECT *, ");
        sqlBuilder.append("(");
        for (int i = 0; i < queries.length; i++) {
            sqlBuilder.append("CASE WHEN recipeIngredients LIKE '%").append(queries[i]).append("%' THEN 1 ELSE 0 END");
            if (i < queries.length - 1) {
                sqlBuilder.append(" + ");  // 마지막 조건 뒤에는 +를 추가하지 않음
            }
        }
        sqlBuilder.append(") AS matchCount, ");  // 일치하는 재료 수를 계산

        // 조회수와 좋아요 수를 기반으로 정렬하는 표현식 추가
        sqlBuilder.append("((count + (2 * favorite)) / 2.0) AS sortScore "); // 정렬 기준 추가

        sqlBuilder.append("FROM recipe ");
        sqlBuilder.append("WHERE (");  // 하나라도 일치하는 재료가 있는 경우에만 필터링
        for (int i = 0; i < queries.length; i++) {
            sqlBuilder.append("recipeIngredients LIKE '%").append(queries[i]).append("%'");
            if (i < queries.length - 1) {
                sqlBuilder.append(" OR ");  // 마지막 조건 뒤에는 OR를 추가하지 않음
            }
        }
        sqlBuilder.append(") ");  // WHERE 조건 종료

        // 선택된 카테고리 필터 추가
        if (!category.equals("전체")) {  // '전체'는 필터링 없이 모든 카테고리를 포함
            sqlBuilder.append("AND recipeCategory = '").append(category).append("' ");
        }

        // matchCount로 정렬하고, 같을 경우 sortScore로 정렬
        sqlBuilder.append("ORDER BY matchCount DESC, sortScore DESC ");
        sqlBuilder.append("LIMIT ").append(PAGE_SIZE).append(" OFFSET ").append(pageNumber * PAGE_SIZE);  // 페이지 기반 쿼리

        String sql = sqlBuilder.toString();

        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            RecipeData recipeData = new RecipeData();
            String recipeName = cursor.getString(1); // 레시피 이름 가져오기
            String recipeIngredients = cursor.getString(4);

            recipeData.recipeName = recipeName;

            for (String query : queries) {
                if (recipeIngredients.contains(query)) {
                    recipeData.ingredients.add(query); // 일치하는 재료 추가
                }
            }

            recipeArrayList.add(recipeData);
        }
        cursor.close();
        sqlDB.close();

        textClass.setVisibility(View.VISIBLE);
        recipePage.setVisibility(View.VISIBLE);
        textCategory.setVisibility(View.VISIBLE);
        Category.setVisibility(View.VISIBLE);
        Class.setVisibility(View.VISIBLE);

        // 선택된 카테고리를 텍스트뷰에 표시
        textCategory.setText(category);

        adapter = new RecommendListAdapter(recipeArrayList);
        recommendListview.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    private void classifySelectedImage() {
        List<Bitmap> bitmaps = getBitmapsFromRecyclerView(); // RecyclerView에서 비트맵 가져오기
        if (bitmaps.isEmpty()) {
            Toast.makeText(getContext(), "먼저 이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> results = new ArrayList<>(); // 결과를 저장할 리스트
        for (Bitmap bitmap : bitmaps) {
            classifyImage(bitmap, results);
        }
        // 모든 이미지를 분류한 후 레시피 검색 수행
        List<String> resultList = new ArrayList<>(results); // Set을 List로 변환
        displayResultsDialog(resultList);
//
//        // 모든 이미지를 분류한 후 레시피 검색 수행
//        displayResultsDialog(results);
    }

    private void searchRecipeFromResults(List<String> results, String selectedCategory) {
        String[] queries = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            queries[i] = results.get(i).split(" ")[0]; // 예: "계란 (신뢰도: 99.99%)" -> "계란"
        }
        searchRecipe(queries, selectedCategory); // 레시피 검색 수행
    }

    private void classifyImage(Bitmap bitmap, List<String> results) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);
        float[][] output = new float[1][labels.length]; // 클래스 수에 맞게 조정
        try {
            tfLite.run(inputBuffer, output);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "이미지 분류 실패", Toast.LENGTH_SHORT).show();
            return;
        }
        // 가장 높은 확률을 가진 클래스 찾기
        int maxIndex = 0;
        float maxConfidence = output[0][0];
        for (int i = 1; i < labels.length; i++) {
            if (output[0][i] > maxConfidence) {
                maxConfidence = output[0][i];
                maxIndex = i;
            }
        }
        String predictedLabel = labels[maxIndex];
        results.add(predictedLabel + " (신뢰도: " + String.format("%.2f", maxConfidence * 100) + "%)"); // 결과 저장
        // 결과를 textClass에 반점으로 나누어 추가
        String allResults = TextUtils.join(", ", results);
        textClass.setText(allResults);
//        // 모든 이미지 분류 후 다이얼로그 표시
//        if (results.size() == getBitmapsFromRecyclerView().size()) {
//            displayResultsDialog(results);
//        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixel : intValues) {
            byteBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.f); // R
            byteBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.f);  // G
            byteBuffer.putFloat((pixel & 0xFF) / 255.f);         // B
        }

        return byteBuffer;
    }

    private void displayResultsDialog(List<String> results) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_results, null);

        // 분류 결과 표시
        TextView tvResults = dialogView.findViewById(R.id.tvResults);

        btnName.setBackgroundColor(getResources().getColor(R.color.pink));
        btnCamera.setBackgroundColor(getResources().getColor(R.color.main));
        StringBuilder sb = new StringBuilder("분류 결과:\n");
        for (String result : results) {
            sb.append(result).append("\n");
        }
        tvResults.setText(sb.toString());

        // Spinner 설정
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);


        builder.setView(dialogView)
                .setTitle("이미지 분류 결과")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 카테고리 선택 및 검색어 추출
                        String selectedCategory = spinnerCategory.getSelectedItem().toString();
                        String[] queries = new String[results.size()];
                        for (int i = 0; i < results.size(); i++) {
                            queries[i] = results.get(i).split(" ")[0];
                        }

                        // 검색 수행
                        searchRecipe(queries, selectedCategory);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }



    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("converted_tflite/model_unquant.tflite"); // 경로 수정
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<Bitmap> getBitmapsFromRecyclerView() {
        List<Bitmap> bitmaps = new ArrayList<>();

        // RecyclerView의 어댑터에 접근
        MultiImageAdapter adapter = (MultiImageAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            // 어댑터의 데이터 리스트에서 비트맵을 가져옴
            for (Uri uri : adapter.getUriList()) { // 이미지 URI를 가져온다고 가정
                Bitmap bitmap = getBitmapFromUri(uri);
                if (bitmap != null) {
                    bitmaps.add(bitmap);
                }
            }
        }

        return bitmaps;
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream input = getActivity().getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

class RecommendListAdapter extends BaseAdapter {

    LayoutInflater layoutInflater = null;

    private ArrayList<RecipeData> recipeDataArrayList = null;

    public RecommendListAdapter(ArrayList<RecipeData> recipeDataArrayList) {
        this.recipeDataArrayList = recipeDataArrayList;
    }

    @Override
    public int getCount() {
        return recipeDataArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return recipeDataArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            Context context = viewGroup.getContext();
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            view = LayoutInflater.from(context).inflate(R.layout.recommend_recipelist_item, null);
        }

        TextView r_recipeName = view.findViewById(R.id.r_recipeName);
        TextView r_ingredient = view.findViewById(R.id.r_ingredient);

        // 레시피 이름과 재료를 설정
        RecipeData recipeData = recipeDataArrayList.get(i);

        r_recipeName.setText(recipeData.recipeName);

        // 재료 리스트를 문자열로 변환
        String ingredientsString = TextUtils.join(", ", recipeData.ingredients);
        r_ingredient.setText(ingredientsString);

        return view;
    }

}

class MultiImageAdapter extends RecyclerView.Adapter<MultiImageAdapter.ViewHolder>{
    private ArrayList<Uri> mData = null ;
    private Context mContext = null ;

    // 생성자에서 데이터 리스트 객체, Context를 전달받음.
    MultiImageAdapter(ArrayList<Uri> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조.
            image = itemView.findViewById(R.id.imageItem);
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater - XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    @Override
    public MultiImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;    // context에서 LayoutInflater 객체를 얻는다.
        View view = inflater.inflate(R.layout.image_item, parent, false) ;	// 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        MultiImageAdapter.ViewHolder vh = new MultiImageAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(MultiImageAdapter.ViewHolder holder, int position) {
        Uri image_uri = mData.get(position) ;

        Glide.with(mContext)
                .load(image_uri)
                .into(holder.image);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

    // 선택된 이미지 URI 리스트를 반환하는 메소드
    public ArrayList<Uri> getUriList() {
        return mData; // mData 리스트 반환
    }

}
