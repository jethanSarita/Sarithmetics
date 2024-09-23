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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ListAdapterHistoryFirebase extends FirebaseRecyclerAdapter<MyTransaction, ListAdapterHistoryFirebase.myViewHolder> {

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
    public ListAdapterHistoryFirebase(@NonNull FirebaseRecyclerOptions<MyTransaction> options, ListAdapterHistoryFirebase.OnItemClickListener listener, User user) {
        super(options);
        this.listener = listener;
        this.firebaseDatabaseHelper = new FirebaseDatabaseHelper();
        this.user = user;
    }

    public interface OnItemClickListener {
        void onHistoryItemClick(int position, MyTransaction model);
    }

    @Override
    protected void onBindViewHolder(@NonNull ListAdapterHistoryFirebase.myViewHolder holder, int position, @NonNull MyTransaction model) {

        Date value = new Date(((Number)model.getTransaction_date()).longValue());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String date = sdf1.format(value);
        String time = sdf2.format(value);

        holder.tvHistoryDate.setText(date);
        holder.tvHistoryTime.setText(time);
        holder.tvHistoryID.setText(getRef(position).getKey());
        //employee name here
        holder.tvHistoryTotal.setText(String.valueOf(model.getSubtotal()));
        holder.tvHistoryType.setText(model.isIs_out() ? "Out" : "In");

        holder.listLayoutHistory.setOnClickListener(view ->{
            if (listener != null) {
                listener.onHistoryItemClick(position, model);
            }
        });
    }

    @NonNull
    @Override
    public ListAdapterHistoryFirebase.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_layout_history, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        LinearLayout listLayoutHistory;
        TextView tvHistoryDate, tvHistoryTime, tvHistoryID, tvHistoryEmployeeName, tvHistoryTotal, tvHistoryType;

        public myViewHolder(@NonNull View view) {
            super(view);

            listLayoutHistory = view.findViewById(R.id.listLayoutHistory);

            tvHistoryDate = view.findViewById(R.id.tvHistoryDate);
            tvHistoryTime = view.findViewById(R.id.tvHistoryTime);
            tvHistoryID = view.findViewById(R.id.tvHistoryID);
            tvHistoryEmployeeName = view.findViewById(R.id.tvHistoryEmployeeName);
            tvHistoryTotal = view.findViewById(R.id.tvHistoryTotal);
            tvHistoryType = view.findViewById(R.id.tvHistoryType);
        }
    }
}

