package com.recipebox.controller.web;

import com.recipebox.dto.UserRequest;
import com.recipebox.dto.UserResponse;
import com.recipebox.entity.User;
import com.recipebox.repository.UserRepository;
import com.recipebox.service.MealPlanService;
import com.recipebox.service.RecipeService;
import com.recipebox.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthWebController extends BaseWebController {

    private final UserRepository userRepository;
    private final RecipeService recipeService;
    private final MealPlanService mealPlanService;

    public AuthWebController(UserService userService,
                             UserRepository userRepository,
                             RecipeService recipeService,
                             MealPlanService mealPlanService) {
        super(userService);
        this.userRepository = userRepository;
        this.recipeService = recipeService;
        this.mealPlanService = mealPlanService;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("CURRENT_USER_ID") != null) {
            // Already logged in
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam(required = false) boolean rememberMe,
                          Model model,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("errorMessage", "Email and password cannot be empty.");
            return "auth/login";
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password.trim())) {
                session.setAttribute("CURRENT_USER_ID", user.getId());
                session.setAttribute("CURRENT_USER_NAME", user.getName());
                session.setAttribute("CURRENT_USER_EMAIL", user.getEmail());
                redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + user.getName() + "!");
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("errorMessage", "Invalid email or password.");
        model.addAttribute("email", email);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("CURRENT_USER_ID") != null) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("errorMessage", "All fields are required.");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "auth/register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "auth/register";
        }

        if (userRepository.existsByEmail(email.trim())) {
            model.addAttribute("errorMessage", "An account with this email already exists.");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "auth/register";
        }

        try {
            UserRequest req = new UserRequest(name.trim(), email.trim(), password.trim());
            UserResponse created = userService.createUser(req);

            session.setAttribute("CURRENT_USER_ID", created.getId());
            session.setAttribute("CURRENT_USER_NAME", created.getName());
            session.setAttribute("CURRENT_USER_EMAIL", created.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Welcome to RecipeBox.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String doLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("infoMessage", "You have been logged out.");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession session) {
        populateCommonModel(model, session, "profile");
        Long userId = getAuthenticatedUserId(session);

        User user = userService.getUserEntity(userId);
        model.addAttribute("user", user);
        model.addAttribute("recipeCount", recipeService.getAllRecipes(userId).size());
        model.addAttribute("favoriteCount", recipeService.getFavorites(userId).size());
        model.addAttribute("mealPlanCount", mealPlanService.getMealPlans(userId, null, null).size());

        return "profile/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam(required = false) String newPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getAuthenticatedUserId(session);
        try {
            User user = userService.getUserEntity(userId);
            if (name != null && !name.isBlank()) {
                user.setName(name.trim());
            }
            if (newPassword != null && !newPassword.isBlank()) {
                user.setPassword(newPassword.trim());
            }
            userRepository.save(user);

            session.setAttribute("CURRENT_USER_NAME", user.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
