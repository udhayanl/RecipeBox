package com.recipebox.dto;

import jakarta.validation.constraints.NotBlank;

public class IngredientRequest {

    @NotBlank(message = "Ingredient name cannot be empty")
    private String name;

    public IngredientRequest() {
    }

    public IngredientRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
