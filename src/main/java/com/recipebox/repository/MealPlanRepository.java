package com.recipebox.repository;

import com.recipebox.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    List<MealPlan> findByUserId(Long userId);

    List<MealPlan> findByMealDateBetween(LocalDate startDate, LocalDate endDate);

    List<MealPlan> findByUserIdAndMealDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<MealPlan> findByRecipeId(Long recipeId);

    void deleteByRecipeId(Long recipeId);
}
