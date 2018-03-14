package com.example.android.bakeme.data.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakeme.R;
import com.example.android.bakeme.data.Recipe;
import com.example.android.bakeme.data.Recipe.Ingredients;

import java.util.ArrayList;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link IngredientAdapter} is a {@link RecyclerView.Adapter} to display the ingredients for the
 * recipe in question.
 */
public class IngredientAdapter
        extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    Context ctxt;
    ArrayList<Ingredients> measurements;

    public IngredientAdapter(Context ctxt, ArrayList<Ingredients> measurements) {
        this.ctxt = ctxt;
        this.measurements = measurements;
    }

    @Override
    public IngredientAdapter.IngredientViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
        View root = LayoutInflater.from(ctxt).inflate(R.layout.ingredient_item, parent,
                false);
        root.setFocusable(true);
        return new IngredientViewHolder(root);
    }

    @Override
    public void onBindViewHolder(IngredientAdapter.IngredientViewHolder holder, int position) {
        Ingredients currentItem = this.measurements.get(position);

        holder.ingredientTv.setText(currentItem.toString());

    }

    @Override
    public int getItemCount() {
        if (measurements == null) return 0;
        else return measurements.size();
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ingredient_tv)
        TextView ingredientTv;

        public IngredientViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}