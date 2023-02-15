package ru.samsung.case2022.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

import ru.samsung.case2022.R;
import ru.samsung.case2022.objects.Product;
import ru.samsung.case2022.ui.EditActivity;
import ru.samsung.case2022.ui.RootActivity;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private ArrayList<Product> products;
    private Context context;

    public ListAdapter(ArrayList<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutId = R.layout.product_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {

        private TextView productName;
        private View card;
        private TextView count;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            productName = itemView.findViewById(R.id.productName);
            count = itemView.findViewById(R.id.count);
        }

        public void bind(Product product) {
            productName.setText(product.getName());
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RootActivity activity = (RootActivity) context;
                    activity.startEditLauncher(product);
                }
            });
            count.setText("x" + String.valueOf(product.getCount()));
        }
    }
}
