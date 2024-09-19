package com.example.sarithmetics;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    }

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterRestockFirebase.myViewHolder holder, int position, @NonNull Item model) {
        int initial_delay = 500;
        int repeat_delay = 100;

        holder.bindData(model);

        Handler handler = new Handler();

        Runnable incrementRunnable = new Runnable() {
            @Override
            public void run() {
                holder.incrementRestock(model);
                handler.postDelayed(this, repeat_delay);
            }
        };

        Runnable decrementRunnable = new Runnable() {
            @Override
            public void run() {
                holder.decrementRestock(model);
                handler.postDelayed(this, repeat_delay);
            }
        };

        holder.button_plus.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: // When button is pressed
                    holder.incrementRestock(model);
                    handler.postDelayed(incrementRunnable, initial_delay);
                    return true;

                case MotionEvent.ACTION_UP: // When button is released
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(incrementRunnable);
                    return true;
            }
            return false;
        });

        holder.button_minus.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: // When button is pressed
                    holder.decrementRestock(model);
                    handler.postDelayed(decrementRunnable, initial_delay);
                    return true;

                case MotionEvent.ACTION_UP: // When button is released
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(decrementRunnable);
                    return true;
            }
            return false;
        });

        holder.list_layout_restock.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRestockItemClick(position, model);
            }
        });

        /*holder.button_plus.setOnClickListener(view -> {
            holder.current_set_stock++;
            holder.item_total_cost.setText("₱" + (model.getPrice() * holder.current_set_stock));
            holder.restock_number.setText(String.valueOf(holder.current_set_stock));
        });

        holder.button_minus.setOnClickListener(view -> {
            holder.current_set_stock--;

            if (holder.current_set_stock <= -1) {
                holder.current_set_stock = 0;
            }

            holder.item_total_cost.setText("₱" + (model.getPrice() * holder.current_set_stock));
            holder.restock_number.setText(String.valueOf(holder.current_set_stock));
        });*/

        /*holder.button_plus.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRestockPlusButtonClick(position, model);
            }
        });*/
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
        TextView restock_number;
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
        }

        public void bindData(Item item) {
            item_name.setText(item.getName());
            item_quantity.setText("Stock: " + item.getQuantity());
            item_selling_price.setText("₱" + item.getPrice());
            item_total_cost.setText("₱" + (item.getPrice() * current_set_stock));

            if (item.getCostPrice() == 0.0) {
                item_cost_price.setText("Cost Price: CLICK TO SET");
            } else {
                item_cost_price.setText("Cost: ₱" +  + item.getCostPrice());
            }
        }

        public void incrementRestock(Item item) {
            current_set_stock++;
            item_total_cost.setText("₱" + (item.getPrice() * current_set_stock));
            restock_number.setText(String.valueOf(current_set_stock));
        }

        public void decrementRestock(Item item) {
            current_set_stock--;

            if (current_set_stock <= -1) {
                current_set_stock = 0;
            }

            item_total_cost.setText("₱" + (item.getPrice() * current_set_stock));
            restock_number.setText(String.valueOf(current_set_stock));
        }
    }
}
