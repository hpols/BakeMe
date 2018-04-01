package com.example.android.bakeme.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.android.bakeme.R;
import com.example.android.bakeme.data.Recipe;
import com.example.android.bakeme.data.adapter.IngredientAdapter;
import com.example.android.bakeme.data.adapter.StepAdapter;
import com.example.android.bakeme.utils.RecipeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * {@link OverviewFragment} is a {@link Fragment} offering the needed ingredients and steps for the
 * selected recipe.
 */
public class OverviewFragment extends Fragment {

    // lists for the recipe in question.
    ArrayList<Recipe.Ingredients> ingredientsList;
    ArrayList<Recipe.Steps> stepsList;
    boolean isFavourited;
    Recipe selectedRecipe;

    //Adapters for displaying the ingredients and steps of the recipe in question.
    IngredientAdapter ingredientAdapter;
    IngredientAdapter.IngredientViewHolder ingredientViewHolder;
    StepAdapter stepAdapter;

    //views
    @BindView(R.id.ingredient_rv)
    RecyclerView ingredientRv;
    @BindView(R.id.recipe_steps_rv)
    RecyclerView stepRv;

    @BindView(R.id.fav_button_ib)
    ImageButton favButtonIb;

    public OverviewFragment() {
        //required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.bind(this, root);

        Timber.v("ingredients: %s", ingredientsList);
        Timber.v("steps: %s", stepsList);

        //Setup Ingredient adapter
        if (ingredientsList != null) {
            ingredientAdapter = new IngredientAdapter(getActivity(), ingredientsList);
            ingredientRv.setAdapter(ingredientAdapter);
            ingredientRv.setLayoutManager(new StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL));
            ingredientAdapter.notifyDataSetChanged();
        }

        if (stepsList != null) {
            stepAdapter = new StepAdapter((DetailActivity) getActivity());
            stepAdapter.setData(getContext(), stepsList);
            stepRv.setAdapter(stepAdapter);
            stepRv.setLayoutManager(new LinearLayoutManager(getActivity()));
            stepAdapter.notifyDataSetChanged();
        }

        RecipeUtils.setfavButton(isFavourited, favButtonIb, getActivity());
        favButtonIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavourited) {
                    selectedRecipe.setFavourited(R.integer.not_favourited);
                    isFavourited = false;
                    ingredientAdapter.setOfferCheckBoxes(false);
                } else {
                    selectedRecipe.setFavourited(R.integer.is_favourited);
                    isFavourited = true;
                    ingredientAdapter.setOfferCheckBoxes(true);
                }
                ingredientAdapter.notifyDataSetChanged();
                RecipeUtils.setfavButton(isFavourited, favButtonIb, getActivity());
                RecipeUtils.updateFavDb(selectedRecipe, getActivity());
            }
        });
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(String.valueOf(R.string.STEP_LIST), stepsList);
        outState.putParcelableArrayList(String.valueOf(R.string.INGREDIENT_LIST), ingredientsList);
        super.onSaveInstanceState(outState);
    }

    public void setIngredientsList(ArrayList<Recipe.Ingredients> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    public void setStepsList(ArrayList<Recipe.Steps> stepsList) {
        this.stepsList = stepsList;
    }

    public void setFavourited(boolean favourited) {
        isFavourited = favourited;
    }

    public void setSelectedRecipe(Recipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }
}
