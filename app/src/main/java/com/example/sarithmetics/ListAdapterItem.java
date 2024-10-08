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
import java.util.List;

public class ListAdapterItem extends RecyclerView.Adapter<ListAdapterItem.MyViewHolder> {
    private Context context;
    private ArrayList productName, productPrice, productQty;
    private OnItemClickListener listener;
    private List<Product> productList;
    public interface OnItemClickListener {
        void onItemClick(int position, String productName, String productPrice, String productQty);
    }
    ListAdapterItem(Context context , ArrayList productName, ArrayList productPrice, ArrayList productQty, OnItemClickListener listener){
        this.context = context;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQty = productQty;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ListAdapterItem.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_layout_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapterItem.MyViewHolder holder, int position) {
        holder.productNameText.setText(String.valueOf(productName.get(position)));
        holder.productPriceText.setText("₱" + productPrice.get(position));
        holder.productQtyText.setText("Stock: " + productQty.get(position));
        holder.rowListLayout.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(position, String.valueOf(productName.get(position)), String.valueOf(productPrice.get(position)), String.valueOf(productQty.get(position)));
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
            productNameText = itemView.findViewById(R.id.tvItemName);
            productPriceText = itemView.findViewById(R.id.tvItemPrice);
            productQtyText = itemView.findViewById(R.id.tvItemQuantity);
            rowListLayout = itemView.findViewById(R.id.listLayoutItem);
        }

    }
    public void setFilteredList(ArrayList<Product> filteredList){
        //this.productId.clear();
        this.productName.clear();
        this.productPrice.clear();
        this.productQty.clear();
        for(Product p : filteredList){
            //this.productId.add(String.valueOf(p.getProductID()));
            this.productName.add(p.getProductName());
            this.productPrice.add(String.valueOf(p.getProductPrice()));
            this.productQty.add(String.valueOf(p.getProductQty()));
        }
        notifyDataSetChanged();
    }
}
