package com.richfield.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that binds pantry rows from the database to item_pantry.xml.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Callbacks so the Activity decides what happens on click/delete. */
    public interface Listener {
        void onItemClick(Ingredient item);
        void onDeleteClick(Ingredient item);
    }

    private static final int COLOR_WARNING = 0xFFE65100;
    private static final int COLOR_NORMAL = 0xFF757575;

    private final Listener listener;
    private List<Ingredient> items = new ArrayList<>();
    private boolean highlightExpiring = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Ingredient> newItems, boolean highlightExpiring) {
        this.items = newItems;
        this.highlightExpiring = highlightExpiring;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Ingredient item = items.get(position);
        h.tvName.setText(item.getName());
        h.tvDetails.setText(Formats.qty(item.getQuantity()) + " " + item.getUnit());

        Integer days = ExpiryUtils.daysUntil(item.getExpiryDate());
        if (days == null) {
            h.tvExpiry.setVisibility(View.GONE);
        } else {
            h.tvExpiry.setVisibility(View.VISIBLE);
            String text;
            if (days < 0) {
                text = "Expired " + (-days) + " day(s) ago";
            } else if (days == 0) {
                text = "Expires today";
            } else {
                text = "Expires in " + days + " day(s) (" + item.getExpiryDate() + ")";
            }
            h.tvExpiry.setText(text);
            boolean soon = highlightExpiring && days <= ExpiryUtils.SOON_DAYS;
            h.tvExpiry.setTextColor(soon ? COLOR_WARNING : COLOR_NORMAL);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(item));
        h.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvDetails, tvExpiry;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDetails = itemView.findViewById(R.id.tv_details);
            tvExpiry = itemView.findViewById(R.id.tv_expiry);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
