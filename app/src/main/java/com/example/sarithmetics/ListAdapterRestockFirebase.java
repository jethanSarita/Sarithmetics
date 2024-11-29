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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

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
        void onRestockItemClick(int position, int type, Item model);
    }

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterRestockFirebase.myViewHolder holder, int position, @NonNull Item model) {
        holder.bindData(model);

        holder.item_cost_price.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRestockItemClick(position, 0, model);
            }
        });

        holder.restock_number.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRestockItemClick(position, 1, model);
            }
        });

        holder.button_plus.setOnClickListener(view -> {
            holder.incrementRestock(model);
        });

        holder.button_minus.setOnClickListener(view -> {
            holder.decrementRestock(model);
        });

        /*int initial_delay = 500;
        int repeat_delay = 100;

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
        DatabaseReference items_ref = firebaseDatabaseHelper.getItemsRef(user.getBusiness_code());

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
            item_quantity.setText("" + item.getQuantity());
            item_selling_price.setText("₱" + item.getPrice());
            item_total_cost.setText("₱" + (item.getCost_price() * item.getRestock_quantity()));
            restock_number.setText(String.valueOf(item.getRestock_quantity()));

            if (item.getCost_price() == 0.0) {
                item_cost_price.setText("CLICK TO SET");
            } else {
                item_cost_price.setText("₱" + item.getCost_price());
            }
        }

        public void incrementRestock(Item item) {
            items_ref.child(item.getName()).child("restock_quantity").setValue(ServerValue.increment(1));
            /*current_set_stock++;
            item_total_cost.setText("₱" + (item.getPrice() * item.getCost_price()));
            restock_number.setText(String.valueOf(current_set_stock));*/
        }

        public void decrementRestock(Item item) {
            items_ref.child(item.getName()).child("restock_quantity").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int current_quantity = snapshot.getValue(Integer.class);
                        if (current_quantity > 0) {
                            items_ref.child(item.getName()).child("restock_quantity").setValue(ServerValue.increment(-1));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
           /* current_set_stock--;

            if (current_set_stock <= -1) {
                current_set_stock = 0;
            }

            item_total_cost.setText("₱" + (item.getPrice() * current_set_stock));
            restock_number.setText(String.valueOf(current_set_stock));*/
        }
    }
}
