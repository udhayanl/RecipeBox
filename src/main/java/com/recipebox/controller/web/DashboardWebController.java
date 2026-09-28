package com.recipebox.controller.web;

import com.recipebox.dto.MealPlanResponse;
import com.recipebox.dto.RecipeResponse;
import com.recipebox.service.MealPlanService;
import com.recipebox.service.RecipeService;
import com.recipebox.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
public class DashboardWebController extends BaseWebController {

    private final RecipeService recipeService;
    private final MealPlanService mealPlanService;

    public DashboardWebController(UserService userService,
                                  RecipeService recipeService,
                                  MealPlanService mealPlanService) {
        super(userService);
        this.recipeService = recipeService;
        this.mealPlanService = mealPlanService;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        populateCommonModel(model, session, "dashboard");
        Long userId = getAuthenticatedUserId(session);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<RecipeResponse> allRecipes = recipeService.getAllRecipes(userId);
        List<RecipeResponse> favoriteRecipes = recipeService.getFavorites(userId);
        List<MealPlanResponse> weekMeals = mealPlanService.getMealPlans(userId, weekStart, weekEnd);
        List<MealPlanResponse> upcomingMeals = mealPlanService.getMealPlans(userId, today, today.plusDays(7));

        model.addAttribute("totalRecipes", allRecipes.size());
        model.addAttribute("favoriteCount", favoriteRecipes.size());
        model.addAttribute("plannedThisWeek", weekMeals.size());
        model.addAttribute("upcomingMealsCount", upcomingMeals.size());

        model.addAttribute("upcomingMeals", upcomingMeals);
        model.addAttribute("weekMeals", weekMeals);
        model.addAttribute("favoriteRecipes", favoriteRecipes);
        model.addAttribute("recentRecipes", allRecipes.stream().limit(6).toList());

        return "dashboard/dashboard";
    }
}
