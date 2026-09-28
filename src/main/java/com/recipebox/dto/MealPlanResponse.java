package com.recipebox.dto;

import com.recipebox.entity.MealType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MealPlanResponse {

    private Long id;
    private LocalDate mealDate;
    private MealType mealType;
    private Long userId;
    private String userName;
    private Long recipeId;
    private String recipeName;
    private String recipeCuisine;
    private String notes;
    private LocalDateTime createdAt;

    public MealPlanResponse() {
    }

    public MealPlanResponse(Long id, LocalDate mealDate, MealType mealType, Long userId, String userName,
                            Long recipeId, String recipeName, String recipeCuisine, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.userId = userId;
        this.userName = userName;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.recipeCuisine = recipeCuisine;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
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

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeCuisine() {
        return recipeCuisine;
    }

    public void setRecipeCuisine(String recipeCuisine) {
        this.recipeCuisine = recipeCuisine;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
