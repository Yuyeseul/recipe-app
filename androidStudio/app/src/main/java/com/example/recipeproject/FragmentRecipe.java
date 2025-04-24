package com.example.recipeproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class FragmentRecipe extends Fragment {

    Chip search;
    Button register;
    ListView recipeList;
    MainActivity.MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
//    Button[] btn = new Button[10];
//    Integer[] btn_id = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);
        // Inflate the layout for this fragment
        register = (Button) view.findViewById(R.id.btnRegister);
        search = (Chip) view.findViewById(R.id.chipSearch);
        recipeList = (ListView) view.findViewById(R.id.recipeList);
//        for(int i=0;i<8;i++){
//            btn[i] = (Button) view.findViewById(btn_id[i]);
//        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RecipeRegisterActivity.class);
                startActivity(intent);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        ArrayList<RecipeData> recipeDataList = new ArrayList<>();

        myDBHelper = new MainActivity.MyDBHelper(getActivity(), "recipe", null, 1);
        sqlDB = myDBHelper.getReadableDatabase();

        String sql = "select * from recipe";
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while(cursor.moveToNext()){
            RecipeData recipeData = new RecipeData();
            String recipeName = cursor.getString(1);

            recipeData.recipeName = recipeName;
//            recipeData.setRecipeName(recipeName);
            recipeDataList.add(recipeData);

        }
        cursor.close();
        sqlDB.close();

        ListAdapter listAdapter = new ListAdapter(recipeDataList, 0);
        recipeList.setAdapter(listAdapter);

//        for(int i = 0;i<8;i++){
//            final int index = i;
//            btn[index].setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String category = btn[index].getText().toString();
//                    recipeDataList.clear();
//                    sqlDB = myDBHelper.getReadableDatabase();
//                    String sql = "select * from recipe where recipeCategory like '%" + category + "%';";
//                    Cursor cursor = sqlDB.rawQuery(sql, null);
//                    while(cursor.moveToNext()){
//                        RecipeData recipeData = new RecipeData();
//                        String recipeName = cursor.getString(1);
//
//                        recipeData.recipeName = recipeName;
////            recipeData.setRecipeName(recipeName);
//                        recipeDataList.add(recipeData);
//
//                    }
//                    cursor.close();
//                    sqlDB.close();
//
////                    ListAdapter listAdapter = new ListAdapter(recipeDataList);
////                    recipeList.setAdapter(listAdapter);
//
//                    listAdapter.notifyDataSetChanged();
//                }
//            });
//        }

        recipeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RecipeData selectedRecipe = (RecipeData) adapterView.getAdapter().getItem(position);
                String recipeName = selectedRecipe.recipeName;

                Intent intent = new Intent(getActivity(), RecipePageActivity.class);
                intent.putExtra("recipeName", recipeName);
                startActivity(intent);
            }
        });

        return view;
    }


}








