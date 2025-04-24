package com.example.recipeproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    LayoutInflater layoutInflater = null;

    private ArrayList<RecipeData> recipeDataArrayList = null;
    private int layoutType; // 레이아웃 타입을 구분할 변수

    public ListAdapter(ArrayList<RecipeData> recipeDataArrayList, int layoutType){
        this.recipeDataArrayList = recipeDataArrayList;
        this.layoutType = layoutType;
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
    public int getItemViewType(int position) {
        return layoutType; // 생성자에서 전달된 layoutType을 반환
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        if (layoutInflater == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        int viewType = getItemViewType(i); // 레이아웃 타입에 따른 뷰 결정

        // viewType에 따라 서로 다른 레이아웃을 인플레이트
        if (view == null) {
            if (viewType == 0) {
                view = layoutInflater.inflate(R.layout.recipelist_item, viewGroup, false); // 다른 레이아웃
            } else if(viewType == 1) {
                view = layoutInflater.inflate(R.layout.regrigerator_recipelist_item, viewGroup, false); // 기본 레이아웃
            }
        }

        if (viewType == 0) {
            TextView textTitle = view.findViewById(R.id.textTitle); // 첫 번째 레이아웃의 ID
            textTitle.setText(recipeDataArrayList.get(i).recipeName);
        } else if (viewType == 1) {
            // 두 번째 레이아웃의 경우 (regrigerator_recipelist_item.xml)
            TextView textTitle = view.findViewById(R.id.recipeName); // 레시피 이름 표시
            TextView ingredientText = view.findViewById(R.id.ingredient); // 재료 표시

            // 레시피 이름과 재료를 설정
            RecipeData recipeData = recipeDataArrayList.get(i);
            textTitle.setText(recipeData.recipeName);

            // getIngredients()로 재료 가져오기
            ingredientText.setText(recipeData.getIngredients());
            Log.d("보유재료 뜨게하라", "보유재료 : " + recipeData.getIngredients());

        }

        return view;
    }
    public void updateData(ArrayList<RecipeData> newData) {
        recipeDataArrayList.clear();
        recipeDataArrayList.addAll(newData);
        notifyDataSetChanged();
    }
}
