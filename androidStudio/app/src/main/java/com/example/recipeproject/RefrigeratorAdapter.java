package com.example.recipeproject;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class RefrigeratorAdapter extends RecyclerView.Adapter<RefrigeratorAdapter.ViewHolder> {

    private ArrayList<IngredientData> ingredientsList; // IngredientData 리스트
    private LayoutInflater layoutInflater; // 레이아웃 인플레이터
    private OnItemClickListener listener;
    private int layoutType; // 레이아웃 타입을 구분할 변수
    private ArrayList<IngredientData> selectedIngredients = new ArrayList<>(); // 선택된 재료 리스트
    private Context context; // 액티비티 인스턴스 추가


    public RefrigeratorAdapter(Context context, ArrayList<IngredientData> ingredientsList, int layoutType) {
        this.ingredientsList = ingredientsList; // 생성자에서 재료 리스트 초기화
        this.layoutInflater = LayoutInflater.from(context); // 레이아웃 인플레이터 초기화
        this.layoutType = layoutType;
        this.context = context; // 액티비티 인스턴스 초기화
    }



    // 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // 클릭 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    // ViewHolder 클래스 정의
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName; // viewType 0에서 사용될 TextView
        TextView btnIngredientName; // viewType 1에서 사용될 TextView


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_ingredient.xml에서 TextView 연결
            ingredientName = itemView.findViewById(R.id.ingredientName); // viewType 0
            btnIngredientName = itemView.findViewById(R.id.btn_ingredientName); // viewType 1
        }
    }

    @Override
    public int getItemViewType(int position) {
        return layoutType; // 생성자에서 전달된 layoutType을 반환
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        // viewType에 따라 서로 다른 레이아웃을 인플레이트
        if (viewType == 0) {
            view = layoutInflater.inflate(R.layout.ingredient_item, parent, false); // 냉장고

        } else if (viewType == 1) {
            view = layoutInflater.inflate(R.layout.ingredient_item_btn, parent, false); // 냉장고 재료 기반 레시피 목록

        }
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 현재 재료 데이터를 가져와서 ViewHolder의 TextView에 설정
        if (position >= 0 && position < ingredientsList.size()) {
            IngredientData currentIngredient = ingredientsList.get(position);
//            holder.ingredientName.setText(currentIngredient.ingredientName); // 재료 이름 설정
            if (getItemViewType(position) == 0) {
                holder.ingredientName.setText(currentIngredient.ingredientName);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onItemClick(position); // 아이템 클릭 시 리스너 호출
                        }
                    }
                });
            } else if (getItemViewType(position) == 1) {
                holder.btnIngredientName.setText(currentIngredient.ingredientName);
                // btnIngredientName 클릭 리스너 설정
                holder.btnIngredientName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentIngredient.isSelected = !currentIngredient.isSelected; // 선택 상태 토글
                        if (currentIngredient.isSelected) {
                            selectedIngredients.add(currentIngredient); // 선택된 재료 목록에 추가
                            Log.d("클릭", "Added to selected: " + currentIngredient.ingredientName);
                        } else {
                            selectedIngredients.remove(currentIngredient); // 선택된 재료 목록에서 제거
                            Log.d("클릭", "Removed from selected: " + currentIngredient.ingredientName);
                        }
                        performSearchWithSelectedIngredients(); // 선택된 재료 기반 검색 수행
                        notifyItemChanged(position); // 해당 항목만 업데이트

//                        // 선택된 재료 목록 업데이트
//                        if (currentIngredient.isSelected) {
//                            selectedIngredients.add(currentIngredient); // 목록에 추가
//                            Log.d("클릭", "Added to selected: " + currentIngredient.ingredientName);
//                        } else {
//                            selectedIngredients.remove(currentIngredient); // 목록에서 제거
//                            Log.d("클릭", "Removed from selected: " + currentIngredient.ingredientName);
//                        }
//
//                        Log.d("클릭", "Current selected ingredients: " + selectedIngredients.toString());
//                        notifyItemChanged(); // RecyclerView에 변경 알림
//                        performSearchWithSelectedIngredients();
//                        notifyItemChanged(position); // 데이터 변경 알림
// 선택된 재료로 검색 수행
//                        if (!selectedIngredients.isEmpty()) {
//                            performSearchWithSelectedIngredients();
//                        } else {
//                            Log.d("검색", "Selected ingredients are empty");
//                        }
                    }
                });
            }
            // 선택 상태에 따라 배경 drawable 변경
            if (currentIngredient.isSelected) {
                holder.itemView.setBackgroundResource(R.drawable.edit_color); // 선택된 경우 배경 색상
            } else {
                holder.itemView.setBackgroundResource(R.drawable.edit_round); // 선택되지 않은 경우 기본 drawable
            }
//             클릭 리스너 설정
//            holder.itemView.setOnClickListener(v -> {
//                currentIngredient.isSelected = !currentIngredient.isSelected; // 선택 상태 토글
//                notifyItemChanged(position); // RecyclerView에 변경 알림
//
//                if (listener != null) {
//                    listener.onItemClick(position); // 아이템 클릭 시 리스너 호출
//                }
//            });

            // viewType이 1일 경우에만 색상 변경 및 클릭 리스너 설정
//            if (getItemViewType(position) == 1) {
//                // 선택된 상태에 따라 색상 변경
//                // 선택 상태에 따라 배경색 변경
//                holder.itemView.setBackgroundColor(currentIngredient.isSelected
//                        ? holder.itemView.getContext().getResources().getColor(R.color.main)
//                        : holder.itemView.getContext().getResources().getColor(R.color.white));
//
//
//                // 클릭 리스너 설정
//                holder.itemView.setOnClickListener(v -> {
//                    currentIngredient.isSelected = !currentIngredient.isSelected; // 선택 상태 토글
//                    notifyItemChanged(position); // RecyclerView에 변경 알림
//
//                    if (listener != null) {
//                        listener.onItemClick(position); // 아이템 클릭 시 리스너 호출
//                    }
//                });
//            } else {
//                // 클릭 리스너 설정
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (listener != null) {
//                            listener.onItemClick(position); // 아이템 클릭 시 리스너 호출
//                        }
//                    }
//                });
//            }
//
//        // 클릭 리스너 설정
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (listener != null) {
//                    listener.onItemClick(position); // 아이템 클릭 시 리스너 호출
//                }
//            }
//        });
            Log.d("RefrigeratorAdapter", "Binding item at position: " + position + ", Ingredient: " + currentIngredient.ingredientName);
        }
    }

    private void performSearchWithSelectedIngredients() {
        selectedIngredients.clear();
        StringBuilder selectedNames = new StringBuilder();
        for (IngredientData ingredient : ingredientsList) {
            if (ingredient.isSelected) {
                selectedIngredients.add(ingredient); // 선택된 재료 리스트에 추가
                selectedNames.append(ingredient.ingredientName).append(", ");
            }
        }
        String selectedIngredientNames = selectedNames.length() > 0 ? selectedNames.substring(0, selectedNames.length() - 2) : "None"; // 마지막 쉼표 제거
        Log.d("RefrigeratorAdapter", "Searching with selected ingredients: " + selectedIngredientNames);

        if (context instanceof RefrigeratorRecipe) { // YourActivity를 실제 액티비티 이름으로 변경
            ((RefrigeratorRecipe) context).loadData(); // 액티비티의 loadData() 메서드 호출
        } else {
            Log.e("RefrigeratorAdapter", "Context is not an instance of YourActivity");
        }
    }

    @Override
    public int getItemCount() {
        int count = ingredientsList.size(); // 재료 리스트의 크기 반환
        Log.d("RefrigeratorAdapter", "Item count: " + count);
        return count;
    }

    // 재료 추가 메서드
    public void addIngredient(IngredientData ingredient) {
        ingredientsList.add(ingredient); // 재료 리스트에 추가
        notifyItemInserted(ingredientsList.size() - 1); // RecyclerView에 알림 (최신 추가된 항목)
    }

    // 재료 제거 메서드
    public void removeIngredient(int position) {
        if (position >= 0 && position < ingredientsList.size()) {
            ingredientsList.remove(position); // 리스트에서 제거
            notifyItemRemoved(position); // RecyclerView에 알림 (제거된 항목)
            notifyItemRangeChanged(position, ingredientsList.size()); // 위치 변화 알려주기
        }
    }
}
