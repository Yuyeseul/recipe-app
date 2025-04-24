package com.example.recipeproject;

public class IngredientData {
    public String ingredientName ;
    public boolean isSelected; // 선택 상태 추가

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
        this.isSelected = false; // 초기화
    }

    public IngredientData() {
        // 기본 생성자
    }

    public IngredientData(String ingredientName) {
        this.ingredientName = ingredientName;
    }
}
