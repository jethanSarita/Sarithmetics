package com.example.sarithmetics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ListAdapterCategoryFirebase extends FirebaseRecyclerAdapter<String, ListAdapterCategoryFirebase.myViewHolder> {

    public ListAdapterCategoryFirebase(@NonNull FirebaseRecyclerOptions<String> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull String model) {
        holder.category_name.setText(model);
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_category, parent, false);
        return new myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder {

        TextView category_name;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            category_name = itemView.findViewById(R.id.list_category_name);
        }
    }
}
