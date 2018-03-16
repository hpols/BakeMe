package com.example.android.bakeme.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakeme.R;
import com.example.android.bakeme.data.Recipe;
import com.example.android.bakeme.data.adapter.IngredientAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import timber.log.Timber;

/**
 * {@link OverviewFragment} is a {@link Fragment} offering the needed ingredients and steps for the
 * selected recipe.
 */
public class OverviewFragment extends Fragment {

    Recipe selectedRecipe;

    // lists for the recipe in question.
    ArrayList<Recipe.Ingredients> ingredientsList;
    ArrayList<Recipe.Steps> stepsList;

    //Adapters for displaying the ingredients and steps recipe in question.
    IngredientAdapter ingredientAdapter;

    //views
    @BindView(R.id.ingredient_rv)
    RecyclerView ingredientRv;

    public OverviewFragment() {
        //required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.overview_fragment, container, false);

        Timber.v("ingredients: %s", ingredientsList);

        //Setup Ingredient adapter
        if (ingredientsList != null) {
            ingredientAdapter = new IngredientAdapter(getActivity(), ingredientsList);
            ingredientRv.setAdapter(ingredientAdapter); //TODO: in debugger – step over causes error here
            ingredientRv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            ingredientAdapter.notifyDataSetChanged();
        }
        return root;
    }

    public void setIngredientsList(ArrayList<Recipe.Ingredients> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    public void setStepsList(ArrayList<Recipe.Steps> stepsList) {
        this.stepsList = stepsList;
    }
}
