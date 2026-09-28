package com.recipebox.controller.web;

import com.recipebox.dto.RecipeIngredientRequest;
import com.recipebox.dto.RecipeRequest;
import com.recipebox.service.RecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class RecipeBoxWebViewControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    public RecipeBoxWebViewControllerTest(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Web 1: Dashboard page loads with stats and view name")
    void testDashboardView() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/dashboard"))
                .andExpect(model().attributeExists("totalRecipes"))
                .andExpect(model().attributeExists("upcomingMeals"))
                .andExpect(model().attributeExists("favoriteRecipes"));
    }

    @Test
    @DisplayName("Web 2: Root path redirects to /dashboard")
    void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Web 3: Recipe list view loads with search and filters")
    void testRecipesView() throws Exception {
        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/recipes"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attributeExists("cuisines"));
    }

    @Test
    @DisplayName("Web 4: Recipe Add page loads with form data")
    void testRecipeAddFormView() throws Exception {
        mockMvc.perform(get("/recipes/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/recipe-form"))
                .andExpect(model().attributeExists("recipeForm"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @DisplayName("Web 5: Recipe Details page loads for existing recipe")
    void testRecipeDetailsView() throws Exception {
        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/recipe-details"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attributeExists("stepsList"));
    }

    @Test
    @DisplayName("Web 6: Favorites page loads")
    void testFavoritesView() throws Exception {
        mockMvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/favorites"))
                .andExpect(model().attributeExists("favorites"));
    }

    @Test
    @DisplayName("Web 7: Meal Planner page loads with calendar days and slots")
    void testMealPlannerView() throws Exception {
        mockMvc.perform(get("/meal-planner"))
                .andExpect(status().isOk())
                .andExpect(view().name("meal-planner/meal-planner"))
                .andExpect(model().attributeExists("days"))
                .andExpect(model().attributeExists("mealTypes"))
                .andExpect(model().attributeExists("mealGrid"));
    }

    @Test
    @DisplayName("Web 8: Add Meal Plan with non-existent recipe ID triggers validation error")
    void testAddMealPlanInvalidRecipeFails() throws Exception {
        mockMvc.perform(post("/meal-planner/add")
                        .param("recipeId", "999999")
                        .param("mealDate", LocalDate.now().toString())
                        .param("mealType", "DINNER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Web 9: Shopping List page loads with date range and aggregated items")
    void testShoppingListView() throws Exception {
        mockMvc.perform(get("/shopping-list"))
                .andExpect(status().isOk())
                .andExpect(view().name("shopping/shopping-list"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("startDate"))
                .andExpect(model().attributeExists("endDate"));
    }

    @Test
    @DisplayName("Web 10: Auth pages (login, register, profile) load successfully")
    void testAuthPages() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attributeExists("user"));
    }
}
