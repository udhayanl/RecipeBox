package com.recipebox.controller;

import com.recipebox.dto.RecipeRequest;
import com.recipebox.dto.RecipeResponse;
import com.recipebox.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipe Management", description = "Endpoints for creating, managing, and searching recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    @Operation(summary = "Create recipe", description = "Creates a new recipe with ingredients, quantities, and steps")
    public ResponseEntity<RecipeResponse> createRecipe(
            @Valid @RequestBody RecipeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        RecipeResponse created = recipeService.createRecipe(request, headerUserId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all recipes", description = "Returns all recipes, optionally filtered by user ID")
    public ResponseEntity<List<RecipeResponse>> getAllRecipes(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long targetUserId = userId != null ? userId : headerUserId;
        return ResponseEntity.ok(recipeService.getAllRecipes(targetUserId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by ID", description = "Returns full details for a single recipe by its ID")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipe", description = "Updates an existing recipe and its ingredients")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request, headerUserId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recipe", description = "Deletes a recipe and safely handles meal plan references")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        recipeService.deleteRecipe(id, headerUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes", description = "Search recipes by cuisine and/or ingredient name")
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long targetUserId = userId != null ? userId : headerUserId;
        return ResponseEntity.ok(recipeService.searchRecipes(cuisine, ingredient, targetUserId));
    }

    @PutMapping("/{id}/favorite")
    @Operation(summary = "Mark recipe as favorite", description = "Flags a recipe as favorite")
    public ResponseEntity<RecipeResponse> markFavorite(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        return ResponseEntity.ok(recipeService.setFavorite(id, true, headerUserId));
    }

    @PutMapping("/{id}/unfavorite")
    @Operation(summary = "Unmark recipe as favorite", description = "Removes favorite flag from a recipe")
    public ResponseEntity<RecipeResponse> unmarkFavorite(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        return ResponseEntity.ok(recipeService.setFavorite(id, false, headerUserId));
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get favorite recipes", description = "Lists all recipes marked as favorite")
    public ResponseEntity<List<RecipeResponse>> getFavorites(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long targetUserId = userId != null ? userId : headerUserId;
        return ResponseEntity.ok(recipeService.getFavorites(targetUserId));
    }
}
