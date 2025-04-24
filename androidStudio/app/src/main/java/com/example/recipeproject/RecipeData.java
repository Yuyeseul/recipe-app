package com.example.recipeproject;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.ArrayList;

public class RecipeData {
    public String recipeName ;
    public String recipeImage;
    public String recipeCategory;
    public String recipeIngredients;
    public String recipeContent;
    public String matchingIngredients;
    ArrayList<String> ingredients = new ArrayList<>(); // 재료 목록

    // 기본 생성자
    public RecipeData() {
        this.recipeIngredients = ""; // 기본값 설정
    }

//    public RecipeData(String recipeName) {
//        this.recipeName = recipeName;
//        this.ingredients = new ArrayList<>(); // 초기화
//    }


    public String recipeClass;

    public String getRecipeCategory() {
        return recipeCategory;
    }

    public void setRecipeCategory(String recipeCategory) {
        this.recipeCategory = recipeCategory;
    }

    public String getRecipeContent() {
        return recipeContent;
    }

    public void setRecipeContent(String recipeContent) {
        this.recipeContent = recipeContent;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public String getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(String recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    public String getRecipeName() {
        return recipeName;
    }

    // 레시피 이름만 필요한 생성자 (북마크 기능용)
    public RecipeData(String recipeName) {
        this.recipeName = recipeName;
    }

    // 재료 목록을 String으로 반환하는 메서드
    public String getIngredients() {
        if (ingredients == null || ingredients.isEmpty()) {
            return "보유 재료가 없습니다."; // 재료가 없을 경우
        }
        return String.join(", ", ingredients);
    }

    public void addIngredient(String ingredient) {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        ingredients.add(ingredient); // 재료를 목록에 추가
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }
//    public RecipeData(String title){
////        this.image = image;
//        this.title = title;
//    }
}
