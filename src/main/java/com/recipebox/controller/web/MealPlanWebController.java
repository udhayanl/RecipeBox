package com.recipebox.controller.web;

import com.recipebox.dto.MealPlanRequest;
import com.recipebox.dto.MealPlanResponse;
import com.recipebox.dto.RecipeResponse;
import com.recipebox.entity.MealType;
import com.recipebox.exception.ResourceNotFoundException;
import com.recipebox.service.MealPlanService;
import com.recipebox.service.RecipeService;
import com.recipebox.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Controller
public class MealPlanWebController extends BaseWebController {

    private final MealPlanService mealPlanService;
    private final RecipeService recipeService;

    public MealPlanWebController(UserService userService,
                                 MealPlanService mealPlanService,
                                 RecipeService recipeService) {
        super(userService);
        this.mealPlanService = mealPlanService;
        this.recipeService = recipeService;
    }

    @GetMapping("/meal-planner")
    public String mealPlanner(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                              Model model,
                              HttpSession session) {
        populateCommonModel(model, session, "meal-planner");
        Long userId = getAuthenticatedUserId(session);

        LocalDate effectiveStart;
        if (weekStart != null) {
            effectiveStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        } else {
            effectiveStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        LocalDate effectiveEnd = effectiveStart.plusDays(6);

        // Fetch meal plans for the selected 7 days
        List<MealPlanResponse> plans = mealPlanService.getMealPlans(userId, effectiveStart, effectiveEnd);

        // Group plans by date string and meal type for easy calendar lookup
        // Key: "YYYY-MM-DD_MEALTYPE" -> List<MealPlanResponse>
        Map<String, List<MealPlanResponse>> mealGrid = new HashMap<>();
        for (MealPlanResponse plan : plans) {
            String key = plan.getMealDate().toString() + "_" + plan.getMealType().name();
            mealGrid.computeIfAbsent(key, k -> new ArrayList<>()).add(plan);
        }

        // Generate list of 7 days
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(effectiveStart.plusDays(i));
        }

        // Available recipes for dropdown (must exist)
        List<RecipeResponse> userRecipes = recipeService.getAllRecipes(userId);

        model.addAttribute("weekStart", effectiveStart);
        model.addAttribute("weekEnd", effectiveEnd);
        model.addAttribute("prevWeek", effectiveStart.minusWeeks(1));
        model.addAttribute("nextWeek", effectiveStart.plusWeeks(1));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("days", days);
        model.addAttribute("mealTypes", MealType.values());
        model.addAttribute("mealGrid", mealGrid);
        model.addAttribute("recipes", userRecipes);
        model.addAttribute("newMealPlan", new MealPlanRequest(LocalDate.now(), MealType.DINNER, null));

        return "meal-planner/meal-planner";
    }

    @PostMapping("/meal-planner/add")
    public String addMealPlan(@Valid @ModelAttribute("newMealPlan") MealPlanRequest request,
                              BindingResult result,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnWeek,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        Long userId = getAuthenticatedUserId(session);

        if (result.hasErrors() || request.getRecipeId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a valid recipe, date, and meal type.");
            return "redirect:/meal-planner" + (returnWeek != null ? "?weekStart=" + returnWeek : "");
        }

        try {
            // Strictly enforce business rule: Recipe MUST exist before saving
            recipeService.getRecipeEntity(request.getRecipeId());

            request.setUserId(userId);
            mealPlanService.createMealPlan(request, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Meal plan scheduled successfully!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid recipe: The selected recipe does not exist.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not save meal plan: " + e.getMessage());
        }

        return "redirect:/meal-planner" + (returnWeek != null ? "?weekStart=" + returnWeek : "");
    }

    @PostMapping("/meal-planner/{id}/delete")
    public String deleteMealPlan(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnWeek,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        Long userId = getAuthenticatedUserId(session);
        try {
            mealPlanService.deleteMealPlan(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Meal removed from schedule.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not remove meal plan: " + e.getMessage());
        }
        return "redirect:/meal-planner" + (returnWeek != null ? "?weekStart=" + returnWeek : "");
    }
}
