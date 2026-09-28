package com.recipebox.controller.web;

import com.recipebox.entity.User;
import com.recipebox.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

public abstract class BaseWebController {

    protected final UserService userService;

    public BaseWebController(UserService userService) {
        this.userService = userService;
    }

    protected Long getAuthenticatedUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("CURRENT_USER_ID");
        if (userId == null) {
            // Default to first user (Alex Morgan, ID 1) for seamless out-of-the-box demonstration
            userId = 1L;
            try {
                User user = userService.getUserEntity(1L);
                session.setAttribute("CURRENT_USER_ID", user.getId());
                session.setAttribute("CURRENT_USER_NAME", user.getName());
                session.setAttribute("CURRENT_USER_EMAIL", user.getEmail());
            } catch (Exception ignored) {
            }
        }
        return userId;
    }

    protected void populateCommonModel(Model model, HttpSession session, String activePage) {
        Long userId = getAuthenticatedUserId(session);
        model.addAttribute("activePage", activePage);
        try {
            User user = userService.getUserEntity(userId);
            model.addAttribute("currentUser", user);
        } catch (Exception ignored) {
        }
    }
}
