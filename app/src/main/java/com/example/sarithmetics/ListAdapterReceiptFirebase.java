package com.example.sarithmetics;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
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

import org.w3c.dom.Text;

public class ListAdapterReceiptFirebase extends FirebaseRecyclerAdapter<Item, ListAdapterReceiptFirebase.myViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    //private OnItemClickListener listener;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private User user;
    private Context context;
    public ListAdapterReceiptFirebase(@NonNull FirebaseRecyclerOptions<Item> options, User user) {
        super(options);
        //this.listener = listener;
        this.firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        this.user = user;
    }

    /*public interface OnItemClickListener {
        void onRestockItemClick(int position, Item model);
    }*/

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterReceiptFirebase.myViewHolder holder, int position, @NonNull Item model) {
        holder.receipt_list_item_quantity_name.setText(model.getQuantity() + " " + model.getName());
        holder.receipt_list_item_total_cost.setText("₱" + (model.getPrice() * model.getQuantity()));
    }

    @NonNull
    @Override
    public ListAdapterReceiptFirebase.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_layout_receipt, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView receipt_list_item_quantity_name, receipt_list_item_total_cost;

        public myViewHolder(@NonNull View receiptView) {
            super(receiptView);
            receipt_list_item_quantity_name = receiptView.findViewById(R.id.receipt_list_item_quantity_name);
            receipt_list_item_total_cost = receiptView.findViewById(R.id.receipt_list_item_total_cost);
        }
    }
}

