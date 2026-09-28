package com.recipebox.controller;

import com.recipebox.dto.ShoppingListItem;
import com.recipebox.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shopping-list")
@Tag(name = "Shopping List", description = "Endpoints for generating consolidated shopping lists from meal plans")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @GetMapping
    @Operation(summary = "Generate consolidated shopping list",
               description = "Aggregates ingredients across all scheduled meals for the given date range, summing compatible quantities.")
    public ResponseEntity<List<ShoppingListItem>> getShoppingList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long targetUserId = userId != null ? userId : headerUserId;
        return ResponseEntity.ok(shoppingListService.generateShoppingList(targetUserId, startDate, endDate));
    }
}
