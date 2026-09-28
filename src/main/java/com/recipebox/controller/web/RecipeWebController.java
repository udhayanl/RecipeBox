package com.recipebox.controller.web;

import com.recipebox.dto.RecipeFormData;
import com.recipebox.dto.RecipeRequest;
import com.recipebox.dto.RecipeResponse;
import com.recipebox.service.RecipeService;
import com.recipebox.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RecipeWebController extends BaseWebController {

    private final RecipeService recipeService;

    public RecipeWebController(UserService userService, RecipeService recipeService) {
        super(userService);
        this.recipeService = recipeService;
    }

    @GetMapping("/recipes")
    public String listRecipes(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String cuisine,
                              @RequestParam(required = false) String ingredient,
                              Model model,
                              HttpSession session) {
        populateCommonModel(model, session, "recipes");
        Long userId = getAuthenticatedUserId(session);

        List<RecipeResponse> recipes;
        if (search != null && !search.trim().isEmpty()) {
            // Search in both cuisine or ingredients
            recipes = recipeService.searchRecipes(search, search, userId);
            if (recipes.isEmpty()) {
                // Also search directly across all user recipes matching name
                String query = search.trim().toLowerCase();
                recipes = recipeService.getAllRecipes(userId).stream()
                        .filter(r -> r.getName().toLowerCase().contains(query)
                                || r.getCuisine().toLowerCase().contains(query)
                                || (r.getDescription() != null && r.getDescription().toLowerCase().contains(query))
                                || r.getIngredients().stream().anyMatch(i -> i.getIngredientName().toLowerCase().contains(query)))
                        .collect(Collectors.toList());
            }
        } else if ((cuisine != null && !cuisine.trim().isEmpty() && !cuisine.equalsIgnoreCase("All"))
                || (ingredient != null && !ingredient.trim().isEmpty())) {
            String filterCuisine = (cuisine != null && !cuisine.equalsIgnoreCase("All")) ? cuisine : null;
            recipes = recipeService.searchRecipes(filterCuisine, ingredient, userId);
        } else {
            recipes = recipeService.getAllRecipes(userId);
        }

        List<String> defaultCuisines = Arrays.asList("All", "Indian", "Italian", "Chinese", "Mexican", "American", "Mediterranean");

        model.addAttribute("recipes", recipes);
        model.addAttribute("cuisines", defaultCuisines);
        model.addAttribute("activeCuisine", (cuisine != null && !cuisine.isBlank()) ? cuisine : "All");
        model.addAttribute("searchTerm", search != null ? search : "");
        model.addAttribute("ingredientTerm", ingredient != null ? ingredient : "");

        return "recipes/recipes";
    }

    @GetMapping("/recipes/add")
    public String showAddForm(Model model, HttpSession session) {
        populateCommonModel(model, session, "recipes");
        if (!model.containsAttribute("recipeForm")) {
            model.addAttribute("recipeForm", new RecipeFormData());
        }
        model.addAttribute("isEdit", false);
        return "recipes/recipe-form";
    }

    @PostMapping("/recipes/add")
    public String saveRecipe(@Valid @ModelAttribute("recipeForm") RecipeFormData form,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) {
        Long userId = getAuthenticatedUserId(session);

        if (result.hasErrors()) {
            populateCommonModel(model, session, "recipes");
            model.addAttribute("isEdit", false);
            return "recipes/recipe-form";
        }

        try {
            RecipeRequest request = form.toRequest(userId);
            RecipeResponse created = recipeService.createRecipe(request, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe '" + created.getName() + "' created successfully!");
            return "redirect:/recipes/" + created.getId();
        } catch (Exception e) {
            populateCommonModel(model, session, "recipes");
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Error creating recipe: " + e.getMessage());
            return "recipes/recipe-form";
        }
    }

    @GetMapping("/recipes/{id}")
    public String viewRecipe(@PathVariable Long id, Model model, HttpSession session) {
        populateCommonModel(model, session, "recipes");
        try {
            RecipeResponse recipe = recipeService.getRecipeById(id);
            model.addAttribute("recipe", recipe);

            // Split cooking steps by newline or "Step "
            List<String> stepsList = Arrays.stream(recipe.getCookingSteps().split("\n+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            model.addAttribute("stepsList", stepsList);

            return "recipes/recipe-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Recipe not found: " + e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/recipes/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        populateCommonModel(model, session, "recipes");
        try {
            RecipeResponse recipe = recipeService.getRecipeById(id);
            RecipeFormData form = RecipeFormData.fromResponse(recipe);
            model.addAttribute("recipeForm", form);
            model.addAttribute("isEdit", true);
            model.addAttribute("recipeId", id);
            return "recipes/recipe-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Recipe not found: " + e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/recipes/{id}/edit")
    public String updateRecipe(@PathVariable Long id,
                               @Valid @ModelAttribute("recipeForm") RecipeFormData form,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               HttpSession session) {
        Long userId = getAuthenticatedUserId(session);

        if (result.hasErrors()) {
            populateCommonModel(model, session, "recipes");
            model.addAttribute("isEdit", true);
            model.addAttribute("recipeId", id);
            return "recipes/recipe-form";
        }

        try {
            RecipeRequest request = form.toRequest(userId);
            recipeService.updateRecipe(id, request, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe updated successfully!");
            return "redirect:/recipes/" + id;
        } catch (Exception e) {
            populateCommonModel(model, session, "recipes");
            model.addAttribute("isEdit", true);
            model.addAttribute("recipeId", id);
            model.addAttribute("errorMessage", "Error updating recipe: " + e.getMessage());
            return "recipes/recipe-form";
        }
    }

    @PostMapping("/recipes/{id}/delete")
    public String deleteRecipe(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        Long userId = getAuthenticatedUserId(session);
        try {
            recipeService.deleteRecipe(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Recipe deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete recipe: " + e.getMessage());
        }
        return "redirect:/recipes";
    }

    @PostMapping("/recipes/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id,
                                 @RequestParam(required = false, defaultValue = "false") boolean currentStatus,
                                 @RequestParam(required = false) String returnUrl,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        Long userId = getAuthenticatedUserId(session);
        try {
            boolean nextStatus = !currentStatus;
            recipeService.setFavorite(id, nextStatus, userId);
            redirectAttributes.addFlashAttribute("successMessage", nextStatus ? "Added to favorites!" : "Removed from favorites.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not update favorite status: " + e.getMessage());
        }

        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/recipes/" + id;
    }

    @GetMapping("/favorites")
    public String listFavorites(Model model, HttpSession session) {
        populateCommonModel(model, session, "favorites");
        Long userId = getAuthenticatedUserId(session);
        List<RecipeResponse> favorites = recipeService.getFavorites(userId);
        model.addAttribute("favorites", favorites);
        return "recipes/favorites";
    }
}
