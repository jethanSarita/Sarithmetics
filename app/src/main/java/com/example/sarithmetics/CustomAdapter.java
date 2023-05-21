package com.example.sarithmetics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private Context context;
    private ArrayList productId, productName, productPrice, productQty;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    CustomAdapter(Context context, ArrayList productId, ArrayList productName, ArrayList productPrice, ArrayList productQty){
        this.context = context;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQty = productQty;
    }
    @NonNull
    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rowlistlayout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {
        holder.productNameText.setText(String.valueOf(productName.get(position)));
        holder.productPriceText.setText(String.valueOf(productPrice.get(position)));
        holder.productQtyText.setText(String.valueOf(productQty.get(position)));
        holder.rowListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return productName.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView productNameText, productPriceText, productQtyText;
        LinearLayout rowListLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.tvProductName);
            productPriceText = itemView.findViewById(R.id.tvProductPrice);
            productQtyText = itemView.findViewById(R.id.tvProductQty);
            rowListLayout = itemView.findViewById(R.id.rowListLayoutID);
        }

    }
}
