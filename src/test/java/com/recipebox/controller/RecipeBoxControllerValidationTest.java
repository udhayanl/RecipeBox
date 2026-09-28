package com.recipebox.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.recipebox.dto.MealPlanRequest;
import com.recipebox.dto.RecipeIngredientRequest;
import com.recipebox.dto.RecipeRequest;
import com.recipebox.dto.UserRequest;
import com.recipebox.entity.MealType;
import com.recipebox.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class RecipeBoxControllerValidationTest {

    @Autowired
    private UserController userController;

    @Autowired
    private RecipeController recipeController;

    @Autowired
    private MealPlanController mealPlanController;

    @Autowired
    private ShoppingListController shoppingListController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(
                        userController,
                        recipeController,
                        mealPlanController,
                        shoppingListController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("12. Reject invalid quantities (Quantity <= 0)")
    void testRejectInvalidQuantity() throws Exception {
        RecipeRequest invalidRequest = new RecipeRequest(
                "Invalid Recipe", "Fusion", "Desc", 15, "Steps",
                List.of(new RecipeIngredientRequest("Sugar", -5.0, "grams"))
        );

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("13. Reject invalid email format")
    void testRejectInvalidEmail() throws Exception {
        UserRequest invalidUser = new UserRequest("Bad Email User", "not-a-valid-email", "secret123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"));
    }

    @Test
    @DisplayName("14. Reject missing required fields (Empty Recipe Name)")
    void testRejectMissingRecipeName() throws Exception {
        RecipeRequest missingName = new RecipeRequest(
                "", "Italian", "Desc", 20, "Steps", List.of()
        );

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").value("Recipe name cannot be empty"));
    }

    @Test
    @DisplayName("Reject missing required fields (Preparation time <= 0)")
    void testRejectZeroPreparationTime() throws Exception {
        RecipeRequest invalidPrep = new RecipeRequest(
                "Fast Soup", "Italian", "Desc", 0, "Steps", List.of()
        );

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPrep)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.preparationTime").value("Preparation time must be greater than 0"));
    }

    @Test
    @DisplayName("Reject meal plan with non-existing recipe via HTTP returns 404")
    void testMealPlanNonExistingRecipeReturns404() throws Exception {
        MealPlanRequest request = new MealPlanRequest(LocalDate.now(), MealType.LUNCH, 999999L);

        mockMvc.perform(post("/api/meal-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recipe with ID 999999 does not exist"));
    }

    @Test
    @DisplayName("GET /api/shopping-list returns 200 OK")
    void testGetShoppingListEndpoint() throws Exception {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(7);

        mockMvc.perform(get("/api/shopping-list")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
