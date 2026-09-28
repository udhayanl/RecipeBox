package com.recipebox.controller.web;

import com.recipebox.dto.ShoppingListItem;
import com.recipebox.service.ShoppingListService;
import com.recipebox.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;

@Controller
public class ShoppingListWebController extends BaseWebController {

    private final ShoppingListService shoppingListService;

    public ShoppingListWebController(UserService userService, ShoppingListService shoppingListService) {
        super(userService);
        this.shoppingListService = shoppingListService;
    }

    @GetMapping("/shopping-list")
    public String shoppingList(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               Model model,
                               HttpSession session) {
        populateCommonModel(model, session, "shopping-list");
        Long userId = getAuthenticatedUserId(session);

        LocalDate today = LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        if (start.isAfter(end)) {
            model.addAttribute("errorMessage", "Start date must be before or equal to end date.");
            end = start.plusDays(6);
        }

        List<ShoppingListItem> items;
        try {
            items = shoppingListService.generateShoppingList(userId, start, end);
        } catch (Exception e) {
            items = Collections.emptyList();
            model.addAttribute("errorMessage", "Could not generate shopping list: " + e.getMessage());
        }

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("items", items);
        model.addAttribute("totalItems", items.size());

        return "shopping/shopping-list";
    }
}
