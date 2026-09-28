package com.recipebox.config;

import com.recipebox.entity.*;
import com.recipebox.repository.IngredientRepository;
import com.recipebox.repository.MealPlanRepository;
import com.recipebox.repository.RecipeRepository;
import com.recipebox.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanRepository mealPlanRepository;

    public DataInitializer(UserRepository userRepository,
                           IngredientRepository ingredientRepository,
                           RecipeRepository recipeRepository,
                           MealPlanRepository mealPlanRepository) {
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.mealPlanRepository = mealPlanRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with data. Skipping initialization.");
            return;
        }

        log.info("Initializing sample data for RecipeBox...");

        // 1. Create 2 Users
        User alice = new User("Chef Alice", "alice@example.com", "password123");
        User bob = new User("Hostel Bob", "bob@example.com", "password123");
        alice = userRepository.save(alice);
        bob = userRepository.save(bob);

        // 2. Create Reusable Ingredients
        String[] ingredientNames = {"Rice", "Pasta", "Tomato", "Onion", "Chicken", "Salt", "Oil", "Bread", "Garlic", "Cheese"};
        Map<String, Ingredient> ingredientMap = new HashMap<>();
        for (String name : ingredientNames) {
            Ingredient ingredient = ingredientRepository.save(new Ingredient(name));
            ingredientMap.put(name, ingredient);
        }

        // 3. Create 4 Recipes
        // Recipe 1: Vegetable Pasta
        Recipe pasta = new Recipe(
                "Vegetable Pasta",
                "Italian",
                "Simple, healthy and delicious vegetable pasta with fresh tomatoes.",
                20,
                "1. Boil pasta in salted water for 10 minutes.\n2. Heat olive oil in a pan, sauté chopped garlic and tomatoes.\n3. Toss pasta into the pan with vegetables and mix thoroughly.",
                alice
        );
        pasta.setFavorite(true);
        pasta.addRecipeIngredient(new RecipeIngredient(pasta, ingredientMap.get("Pasta"), 200.0, "grams"));
        pasta.addRecipeIngredient(new RecipeIngredient(pasta, ingredientMap.get("Tomato"), 3.0, "pieces"));
        pasta.addRecipeIngredient(new RecipeIngredient(pasta, ingredientMap.get("Oil"), 2.0, "tablespoon"));
        pasta.addRecipeIngredient(new RecipeIngredient(pasta, ingredientMap.get("Salt"), 1.0, "teaspoon"));
        pasta = recipeRepository.save(pasta);

        // Recipe 2: Chicken Rice
        Recipe chickenRice = new Recipe(
                "Chicken Rice",
                "Asian",
                "Tender chicken served over aromatic seasoned rice.",
                35,
                "1. Wash and soak rice for 15 minutes.\n2. Sear seasoned chicken chunks in hot oil until golden brown.\n3. Add diced onions, garlic, and rice with broth. Simmer for 20 minutes until cooked.",
                alice
        );
        chickenRice.setFavorite(true);
        chickenRice.addRecipeIngredient(new RecipeIngredient(chickenRice, ingredientMap.get("Rice"), 250.0, "grams"));
        chickenRice.addRecipeIngredient(new RecipeIngredient(chickenRice, ingredientMap.get("Chicken"), 300.0, "grams"));
        chickenRice.addRecipeIngredient(new RecipeIngredient(chickenRice, ingredientMap.get("Onion"), 2.0, "pieces"));
        chickenRice.addRecipeIngredient(new RecipeIngredient(chickenRice, ingredientMap.get("Oil"), 2.0, "tablespoon"));
        chickenRice.addRecipeIngredient(new RecipeIngredient(chickenRice, ingredientMap.get("Salt"), 1.0, "teaspoon"));
        chickenRice = recipeRepository.save(chickenRice);

        // Recipe 3: Tomato Sandwich
        Recipe sandwich = new Recipe(
                "Tomato Sandwich",
                "American",
                "Quick hostel-friendly fresh tomato and cheese sandwich.",
                10,
                "1. Toast bread slices lightly.\n2. Layer thin tomato slices and cheese.\n3. Sprinkle with a pinch of salt and grill for 3 minutes.",
                bob
        );
        sandwich.setFavorite(false);
        sandwich.addRecipeIngredient(new RecipeIngredient(sandwich, ingredientMap.get("Bread"), 4.0, "pieces"));
        sandwich.addRecipeIngredient(new RecipeIngredient(sandwich, ingredientMap.get("Tomato"), 2.0, "pieces"));
        sandwich.addRecipeIngredient(new RecipeIngredient(sandwich, ingredientMap.get("Cheese"), 2.0, "pieces"));
        sandwich.addRecipeIngredient(new RecipeIngredient(sandwich, ingredientMap.get("Salt"), 0.5, "teaspoon"));
        sandwich = recipeRepository.save(sandwich);

        // Recipe 4: Vegetable Fried Rice
        Recipe friedRice = new Recipe(
                "Vegetable Fried Rice",
                "Asian",
                "Classic stir-fried rice with onion, garlic, and savory seasoning.",
                25,
                "1. Cook rice and let it cool completely.\n2. Heat oil in a wok on high heat. Add minced garlic and sliced onions.\n3. Stir in cold rice and salt. Toss vigorously until fragrant.",
                bob
        );
        friedRice.setFavorite(false);
        friedRice.addRecipeIngredient(new RecipeIngredient(friedRice, ingredientMap.get("Rice"), 200.0, "grams"));
        friedRice.addRecipeIngredient(new RecipeIngredient(friedRice, ingredientMap.get("Onion"), 1.0, "pieces"));
        friedRice.addRecipeIngredient(new RecipeIngredient(friedRice, ingredientMap.get("Oil"), 1.0, "tablespoon"));
        friedRice.addRecipeIngredient(new RecipeIngredient(friedRice, ingredientMap.get("Salt"), 1.0, "teaspoon"));
        friedRice = recipeRepository.save(friedRice);

        // 4. Create 5 Meal Plans
        LocalDate today = LocalDate.now();
        mealPlanRepository.save(new MealPlan(today, MealType.LUNCH, alice, pasta, "Prep before team call"));
        mealPlanRepository.save(new MealPlan(today, MealType.DINNER, alice, chickenRice, "High protein meal"));
        mealPlanRepository.save(new MealPlan(today.plusDays(1), MealType.LUNCH, alice, pasta, "Leftovers from yesterday"));
        mealPlanRepository.save(new MealPlan(today.plusDays(1), MealType.BREAKFAST, bob, sandwich, "Quick breakfast before class"));
        mealPlanRepository.save(new MealPlan(today.plusDays(2), MealType.DINNER, bob, friedRice, "Hostel dinner with roommates"));

        log.info("Sample data initialization completed successfully! 2 users, 10 ingredients, 4 recipes, 5 meal plans loaded.");
    }
}
