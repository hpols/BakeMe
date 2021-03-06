package com.example.android.bakeme.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.bakeme.data.Recipe;
import com.example.android.bakeme.data.Recipe.Ingredients;
import com.example.android.bakeme.data.Recipe.Steps;
import com.example.android.bakeme.data.db.RecipeContract.IngredientsEntry;
import com.example.android.bakeme.data.db.RecipeContract.RecipeEntry;
import com.example.android.bakeme.data.db.RecipeContract.StepsEntry;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Methods enabling the storing and retrieving of the recipe information.
 */
public class RecipeUtils {

    //Loader constants
    public static final int RECIPE_DETAIL_LOADER = 21;
    public static final int INGREDIENTS_DETAIL_LOADER = 22;
    public static final int STEPS_DETAIL_LOADER = 23;
    public static final int RECIPE_MAIN_LOADER = 11;
    public static final int STEPS_METHOD_LOADER = 33;

    public static final String INGREDIENT_LIST = "ingredient list";
    public static final String STEP_LIST = "step list";
    public static final String RECIPE_BUNDLE = "recipe bundle";
    public static final String SELECTED_STEP = "selected step";
    public static final String SELECTED_RECIPE = "selected_recipe";
    public static final String RECIPE_KEY = "recipe_key";

    //Flag constants
    public static final int FAV_UPDATED_FLAG = 222;
    public static final int FAV_UNCHANGED_FLAG = 111;

    private static boolean favIsUpdated;
    private static String currentRecipeName;


    //keep track of when the user (un)favourites a recipe
    public static boolean isFavIsUpdated() {
        return favIsUpdated;
    }

    public static void setFavIsUpdated(boolean favIsUpdated) {
        RecipeUtils.favIsUpdated = favIsUpdated;
    }

    public static void setCurrentRecipeName(String currentRecipeName) {
        RecipeUtils.currentRecipeName = currentRecipeName;
    }

    // --Commented out by Inspection (28.04.18, 21:36):private static String currentRecipeName;

    // –––––– Insert the data to the respective tables ––––––

    /** recipe
     *
     * @param recipes obtained from the api
     * @param ctxt of the activity calling this method
     */
    public static void writeRecipesToDb(List<Recipe> recipes, Context ctxt) {
        ContentValues singleRecipe = new ContentValues();

        for (int i = 0; i < recipes.size(); i++) {
            Recipe receivedRecipe = recipes.get(i);

            receivedRecipe.setFavourited(false); //initially all recipes are unfavourited.
            singleRecipe.put(RecipeEntry.RECIPE_ID, receivedRecipe.getId());
            singleRecipe.put(RecipeEntry.RECIPE_IMAGE, receivedRecipe.getImage());
            singleRecipe.put(RecipeEntry.RECIPE_NAME, receivedRecipe.getName());
            singleRecipe.put(RecipeEntry.RECIPE_SERVINGS, receivedRecipe.getServings());

            int favValue = getCheckValue(receivedRecipe.isFavourited(), RecipeEntry.FAVOURITED_TRUE,
                    RecipeEntry.FAVOURITED_FALSE);
            singleRecipe.put(RecipeEntry.RECIPE_FAVOURITED, favValue);

            ctxt.getContentResolver().insert(RecipeEntry.CONTENT_URI_RECIPE, singleRecipe);
        }
    }

    /** ingredients
     *
     * @param ingredientsList obtained from the api
     * @param recipeName the recipe associated with the ingredients being stored
     * @param ctxt of the activity calling this method
     */
    public static void writeIngredientsToDb(ArrayList<Ingredients> ingredientsList,
                                            String recipeName, Context ctxt) {
        ContentValues setOfIngredients = new ContentValues();

        for (int i = 0; i < ingredientsList.size(); i++) {
            Ingredients receivedIngredients = ingredientsList.get(i);

            receivedIngredients.setChecked(false); //initially all ingredients are unchecked
            setOfIngredients.put(IngredientsEntry.INGREDIENTS_INGREDIENT, receivedIngredients
                    .getIngredient());
            setOfIngredients.put(IngredientsEntry.INGREDIENTS_MEASURE,
                    receivedIngredients.getMeasure());
            setOfIngredients.put(IngredientsEntry.INGREDIENTS_QUANTITY, receivedIngredients
                    .getQuantity());
            setOfIngredients.put(IngredientsEntry.INGREDIENTS_ASSOCIATED_RECIPE,
                    recipeName);

            int checkValue = getCheckValue(receivedIngredients.isChecked(),
                    IngredientsEntry.CHECKED_TRUE, IngredientsEntry.CHECKED_FALSE);
            setOfIngredients.put(IngredientsEntry.INGREDIENTS_CHECKED, checkValue);

            ctxt.getContentResolver().insert(IngredientsEntry.CONTENT_URI_INGREDIENTS,
                    setOfIngredients);
        }
    }

    /** steps
     *
     * @param stepsList obtained from the api
     * @param recipeName the recipe associated with the ingredients being stored
     * @param ctxt of the activity calling this method
     */
    public static void writeStepsToDb(ArrayList<Steps> stepsList, String recipeName,
                                      Context ctxt) {
        ContentValues setOfSteps = new ContentValues();

        for (int i = 0; i < stepsList.size(); i++) {
            Steps receivedSteps = stepsList.get(i);

            setOfSteps.put(Steps.STEPS_ID, receivedSteps.getId());
            setOfSteps.put(Steps.STEPS_THUMB, receivedSteps.getThumbnail());
            setOfSteps.put(Steps.STEPS_VIDEO, receivedSteps.getVideo());
            setOfSteps.put(Steps.STEPS_SHORT_DESCRIP,
                    receivedSteps.getShortDescription());
            setOfSteps.put(Steps.STEPS_DESCRIP, receivedSteps.getDescription());
            setOfSteps.put(Steps.STEPS_ASSOCIATED_RECIPE, recipeName);

            ctxt.getContentResolver().insert(StepsEntry.CONTENT_URI_STEPS, setOfSteps);
        }
    }

    // –––––– updating the data in the respective tables ––––––

    /** Recipe
     *
     * @param selectedRecipe the recipe to be updated
     * @param ctxt of the activity calling this method
     */
    public static void updateFavDb(Recipe selectedRecipe, Context ctxt) {
        //create uri referencing the recipe's id as well as the selection arguments.
        Uri uri = ContentUris.withAppendedId(RecipeEntry.CONTENT_URI_RECIPE,
                selectedRecipe.getId());
        Timber.v("uri: " + uri);

        //store changed favourite selection to the db.
        ContentValues singleRecipe = new ContentValues();
        singleRecipe.put(RecipeEntry.RECIPE_FAVOURITED, getCheckValue(selectedRecipe.isFavourited(),
                RecipeEntry.FAVOURITED_TRUE, RecipeEntry.FAVOURITED_FALSE));
        //getRecipeValues(singleRecipe, selectedRecipe);
        ctxt.getContentResolver().update(uri, singleRecipe, null, null);
    }

    /** Ingredients
     * @param selectedIngredient the selected ingredient to be updated
     * @param ctxt of the activity calling this method
     */
    public static void updateCheckedDb(Ingredients selectedIngredient,
                                       Context ctxt) {
        //create uri referencing the ingredient's id as well as the selection arguments.
        Uri uri = ContentUris.withAppendedId(IngredientsEntry.CONTENT_URI_INGREDIENTS,
                selectedIngredient.getId());

        //store changed checked state to the db.
        ContentValues singleIngredient = new ContentValues();
        singleIngredient.put(IngredientsEntry.INGREDIENTS_CHECKED,
                getCheckValue(selectedIngredient.isChecked(), IngredientsEntry.CHECKED_TRUE,
                        IngredientsEntry.CHECKED_FALSE));
        //getIngredientValues(recipeName, singleIngredient, selectedIngredient);
        ctxt.getContentResolver().update(uri, singleIngredient, null, null);
    }

    //get the corresponding int value for the boolean
    private static int getCheckValue(boolean favourited, int favouritedTrue, int favouritedFalse) {
        int favValue; //convert boolean to int value for db
        if (favourited) {
            favValue = favouritedTrue;
        } else {
            favValue = favouritedFalse;
        }
        return favValue;
    }

    // –––––– Getting the data from the cursor ––––––

    /** Recipe
     *
     * @param data is the cursor from which the data will be extracted
     * @param recipeList will hold the data for the adapter
     */
    public static void getRecipeList(Cursor data, ArrayList<Recipe> recipeList) {
        while (data.moveToNext()) {
            int id = data.getInt(data.getColumnIndex(RecipeEntry.RECIPE_ID));
            String image = data.getString((data.getColumnIndex(RecipeEntry.RECIPE_IMAGE)));
            String name = data.getString(data.getColumnIndex(RecipeEntry.RECIPE_NAME));
            int servings = data.getInt(data.getColumnIndex(RecipeEntry.RECIPE_SERVINGS));
            int favValue = data.getInt(data.getColumnIndex(RecipeEntry.RECIPE_FAVOURITED));
            boolean favourited = favValue == RecipeEntry.FAVOURITED_TRUE;
            recipeList.add(new Recipe(id, image, name, servings, favourited));
        }
    }

    /** Ingredients
     *
     * @param data is the cursor from which the data will be extracted
     * @param ingredientsList will hold the data for the adapter
     * @return the ingredientsList for further processing by the adapter.
     */
    public static void getIngredientList(Cursor data,
                                         ArrayList<Ingredients> ingredientsList) {
        while (data.moveToNext()) {
            long id = data.getLong(data.getColumnIndex(IngredientsEntry.INGREDIENTS_ID));
            String ingredient = data.getString(data.getColumnIndex(IngredientsEntry
                    .INGREDIENTS_INGREDIENT));
            String measure = data.getString(data.getColumnIndex(IngredientsEntry
                    .INGREDIENTS_MEASURE));
            int quantity = data.getInt(data.getColumnIndex(IngredientsEntry
                    .INGREDIENTS_QUANTITY));
            boolean checked = data.getInt(data.getColumnIndex(IngredientsEntry
                    .INGREDIENTS_CHECKED)) == IngredientsEntry.CHECKED_TRUE;
            ingredientsList.add(new Ingredients(id, ingredient, measure, quantity,
                    checked));
        }
    }

    /** Steps
     *
     * @param data is the cursor from which the data will be extracted
     * @param stepsList will hold the data for the adapter
     * @return the stepsList for further processing by the adapter.
     */
    public static void getStepsList(Cursor data, ArrayList<Steps> stepsList) {
        while (data.moveToNext()) {
            long id = data.getLong(data.getColumnIndex(StepsEntry.STEPS_ID));
            String shortDescrip
                    = data.getString(data.getColumnIndex(StepsEntry.STEPS_SHORT_DESCRIP));
            String descrip
                    = data.getString(data.getColumnIndex(StepsEntry.STEPS_DESCRIP));
            String video = data.getString(data.getColumnIndex(StepsEntry.STEPS_VIDEO));
            String thumb = data.getString(data.getColumnIndex(StepsEntry.STEPS_THUMB));
            stepsList.add(new Steps(id, shortDescrip, descrip, video, thumb));
        }
    }
}