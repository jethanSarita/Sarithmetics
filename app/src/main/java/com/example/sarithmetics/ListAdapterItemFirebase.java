package com.example.sarithmetics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ListAdapterItemFirebase extends FirebaseRecyclerAdapter<Item, ListAdapterItemFirebase.myViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    private OnItemClickListener listener;
    private Context context;
    private boolean lock_marked;

    public ListAdapterItemFirebase(@NonNull FirebaseRecyclerOptions<Item> options, OnItemClickListener listener, boolean lock_marked) {
        super(options);
        this.listener = listener;
        this.lock_marked = lock_marked;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Item item, int type);
    }

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterItemFirebase.myViewHolder holder, int position, @NonNull Item model) {

        int type;

        holder.productNameText.setText(model.getName());
        holder.productPriceText.setText("₱" + model.getPrice());
        holder.productQtyText.setText("Stock: " + model.getQuantity());

        if (lock_marked && model.isSub_marked()) {
            holder.rowListLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_list_locked));
            holder.productNameText.setTextColor(ContextCompat.getColor(context, R.color.graySecondary));
            holder.productPriceText.setTextColor(ContextCompat.getColor(context, R.color.graySecondary));
            holder.productQtyText.setTextColor(ContextCompat.getColor(context, R.color.graySecondary));
            type = 1;
        } else {
            type = 0;
        }

        holder.rowListLayout.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(position, model, type);
            }
        });
    }

    @NonNull
    @Override
    public ListAdapterItemFirebase.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_layout_item, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView productNameText, productPriceText, productQtyText;
        LinearLayout rowListLayout;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.tvItemName);
            productPriceText = itemView.findViewById(R.id.tvItemPrice);
            productQtyText = itemView.findViewById(R.id.tvItemQuantity);
            rowListLayout = itemView.findViewById(R.id.listLayoutItem);
        }
    }
}
