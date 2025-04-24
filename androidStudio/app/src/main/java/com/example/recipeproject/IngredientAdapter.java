package com.example.recipeproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class IngredientAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater; // 레이아웃 인플레이터
    private ArrayList<IngredientData> ingredients; // IngredientData 리스트로 변경
    private int selectedPosition = -1; // 클릭된 아이템의 위치

    public IngredientAdapter(Context context, ArrayList<IngredientData> ingredients) {
        this.ingredients = ingredients; // 생성자에서 재료 리스트 초기화
        this.layoutInflater = LayoutInflater.from(context); // 레이아웃 인플레이터 초기화
    }

    @Override
    public int getCount() {
        return ingredients.size(); // 재료 리스트의 크기 반환
    }

    @Override
    public Object getItem(int position) {
        return ingredients.get(position); // 특정 위치의 재료 반환
    }

    @Override
    public long getItemId(int position) {
        return position; // 아이템 ID로 위치 반환
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 뷰가 없으면 새로운 뷰를 인플레이트
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.search_result_item, parent, false);
        }

        // 재료 이름을 표시할 TextView 초기화
        TextView textViewName = convertView.findViewById(R.id.searchResult);

        // 현재 재료 데이터를 가져와서 TextView에 설정
        IngredientData currentIngredient = ingredients.get(position); // IngredientData로 변경
        textViewName.setText(currentIngredient.ingredientName); // 재료 이름 설정

        // 클릭된 아이템의 색상 변경
        if (position == selectedPosition) {
            convertView.setBackgroundColor(Color.LTGRAY); // 클릭된 아이템 색상
        } else {
            convertView.setBackgroundColor(Color.WHITE); // 기본 색상
        }

        return convertView; // 생성된 뷰 반환
    }

    // 재료 추가 메서드
    public void addIngredient(IngredientData ingredient) {
        ingredients.add(ingredient); // 재료 리스트에 추가
        notifyDataSetChanged(); // 데이터 변경 알림
    }

    // 재료 제거 메서드
    public void removeIngredient(int position) {
        if (position >= 0 && position < ingredients.size()) {
            ingredients.remove(position); // 리스트에서 제거
            notifyDataSetChanged(); // 데이터 변경 알림
        }
    }

    // 아이템 선택 토글 메서드
    public void toggleSelection(int position) {
        if (selectedPosition == position) {
            selectedPosition = -1; // 이미 선택된 아이템을 다시 클릭하면 선택 해제
        } else {
            selectedPosition = position; // 선택된 아이템의 위치 업데이트
        }
        notifyDataSetChanged(); // 데이터 변경 알림
    }
}
