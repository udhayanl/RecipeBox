package com.recipebox.service;

import com.recipebox.dto.*;
import com.recipebox.entity.MealType;
import com.recipebox.exception.DuplicateResourceException;
import com.recipebox.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RecipeBoxServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private MealPlanService mealPlanService;

    @Autowired
    private ShoppingListService shoppingListService;

    private UserResponse testUser;

    @BeforeEach
    void setUp() {
        // DataInitializer may have run; let's create a fresh user for isolated tests
        UserRequest userRequest = new UserRequest("Tester Chef", "tester@recipebox.com", "secure123");
        testUser = userService.createUser(userRequest);
    }

    @Test
    @DisplayName("1. Create recipe successfully")
    void testCreateRecipeSuccessfully() {
        RecipeRequest request = new RecipeRequest(
                "Paneer Butter Masala",
                "Indian",
                "Rich and creamy curry",
                30,
                "1. Sauté onions. 2. Blend tomatoes. 3. Add paneer cubes.",
                List.of(
                        new RecipeIngredientRequest("Paneer", 250.0, "grams"),
                        new RecipeIngredientRequest("Butter", 50.0, "grams"),
                        new RecipeIngredientRequest("Tomato", 4.0, "pieces")
                )
        );

        RecipeResponse response = recipeService.createRecipe(request, testUser.getId());

        assertNotNull(response.getId());
        assertEquals("Paneer Butter Masala", response.getName());
        assertEquals("Indian", response.getCuisine());
        assertEquals(3, response.getIngredients().size());
        assertEquals(testUser.getId(), response.getUserId());
    }

    @Test
    @DisplayName("2. Get recipe by ID")
    void testGetRecipe() {
        RecipeRequest request = new RecipeRequest(
                "Simple Omelette", "French", "Quick breakfast", 10,
                "Whisk eggs, fry in pan",
                List.of(new RecipeIngredientRequest("Egg", 2.0, "pieces"))
        );
        RecipeResponse created = recipeService.createRecipe(request, testUser.getId());

        RecipeResponse fetched = recipeService.getRecipeById(created.getId());
        assertEquals(created.getId(), fetched.getId());
        assertEquals("Simple Omelette", fetched.getName());
    }

    @Test
    @DisplayName("3. Update recipe")
    void testUpdateRecipe() {
        RecipeRequest request = new RecipeRequest(
                "Original Soup", "Continental", "Plain soup", 15,
                "Boil ingredients",
                List.of(new RecipeIngredientRequest("Water", 500.0, "ml"))
        );
        RecipeResponse created = recipeService.createRecipe(request, testUser.getId());

        RecipeRequest updateReq = new RecipeRequest(
                "Spicy Tomato Soup", "Continental", "Rich and spicy soup", 20,
                "Boil and season well",
                List.of(
                        new RecipeIngredientRequest("Water", 500.0, "ml"),
                        new RecipeIngredientRequest("Tomato", 3.0, "pieces")
                )
        );

        RecipeResponse updated = recipeService.updateRecipe(created.getId(), updateReq, testUser.getId());
        assertEquals("Spicy Tomato Soup", updated.getName());
        assertEquals(20, updated.getPreparationTime());
        assertEquals(2, updated.getIngredients().size());
    }

    @Test
    @DisplayName("4. Delete recipe")
    void testDeleteRecipe() {
        RecipeRequest request = new RecipeRequest(
                "To Delete", "Fast Food", "Temporary", 5, "Discard", List.of()
        );
        RecipeResponse created = recipeService.createRecipe(request, testUser.getId());

        recipeService.deleteRecipe(created.getId(), testUser.getId());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getRecipeById(created.getId()));
    }

    @Test
    @DisplayName("5. Search recipes by cuisine")
    void testSearchByCuisine() {
        RecipeRequest r1 = new RecipeRequest("Tacos", "Mexican", "Street tacos", 15, "Fill tortilla", List.of());
        RecipeRequest r2 = new RecipeRequest("Burrito", "Mexican", "Large wrap", 20, "Roll wrap", List.of());
        RecipeRequest r3 = new RecipeRequest("Sushi", "Japanese", "Fresh roll", 40, "Roll fish", List.of());

        recipeService.createRecipe(r1, testUser.getId());
        recipeService.createRecipe(r2, testUser.getId());
        recipeService.createRecipe(r3, testUser.getId());

        List<RecipeResponse> mexicanRecipes = recipeService.searchRecipes("mexican", null, testUser.getId());
        assertEquals(2, mexicanRecipes.size());
    }

    @Test
    @DisplayName("6. Search recipes by ingredient")
    void testSearchByIngredient() {
        RecipeRequest r1 = new RecipeRequest(
                "Garlic Bread", "Italian", "Toasty", 10, "Bake bread",
                List.of(
                        new RecipeIngredientRequest("Bread", 2.0, "pieces"),
                        new RecipeIngredientRequest("Garlic", 3.0, "pieces")
                )
        );
        RecipeRequest r2 = new RecipeRequest(
                "Cheese Dip", "American", "Creamy", 5, "Melt cheese",
                List.of(new RecipeIngredientRequest("Cheese", 100.0, "grams"))
        );

        recipeService.createRecipe(r1, testUser.getId());
        recipeService.createRecipe(r2, testUser.getId());

        List<RecipeResponse> results = recipeService.searchRecipes(null, "garlic", testUser.getId());
        assertEquals(1, results.size());
        assertEquals("Garlic Bread", results.get(0).getName());
    }

    @Test
    @DisplayName("7. Mark and unmark recipe as favourite")
    void testFavoriteFeature() {
        RecipeRequest request = new RecipeRequest("Salad", "Healthy", "Fresh", 5, "Mix greens", List.of());
        RecipeResponse created = recipeService.createRecipe(request, testUser.getId());
        assertFalse(created.isFavorite());

        RecipeResponse favored = recipeService.setFavorite(created.getId(), true, testUser.getId());
        assertTrue(favored.isFavorite());

        RecipeResponse unfavored = recipeService.setFavorite(created.getId(), false, testUser.getId());
        assertFalse(unfavored.isFavorite());
    }

    @Test
    @DisplayName("8. Create meal plan with valid recipe")
    void testCreateMealPlanWithValidRecipe() {
        RecipeRequest r = new RecipeRequest("Lentil Soup", "Indian", "Healthy dal", 25, "Cook lentils", List.of());
        RecipeResponse recipe = recipeService.createRecipe(r, testUser.getId());

        LocalDate date = LocalDate.of(2026, 10, 1);
        MealPlanRequest request = new MealPlanRequest(date, MealType.LUNCH, recipe.getId(), testUser.getId(), "Workday lunch");

        MealPlanResponse mealPlan = mealPlanService.createMealPlan(request, testUser.getId());

        assertNotNull(mealPlan.getId());
        assertEquals(date, mealPlan.getMealDate());
        assertEquals(MealType.LUNCH, mealPlan.getMealType());
        assertEquals(recipe.getId(), mealPlan.getRecipeId());
    }

    @Test
    @DisplayName("9. CRITICAL BUSINESS RULE: Attempt meal plan with non-existing recipe must be rejected")
    void testCreateMealPlanWithNonExistingRecipeFails() {
        Long nonExistentRecipeId = 999999L;
        MealPlanRequest request = new MealPlanRequest(LocalDate.now(), MealType.DINNER, nonExistentRecipeId, testUser.getId(), "Should fail");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> mealPlanService.createMealPlan(request, testUser.getId())
        );

        assertTrue(exception.getMessage().contains("Recipe with ID " + nonExistentRecipeId + " does not exist"));
    }

    @Test
    @DisplayName("10 & 11. Generate weekly shopping list & aggregate duplicate ingredients correctly")
    void testGenerateWeeklyShoppingListWithAggregation() {
        // Recipe A: Vegetable Pasta uses Pasta (200g), Tomato (3 pieces)
        RecipeRequest pastaReq = new RecipeRequest(
                "Agg Pasta", "Italian", "Pasta", 15, "Cook",
                List.of(
                        new RecipeIngredientRequest("Pasta", 200.0, "grams"),
                        new RecipeIngredientRequest("Tomato", 3.0, "pieces")
                )
        );
        RecipeResponse pastaRecipe = recipeService.createRecipe(pastaReq, testUser.getId());

        // Recipe B: Large Pasta Bake uses Pasta (300g), Tomato (2 pieces)
        RecipeRequest bakeReq = new RecipeRequest(
                "Pasta Bake", "Italian", "Bake", 30, "Bake",
                List.of(
                        new RecipeIngredientRequest("Pasta", 300.0, "grams"),
                        new RecipeIngredientRequest("Tomato", 2.0, "pieces")
                )
        );
        RecipeResponse bakeRecipe = recipeService.createRecipe(bakeReq, testUser.getId());

        // Schedule both within the week
        LocalDate monday = LocalDate.of(2026, 10, 5);
        LocalDate tuesday = LocalDate.of(2026, 10, 6);
        LocalDate sunday = LocalDate.of(2026, 10, 11);

        mealPlanService.createMealPlan(new MealPlanRequest(monday, MealType.LUNCH, pastaRecipe.getId(), testUser.getId(), null), testUser.getId());
        mealPlanService.createMealPlan(new MealPlanRequest(tuesday, MealType.DINNER, bakeRecipe.getId(), testUser.getId(), null), testUser.getId());

        // Generate shopping list
        List<ShoppingListItem> items = shoppingListService.generateShoppingList(testUser.getId(), monday, sunday);

        assertNotNull(items);
        assertFalse(items.isEmpty());

        // Pasta should be: 200g + 300g = 500 grams
        ShoppingListItem pastaItem = items.stream()
                .filter(i -> i.getIngredientName().equalsIgnoreCase("Pasta"))
                .findFirst()
                .orElse(null);
        assertNotNull(pastaItem);
        assertEquals(500.0, pastaItem.getQuantity());
        assertEquals("grams", pastaItem.getUnit());

        // Tomato should be: 3 pieces + 2 pieces = 5 pieces
        ShoppingListItem tomatoItem = items.stream()
                .filter(i -> i.getIngredientName().equalsIgnoreCase("Tomato"))
                .findFirst()
                .orElse(null);
        assertNotNull(tomatoItem);
        assertEquals(5.0, tomatoItem.getQuantity());
        assertEquals("pieces", tomatoItem.getUnit());
    }

    @Test
    @DisplayName("Duplicate user email rejected")
    void testDuplicateUserEmailRejected() {
        UserRequest dupRequest = new UserRequest("Another Tester", "tester@recipebox.com", "pass456");
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(dupRequest));
    }
}
