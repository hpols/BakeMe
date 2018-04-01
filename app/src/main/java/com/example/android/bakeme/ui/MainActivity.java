package com.example.android.bakeme.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.example.android.bakeme.R;
import com.example.android.bakeme.RecipeIdlingResource;
import com.example.android.bakeme.data.Recipe;
import com.example.android.bakeme.data.adapter.RecipeCardAdapter;
import com.example.android.bakeme.data.api.ApiClient;
import com.example.android.bakeme.data.api.ApiInterface;
import com.example.android.bakeme.data.db.RecipeDatabase;
import com.example.android.bakeme.data.db.RecipeProvider;
import com.example.android.bakeme.databinding.ActivityMainBinding;
import com.example.android.bakeme.utils.RecipeUtils;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.example.android.bakeme.data.Recipe.*;

public class MainActivity extends AppCompatActivity implements RecipeCardAdapter.RecipeClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    ActivityMainBinding mainBinder;
    RecipeCardAdapter recipeCardAdapter;
    ArrayList<Recipe> recipeList;

    // Idling resource for testing purposes only
    @Nullable
    private RecipeIdlingResource idlingResource;

    /**
     * Only called from test, creates and returns a new {@link RecipeIdlingResource}.
     */
    @VisibleForTesting
    @NonNull
    public RecipeIdlingResource getIdlingResource() {
        if (idlingResource == null) {
            idlingResource = new RecipeIdlingResource();
        }
        return idlingResource;
    }

    private static int RECIPE_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinder = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Timber.plant(new Timber.DebugTree());

        if (recipeList == null) {
            getSupportLoaderManager().initLoader(RECIPE_LOADER, null,
                    MainActivity.this);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(String
                .valueOf(R.string.RECIPE_KEY))) {
            mainBinder.alertView.progressPb.setVisibility(View.GONE);
            mainBinder.alertView.alertTv.setVisibility(View.GONE);
            recipeList = savedInstanceState.getParcelableArrayList(String
                    .valueOf(R.string.RECIPE_KEY));
            if (recipeList != null) {
                setAdapter(this, recipeList, this);
            }
        }
    }

    //Make sure we have internet before we load the data.
    private void checkNetworkAndLoadData() {
        ConnectivityManager connectMan = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectMan != null;
        NetworkInfo netInfo = connectMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            mainBinder.alertView.alertTv.setVisibility(View.GONE);
            mainBinder.alertView.progressPb.setVisibility(View.VISIBLE);

            RecipeUtils.retrieveRecipes(this, mainBinder.alertView.progressPb,
                    mainBinder.alertView.alertTv);

            getSupportLoaderManager().initLoader(RECIPE_LOADER, null,
                    MainActivity.this);
        }
    }

    //build the adapter and with it the RecyclerView to display the data.
    private void setAdapter(Context ctxt, ArrayList<Recipe> recipeList,
                            RecipeCardAdapter.RecipeClickHandler clicker) {
        mainBinder.alertView.progressPb.setVisibility(View.GONE);
        //set up adapter and RecyclerView.
        recipeCardAdapter = new RecipeCardAdapter(ctxt, recipeList, clicker);
        mainBinder.recipeOverviewRv.setAdapter(recipeCardAdapter);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mainBinder.recipeOverviewRv.setLayoutManager(new LinearLayoutManager(this));
        } else {
            mainBinder.recipeOverviewRv.setLayoutManager(new GridLayoutManager(this, 2));
        }

        recipeCardAdapter.notifyDataSetChanged();
    }

    /**
     * Start DetailActivity with recipe selected
     *
     * @param selectedRecipe Is the selected recipe to be passed to the activity
     */
    @Override
    public void onClick(Recipe selectedRecipe) {
        Intent openDetailActivity = new Intent(this, DetailActivity.class);
        openDetailActivity.putExtra(String.valueOf(R.string.SELECTED_RECIPE), selectedRecipe);

        startActivity(openDetailActivity);
    }

    /**
     * Keep hold of the data
     *
     * @param outState is the bundle which holds the data for the activity to reuse
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(String.valueOf(R.string.RECIPE_KEY), recipeList);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[]{
                RECIPE_ID,
                RECIPE_IMAGE,
                RECIPE_NAME,
                RECIPE_SERVINGS,
                RECIPE_FAVOURITED};
        return new CursorLoader(this, RecipeProvider.CONTENT_URI_RECIPE, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        recipeList = new ArrayList<>();
        if (data == null) {
            mainBinder.alertView.alertTv.setText(R.string.no_recipes);
        } else {
            while (data.moveToNext()) {
                int id = data.getInt(data.getColumnIndex(RECIPE_ID));
                String image = data.getString((data.getColumnIndex(RECIPE_IMAGE)));
                String name = data.getString(data.getColumnIndex(RECIPE_NAME));
                int servings = data.getInt(data.getColumnIndex(RECIPE_SERVINGS));
                int favourited = data.getInt(data.getColumnIndex(RECIPE_FAVOURITED));
                recipeList.add(new Recipe(id, image, name, servings, favourited));
            }
            data.close();
            setAdapter(this, recipeList, this);
            Timber.v("recipeList: %s", recipeList.size());
        }
        if (recipeList.size() == 0) {
            checkNetworkAndLoadData();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // no action on reset required, as it is not used.
    }
}