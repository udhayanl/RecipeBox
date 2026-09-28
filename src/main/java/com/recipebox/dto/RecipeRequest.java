package com.recipebox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class RecipeRequest {

    @NotBlank(message = "Recipe name cannot be empty")
    private String name;

    @NotBlank(message = "Cuisine cannot be empty")
    private String cuisine;

    private String description;

    @NotNull(message = "Preparation time is required")
    @Min(value = 1, message = "Preparation time must be greater than 0")
    private Integer preparationTime;

    @NotBlank(message = "Cooking steps cannot be empty")
    private String cookingSteps;

    private Long userId;

    @Valid
    private List<RecipeIngredientRequest> ingredients = new ArrayList<>();

    public RecipeRequest() {
    }

    public RecipeRequest(String name, String cuisine, String description, Integer preparationTime, String cookingSteps, List<RecipeIngredientRequest> ingredients) {
        this.name = name;
        this.cuisine = cuisine;
        this.description = description;
        this.preparationTime = preparationTime;
        this.cookingSteps = cookingSteps;
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(Integer preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getCookingSteps() {
        return cookingSteps;
    }

    public void setCookingSteps(String cookingSteps) {
        this.cookingSteps = cookingSteps;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<RecipeIngredientRequest> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredientRequest> ingredients) {
        this.ingredients = ingredients;
    }
}
