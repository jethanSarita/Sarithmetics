package com.example.sarithmetics;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

public class ListAdapterCategoryFirebase extends FirebaseRecyclerAdapter<Category, ListAdapterCategoryFirebase.myViewHolder> {

    private int selectedItem = -1;

    private OnItemClickListener listener;

    public ListAdapterCategoryFirebase(@NonNull FirebaseRecyclerOptions<Category> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Category model, DatabaseReference ref,int type);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Category model) {
        holder.category_name.setText(model.getName());

        DatabaseReference ref = getRef(position);

        if (position == selectedItem) {
            ObjectAnimator containerScale = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1.2f);
            ObjectAnimator textScale = ObjectAnimator.ofFloat(holder.category_name, "scaleY", 0.8f);
            containerScale.setDuration(100);
            textScale.setDuration(100);
            containerScale.start();
            textScale.start();
            holder.category_name.setTextColor(Color.parseColor("#4681f4"));
            if (listener != null) {
                listener.onItemClick(position, model, ref, 0);
            }
        } else {
            ObjectAnimator containerScale = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1.0f);
            ObjectAnimator textScale = ObjectAnimator.ofFloat(holder.category_name, "scaleY", 1.0f);
            containerScale.setDuration(100);
            textScale.setDuration(100);
            containerScale.start();
            textScale.start();
            holder.category_name.setTextColor(Color.parseColor("#FFFFFF"));
        }

        holder.itemView.setOnClickListener(view -> {
            int previousItem = selectedItem;

            selectedItem = holder.getBindingAdapterPosition();

            if (previousItem != -1) {
                notifyItemChanged(previousItem);
            }

            if (selectedItem == previousItem) {
                selectedItem = -1;
                if (listener != null) {
                    listener.onItemClick(position, model, ref, 2);
                }
            }

            notifyItemChanged(selectedItem);
        });

        holder.itemView.setOnLongClickListener(view -> {
            selectedItem = -1;

            notifyItemChanged(holder.getBindingAdapterPosition());

            if (listener != null) {
                listener.onItemClick(position, model, ref, 1);
            }

            if (listener != null) {
                listener.onItemClick(position, model, ref, 2);
            }
            return true;
        });
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
