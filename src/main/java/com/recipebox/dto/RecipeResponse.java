package com.recipebox.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecipeResponse {

    private Long id;
    private String name;
    private String cuisine;
    private String description;
    private Integer preparationTime;
    private String cookingSteps;
    private boolean favorite;
    private Long userId;
    private String userName;
    private List<RecipeIngredientResponse> ingredients = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecipeResponse() {
    }

    public RecipeResponse(Long id, String name, String cuisine, String description, Integer preparationTime,
                          String cookingSteps, boolean favorite, Long userId, String userName,
                          List<RecipeIngredientResponse> ingredients, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.description = description;
        this.preparationTime = preparationTime;
        this.cookingSteps = cookingSteps;
        this.favorite = favorite;
        this.userId = userId;
        this.userName = userName;
        this.ingredients = ingredients;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<RecipeIngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
