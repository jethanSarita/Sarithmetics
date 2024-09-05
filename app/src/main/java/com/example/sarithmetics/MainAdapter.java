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

public class MainAdapter extends FirebaseRecyclerAdapter<Item,MainAdapter.myViewHolder> {

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
    public MainAdapter(@NonNull FirebaseRecyclerOptions<Item> options, OnItemClickListener listener, User user) {
        super(options);
        this.listener = listener;
        firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        this.user = user;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String productName, String productPrice, String productQty);
    }

    @Override
    protected void onBindViewHolder(@NonNull MainAdapter.myViewHolder holder, int position, @NonNull Item model) {
        holder.productNameText.setText(model.getName());
        holder.productPriceText.setText("₱" + model.getPrice());
        holder.productQtyText.setText("Stock: " + model.getQuantity());
        holder.rowListLayout.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(position, String.valueOf(model.getName()), String.valueOf(model.getPrice()), String.valueOf(model.getQuantity()));
            }
        });
        /*firebaseDatabaseHelper.getItemsCategories(user.getBusiness_code()).get().addOnCompleteListener(task -> {
            ArrayList<String> categories = new ArrayList<>();
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    for (DataSnapshot currSnapshot : snapshot.getChildren()) {
                        categories.add(currSnapshot.getValue(String.class));
                    }
                }
            }
            ArrayAdapter<String> adp = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, categories);
            holder.categories_spinner.setAdapter(adp);
        });*/
    }

    @NonNull
    @Override
    public MainAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_layout_item, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView productNameText, productPriceText, productQtyText;
        LinearLayout rowListLayout;
        Spinner categories_spinner;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.tvEmployeeName);
            productPriceText = itemView.findViewById(R.id.tvProductPrice);
            productQtyText = itemView.findViewById(R.id.tvEmployeeType);
            rowListLayout = itemView.findViewById(R.id.rowListLayoutID);
            categories_spinner = itemView.findViewById(R.id.main_popup_category_spinner);
        }
    }
}
