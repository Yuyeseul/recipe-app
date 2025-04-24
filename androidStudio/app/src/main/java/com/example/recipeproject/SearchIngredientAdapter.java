package com.example.recipeproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class SearchIngredientAdapter extends RecyclerView.Adapter<SearchIngredientAdapter.SearchViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String ingredient);
    }

    private List<String> searchResults;
    private OnItemClickListener clickListener;

    public SearchIngredientAdapter(List<String> searchResults, OnItemClickListener clickListener) {
        this.searchResults = searchResults;
        this.clickListener = clickListener;
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewResult;

        public SearchViewHolder(@NonNull View itemView, final OnItemClickListener clickListener, final List<String> searchResults) {
            super(itemView);
            textViewResult = itemView.findViewById(R.id.searchResult);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onItemClick(searchResults.get(position));
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        return new SearchViewHolder(v, clickListener, searchResults);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        String currentResult = searchResults.get(position);
        holder.textViewResult.setText(currentResult);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }
}