package com.example.sarithmetics;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ListAdapterRestockFirebase extends FirebaseRecyclerAdapter<Item, ListAdapterRestockFirebase.myViewHolder> {

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
    public ListAdapterRestockFirebase(@NonNull FirebaseRecyclerOptions<Item> options, OnItemClickListener listener, User user) {
        super(options);
        this.listener = listener;
        this.firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        this.user = user;
    }

    public interface OnItemClickListener {
        void onRestockItemClick(int position, Item model);
        void onRestockMinusButtonClick(int position, Item model);
        void onRestockPlusButtonClick(int position, Item model);
    }

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterRestockFirebase.myViewHolder holder, int position, @NonNull Item model) {
        holder.bindData(model);


        holder.button_plus.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRestockPlusButtonClick(position, model);
            }
        });
    }

    @NonNull
    @Override
    public ListAdapterRestockFirebase.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_layout_restocking, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_quantity, item_selling_price, item_cost_price, item_total_cost;
        EditText restock_number;
        ImageButton button_minus, button_plus;
        LinearLayout list_layout_restock;
        int current_set_stock = 0;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.tvRestockItemName);
            item_quantity = itemView.findViewById(R.id.tvRestockItemQuantity);
            item_selling_price = itemView.findViewById(R.id.tvRestockItemSellingPrice);
            item_cost_price = itemView.findViewById(R.id.tvRestockItemCostPrice);
            item_total_cost = itemView.findViewById(R.id.tvRestockItemTotalCost);
            restock_number = itemView.findViewById(R.id.tvRestockNumberSet);
            button_minus = itemView.findViewById(R.id.imgBtnRestockMinus);
            button_plus = itemView.findViewById(R.id.imgBtnRestockPlus);
            list_layout_restock = itemView.findViewById(R.id.listLayoutRestock);

            restock_number.setText(current_set_stock);

            button_plus.setOnClickListener(view -> {
                current_set_stock++;

                restock_number.setText(current_set_stock);
            });

            button_minus.setOnClickListener(view -> {
                current_set_stock--;

                if (current_set_stock <= -1) {
                    current_set_stock = 0;
                }

                restock_number.setText(current_set_stock);
            });

            restock_number.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    current_set_stock = Integer.parseInt(s.toString());
                    restock_number.setText(current_set_stock);
                }
            });
        }

        public void bindData(Item item) {
            item_name.setText(item.getName());
            item_quantity.setText("Stock: " + item.getQuantity());
            item_selling_price.setText("₱" + item.getPrice());

            if (item.getCostPrice() == 0.0) {
                item_cost_price.setText("CLICK TO SET");
            } else {
                item_cost_price.setText("₱" + item.getCostPrice());
            }
        }
    }
}
