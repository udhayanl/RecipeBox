package com.recipebox.controller;

import com.recipebox.dto.MealPlanRequest;
import com.recipebox.dto.MealPlanResponse;
import com.recipebox.service.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
@Tag(name = "Meal Planning", description = "Endpoints for scheduling meals linked to recipes")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping
    @Operation(summary = "Create meal plan", description = "Schedules a meal with a date, meal type, and recipe reference. Enforces that recipe exists.")
    public ResponseEntity<MealPlanResponse> createMealPlan(
            @Valid @RequestBody MealPlanRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        MealPlanResponse response = mealPlanService.createMealPlan(request, headerUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get meal plans", description = "Lists scheduled meals, optionally filtered by date range and user")
    public ResponseEntity<List<MealPlanResponse>> getMealPlans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long targetUserId = userId != null ? userId : headerUserId;
        return ResponseEntity.ok(mealPlanService.getMealPlans(targetUserId, startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meal plan by ID", description = "Retrieves details of a specific meal plan")
    public ResponseEntity<MealPlanResponse> getMealPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(mealPlanService.getMealPlanById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update meal plan", description = "Updates scheduled date, meal type, or referenced recipe")
    public ResponseEntity<MealPlanResponse> updateMealPlan(
            @PathVariable Long id,
            @Valid @RequestBody MealPlanRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        return ResponseEntity.ok(mealPlanService.updateMealPlan(id, request, headerUserId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete meal plan", description = "Removes a scheduled meal plan entry")
    public ResponseEntity<Void> deleteMealPlan(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        mealPlanService.deleteMealPlan(id, headerUserId);
        return ResponseEntity.noContent().build();
    }
}
