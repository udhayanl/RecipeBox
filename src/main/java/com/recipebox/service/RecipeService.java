package com.recipebox.service;

import com.recipebox.dto.*;
import com.recipebox.entity.Ingredient;
import com.recipebox.entity.Recipe;
import com.recipebox.entity.RecipeIngredient;
import com.recipebox.entity.User;
import com.recipebox.exception.ResourceNotFoundException;
import com.recipebox.repository.MealPlanRepository;
import com.recipebox.repository.RecipeIngredientRepository;
import com.recipebox.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final MealPlanRepository mealPlanRepository;
    private final UserService userService;
    private final IngredientService ingredientService;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         MealPlanRepository mealPlanRepository,
                         UserService userService,
                         IngredientService ingredientService) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.userService = userService;
        this.ingredientService = ingredientService;
    }

    public RecipeResponse createRecipe(RecipeRequest request, Long authenticatedUserId) {
        Long targetUserId = request.getUserId() != null ? request.getUserId() : authenticatedUserId;
        if (targetUserId == null) {
            targetUserId = 1L; // default fallback if single-user testing
        }
        User user = userService.getUserEntity(targetUserId);

        Recipe recipe = new Recipe();
        recipe.setName(request.getName().trim());
        recipe.setCuisine(request.getCuisine().trim());
        recipe.setDescription(request.getDescription());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setCookingSteps(request.getCookingSteps().trim());
        recipe.setUser(user);
        recipe.setFavorite(false);

        if (request.getIngredients() != null) {
            for (RecipeIngredientRequest itemReq : request.getIngredients()) {
                Ingredient ingredient = ingredientService.getOrCreateIngredient(itemReq.getIngredientName());
                RecipeIngredient ri = new RecipeIngredient(recipe, ingredient, itemReq.getQuantity(), itemReq.getUnit().trim());
                recipe.addRecipeIngredient(ri);
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToResponse(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> getAllRecipes(Long userId) {
        List<Recipe> recipes;
        if (userId != null) {
            recipes = recipeRepository.findByUserId(userId);
        } else {
            recipes = recipeRepository.findAll();
        }
        return recipes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = getRecipeEntity(id);
        return mapToResponse(recipe);
    }

    @Transactional(readOnly = true)
    public Recipe getRecipeEntity(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with ID " + id + " does not exist"));
    }

    public RecipeResponse updateRecipe(Long id, RecipeRequest request, Long authenticatedUserId) {
        Recipe recipe = getRecipeEntity(id);

        if (authenticatedUserId != null && recipe.getUser() != null && !recipe.getUser().getId().equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only modify your own recipes");
        }

        recipe.setName(request.getName().trim());
        recipe.setCuisine(request.getCuisine().trim());
        recipe.setDescription(request.getDescription());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setCookingSteps(request.getCookingSteps().trim());

        if (request.getIngredients() != null) {
            recipe.getRecipeIngredients().clear();
            for (RecipeIngredientRequest itemReq : request.getIngredients()) {
                Ingredient ingredient = ingredientService.getOrCreateIngredient(itemReq.getIngredientName());
                RecipeIngredient ri = new RecipeIngredient(recipe, ingredient, itemReq.getQuantity(), itemReq.getUnit().trim());
                recipe.addRecipeIngredient(ri);
            }
        }

        Recipe updated = recipeRepository.save(recipe);
        return mapToResponse(updated);
    }

    public void deleteRecipe(Long id, Long authenticatedUserId) {
        Recipe recipe = getRecipeEntity(id);

        if (authenticatedUserId != null && recipe.getUser() != null && !recipe.getUser().getId().equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only delete your own recipes");
        }

        // Before deleting, handle any MealPlan references appropriately to avoid leaving invalid references.
        mealPlanRepository.deleteByRecipeId(id);

        recipeRepository.delete(recipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> searchRecipes(String cuisine, String ingredient, Long userId) {
        List<Recipe> results;

        if (cuisine != null && !cuisine.trim().isEmpty() && ingredient != null && !ingredient.trim().isEmpty()) {
            List<Recipe> byCuisine = (userId != null)
                    ? recipeRepository.findByUserIdAndCuisineIgnoreCase(userId, cuisine.trim())
                    : recipeRepository.findByCuisineIgnoreCase(cuisine.trim());

            String searchIng = ingredient.trim().toLowerCase();
            results = byCuisine.stream()
                    .filter(r -> r.getRecipeIngredients().stream()
                            .anyMatch(ri -> ri.getIngredient().getName().toLowerCase().contains(searchIng)))
                    .collect(Collectors.toList());
        } else if (cuisine != null && !cuisine.trim().isEmpty()) {
            results = (userId != null)
                    ? recipeRepository.findByUserIdAndCuisineIgnoreCase(userId, cuisine.trim())
                    : recipeRepository.findByCuisineIgnoreCase(cuisine.trim());
        } else if (ingredient != null && !ingredient.trim().isEmpty()) {
            results = (userId != null)
                    ? recipeRepository.searchByUserIdAndIngredient(userId, ingredient.trim())
                    : recipeRepository.searchByIngredient(ingredient.trim());
        } else {
            results = (userId != null)
                    ? recipeRepository.findByUserId(userId)
                    : recipeRepository.findAll();
        }

        return results.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public RecipeResponse setFavorite(Long id, boolean favorite, Long authenticatedUserId) {
        Recipe recipe = getRecipeEntity(id);

        if (authenticatedUserId != null && recipe.getUser() != null && !recipe.getUser().getId().equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only modify your own recipe's favorite status");
        }

        recipe.setFavorite(favorite);
        Recipe updated = recipeRepository.save(recipe);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> getFavorites(Long userId) {
        List<Recipe> favorites = (userId != null)
                ? recipeRepository.findByUserIdAndFavoriteTrue(userId)
                : recipeRepository.findByFavoriteTrue();

        return favorites.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public RecipeResponse mapToResponse(Recipe recipe) {
        List<RecipeIngredientResponse> ingredientResponses = new ArrayList<>();
        if (recipe.getRecipeIngredients() != null) {
            for (RecipeIngredient ri : recipe.getRecipeIngredients()) {
                ingredientResponses.add(new RecipeIngredientResponse(
                        ri.getId(),
                        ri.getIngredient() != null ? ri.getIngredient().getId() : null,
                        ri.getIngredient() != null ? ri.getIngredient().getName() : null,
                        ri.getQuantity(),
                        ri.getUnit()
                ));
            }
        }

        return new RecipeResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getCuisine(),
                recipe.getDescription(),
                recipe.getPreparationTime(),
                recipe.getCookingSteps(),
                recipe.isFavorite(),
                recipe.getUser() != null ? recipe.getUser().getId() : null,
                recipe.getUser() != null ? recipe.getUser().getName() : null,
                ingredientResponses,
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }
}
