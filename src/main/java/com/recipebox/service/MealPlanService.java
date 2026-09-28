package com.recipebox.service;

import com.recipebox.dto.MealPlanRequest;
import com.recipebox.dto.MealPlanResponse;
import com.recipebox.entity.MealPlan;
import com.recipebox.entity.Recipe;
import com.recipebox.entity.User;
import com.recipebox.exception.ResourceNotFoundException;
import com.recipebox.repository.MealPlanRepository;
import com.recipebox.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final UserService userService;

    public MealPlanService(MealPlanRepository mealPlanRepository,
                           RecipeRepository recipeRepository,
                           UserService userService) {
        this.mealPlanRepository = mealPlanRepository;
        this.recipeRepository = recipeRepository;
        this.userService = userService;
    }

    public MealPlanResponse createMealPlan(MealPlanRequest request, Long authenticatedUserId) {
        Long targetUserId = request.getUserId() != null ? request.getUserId() : authenticatedUserId;
        if (targetUserId == null) {
            targetUserId = 1L;
        }
        User user = userService.getUserEntity(targetUserId);

        // Enforce business rule: Recipe MUST exist before saving
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with ID " + request.getRecipeId() + " does not exist"));

        MealPlan mealPlan = new MealPlan();
        mealPlan.setMealDate(request.getMealDate());
        mealPlan.setMealType(request.getMealType());
        mealPlan.setUser(user);
        mealPlan.setRecipe(recipe);
        mealPlan.setNotes(request.getNotes());

        MealPlan saved = mealPlanRepository.save(mealPlan);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MealPlanResponse> getMealPlans(Long userId, LocalDate startDate, LocalDate endDate) {
        List<MealPlan> mealPlans;

        if (startDate != null && endDate != null) {
            if (userId != null) {
                mealPlans = mealPlanRepository.findByUserIdAndMealDateBetween(userId, startDate, endDate);
            } else {
                mealPlans = mealPlanRepository.findByMealDateBetween(startDate, endDate);
            }
        } else {
            if (userId != null) {
                mealPlans = mealPlanRepository.findByUserId(userId);
            } else {
                mealPlans = mealPlanRepository.findAll();
            }
        }

        return mealPlans.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MealPlanResponse getMealPlanById(Long id) {
        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found with id: " + id));
        return mapToResponse(mealPlan);
    }

    public MealPlanResponse updateMealPlan(Long id, MealPlanRequest request, Long authenticatedUserId) {
        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found with id: " + id));

        if (authenticatedUserId != null && mealPlan.getUser() != null && !mealPlan.getUser().getId().equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only modify your own meal plans");
        }

        // Validate recipe exists
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with ID " + request.getRecipeId() + " does not exist"));

        mealPlan.setMealDate(request.getMealDate());
        mealPlan.setMealType(request.getMealType());
        mealPlan.setRecipe(recipe);
        mealPlan.setNotes(request.getNotes());

        MealPlan updated = mealPlanRepository.save(mealPlan);
        return mapToResponse(updated);
    }

    public void deleteMealPlan(Long id, Long authenticatedUserId) {
        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found with id: " + id));

        if (authenticatedUserId != null && mealPlan.getUser() != null && !mealPlan.getUser().getId().equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only delete your own meal plans");
        }

        mealPlanRepository.delete(mealPlan);
    }

    private MealPlanResponse mapToResponse(MealPlan mealPlan) {
        return new MealPlanResponse(
                mealPlan.getId(),
                mealPlan.getMealDate(),
                mealPlan.getMealType(),
                mealPlan.getUser() != null ? mealPlan.getUser().getId() : null,
                mealPlan.getUser() != null ? mealPlan.getUser().getName() : null,
                mealPlan.getRecipe() != null ? mealPlan.getRecipe().getId() : null,
                mealPlan.getRecipe() != null ? mealPlan.getRecipe().getName() : null,
                mealPlan.getRecipe() != null ? mealPlan.getRecipe().getCuisine() : null,
                mealPlan.getNotes(),
                mealPlan.getCreatedAt()
        );
    }
}
