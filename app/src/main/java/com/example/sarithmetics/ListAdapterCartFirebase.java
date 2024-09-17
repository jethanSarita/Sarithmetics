package com.example.sarithmetics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ListAdapterCartFirebase extends FirebaseRecyclerAdapter<Item, ListAdapterCartFirebase.myViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    private OnItemClickListener listener;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private User user;
    private Context context;
    public ListAdapterCartFirebase(@NonNull FirebaseRecyclerOptions<Item> options, OnItemClickListener listener, User user) {
        super(options);
        this.listener = listener;
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        this.user = user;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Item item);
    }

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterCartFirebase.myViewHolder holder, int position, @NonNull Item model) {
        holder.item_name.setText(model.getName());
        holder.item_price.setText("₱" + model.getPrice());
        holder.item_quantity.setText("Stock: " + model.getQuantity());
        holder.rowListLayout.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(position, model);
            }
        });
    }

    @NonNull
    @Override
    public ListAdapterCartFirebase.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_layout_item, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_price, item_quantity;
        LinearLayout rowListLayout;
        Spinner categories_spinner;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.tvItemName);
            item_price = itemView.findViewById(R.id.tvItemPrice);
            item_quantity = itemView.findViewById(R.id.tvItemQuantity);
            rowListLayout = itemView.findViewById(R.id.listLayoutItem);
            categories_spinner = itemView.findViewById(R.id.main_popup_category_spinner);
        }
    }
}
