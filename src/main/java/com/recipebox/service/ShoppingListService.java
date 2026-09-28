package com.recipebox.service;

import com.recipebox.dto.ShoppingListItem;
import com.recipebox.entity.MealPlan;
import com.recipebox.entity.Recipe;
import com.recipebox.entity.RecipeIngredient;
import com.recipebox.repository.MealPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ShoppingListService {

    private final MealPlanRepository mealPlanRepository;

    public ShoppingListService(MealPlanRepository mealPlanRepository) {
        this.mealPlanRepository = mealPlanRepository;
    }

    public List<ShoppingListItem> generateShoppingList(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required query parameters");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        List<MealPlan> mealPlans;
        if (userId != null) {
            mealPlans = mealPlanRepository.findByUserIdAndMealDateBetween(userId, startDate, endDate);
        } else {
            mealPlans = mealPlanRepository.findByMealDateBetween(startDate, endDate);
        }

        // Map key: "normalizedIngredientName###normalizedUnit" -> ShoppingListItem
        Map<String, ShoppingListItem> aggregatedMap = new LinkedHashMap<>();

        for (MealPlan mealPlan : mealPlans) {
            Recipe recipe = mealPlan.getRecipe();
            if (recipe == null || recipe.getRecipeIngredients() == null) {
                continue;
            }

            for (RecipeIngredient ri : recipe.getRecipeIngredients()) {
                if (ri.getIngredient() == null || ri.getQuantity() == null || ri.getQuantity() <= 0) {
                    continue;
                }

                String rawName = ri.getIngredient().getName();
                String displayName = formatDisplayName(rawName);
                String rawUnit = ri.getUnit() != null ? ri.getUnit().trim() : "units";
                String normalizedUnit = normalizeUnit(rawUnit);

                String key = displayName.toLowerCase() + "###" + normalizedUnit.toLowerCase();

                if (aggregatedMap.containsKey(key)) {
                    ShoppingListItem existing = aggregatedMap.get(key);
                    existing.setQuantity(roundTwoDecimals(existing.getQuantity() + ri.getQuantity()));
                } else {
                    aggregatedMap.put(key, new ShoppingListItem(
                            displayName,
                            roundTwoDecimals(ri.getQuantity()),
                            normalizedUnit
                    ));
                }
            }
        }

        return new ArrayList<>(aggregatedMap.values());
    }

    private String formatDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String trimmed = name.trim();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "units";
        }
        String u = unit.trim().toLowerCase();
        switch (u) {
            case "g":
            case "gram":
            case "grams":
                return "grams";
            case "kg":
            case "kilogram":
            case "kilograms":
                return "kg";
            case "piece":
            case "pieces":
            case "pcs":
            case "pc":
                return "pieces";
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return "tablespoon";
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return "teaspoon";
            case "cup":
            case "cups":
                return "cups";
            case "ml":
            case "milliliter":
            case "milliliters":
                return "ml";
            case "l":
            case "liter":
            case "liters":
                return "liters";
            default:
                return unit.trim();
        }
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
