package com.example.sarithmetics;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class EmployeeAdapter extends FirebaseRecyclerAdapter<User, EmployeeAdapter.myViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    private OnItemClickListener listener;

    public EmployeeAdapter(@NonNull FirebaseRecyclerOptions<User> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull User model) {
        if (model.getUser_type() != 1) {
            holder.tvEmployeeName.setText(model.getFirst_name() + " " + model.getLast_name());
            switch (model.getStatus()) {
                case 0:
                    holder.tvEmployeeStatus.setText("Pending approval");
                    holder.tvEmployeeStatus.setBackgroundColor(Color.YELLOW);
                    break;
                case 1:
                    holder.tvEmployeeStatus.setText("Inactive");
                    holder.tvEmployeeStatus.setBackgroundColor(Color.GRAY);
                    break;
                case 2:
                    holder.tvEmployeeStatus.setText("Active");
                    holder.tvEmployeeStatus.setBackgroundColor(Color.GRAY);
                    break;
            }
            switch (model.getUser_type()) {
                case 0:
                    holder.tvEmployeeType.setText("Employee");
                    break;
                case 2:
                    holder.tvEmployeeType.setText("Inventory Employee");
                    break;
            }
            holder.employee_layout.setOnClickListener(view -> {
                if (listener != null) {
                    if (model.getStatus() == 0) {
                        listener.onEmployeeClick(position, model, true);
                    } else {
                        listener.onEmployeeClick(position, model, false);
                    }
                }
            });
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_list_employee_layout, parent, false);
        return new EmployeeAdapter.myViewHolder(view);
    }

    public interface OnItemClickListener {
        void onEmployeeClick(int position, User user, boolean pending_approval);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvEmployeeType, tvEmployeeStatus;
        LinearLayout employee_layout;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeType = itemView.findViewById(R.id.tvEmployeeType);
            tvEmployeeStatus = itemView.findViewById(R.id.tvEmployeeStatus);
            employee_layout = itemView.findViewById(R.id.row_list_employee_layout);
        }
    }
}
