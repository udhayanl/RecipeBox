package com.recipebox.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class RecipeFormData {

    private Long id;

    @NotBlank(message = "Recipe name cannot be empty")
    private String name;

    @NotBlank(message = "Cuisine cannot be empty")
    private String cuisine;

    private String description;

    @NotNull(message = "Preparation time is required")
    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    private Integer preparationTime = 30;

    @NotBlank(message = "Cooking steps cannot be empty")
    private String cookingSteps;

    private boolean favorite;

    private List<RecipeIngredientRequest> ingredients = new ArrayList<>();

    public RecipeFormData() {
        // Initialize with 3 empty ingredient slots for clean UX on new recipe
        this.ingredients.add(new RecipeIngredientRequest("", 1.0, "units"));
    }

    public static RecipeFormData fromResponse(RecipeResponse res) {
        RecipeFormData form = new RecipeFormData();
        form.setId(res.getId());
        form.setName(res.getName());
        form.setCuisine(res.getCuisine());
        form.setDescription(res.getDescription());
        form.setPreparationTime(res.getPreparationTime());
        form.setCookingSteps(res.getCookingSteps());
        form.setFavorite(res.isFavorite());

        List<RecipeIngredientRequest> ingReqs = new ArrayList<>();
        if (res.getIngredients() != null && !res.getIngredients().isEmpty()) {
            for (RecipeIngredientResponse r : res.getIngredients()) {
                ingReqs.add(new RecipeIngredientRequest(r.getIngredientName(), r.getQuantity(), r.getUnit()));
            }
        } else {
            ingReqs.add(new RecipeIngredientRequest("", 1.0, "units"));
        }
        form.setIngredients(ingReqs);
        return form;
    }

    public RecipeRequest toRequest(Long userId) {
        List<RecipeIngredientRequest> validIngredients = new ArrayList<>();
        if (this.ingredients != null) {
            for (RecipeIngredientRequest item : this.ingredients) {
                if (item.getIngredientName() != null && !item.getIngredientName().trim().isEmpty()) {
                    validIngredients.add(new RecipeIngredientRequest(
                            item.getIngredientName().trim(),
                            item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1.0,
                            item.getUnit() != null && !item.getUnit().trim().isEmpty() ? item.getUnit().trim() : "units"
                    ));
                }
            }
        }
        RecipeRequest req = new RecipeRequest(
                this.name,
                this.cuisine,
                this.description,
                this.preparationTime,
                this.cookingSteps,
                validIngredients
        );
        req.setUserId(userId);
        return req;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public List<RecipeIngredientRequest> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredientRequest> ingredients) {
        this.ingredients = ingredients;
    }
}
