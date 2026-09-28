# RecipeBox – Personal Recipe and Meal Planner

A modern, production-grade Spring Boot & Thymeleaf web application designed for home cooks, hostel residents, and food enthusiasts to store personal recipes, schedule weekly meal plans, and generate consolidated shopping lists with intelligent unit-compatible grocery aggregation.

---

## 1. Project Overview & Purpose

RecipeBox solves the daily dilemma of *"What should I cook this week?"* by providing:
- **Personal Recipe Management**: Store and discover recipes with detailed ingredients (quantities and units), preparation times, cuisines, descriptions, and step-by-step cooking instructions.
- **Smart Ingredient Catalog**: Reusable ingredient records to eliminate duplicates and enable cross-recipe grocery aggregation.
- **Weekly Meal Planner**: Interactive 7-day calendar matrix scheduling breakfast, lunch, dinner, or snacks linked to existing recipes.
- **Consolidated Shopping List**: Automatically scan planned meals for the selected date range, group ingredients, safely aggregate compatible units (e.g., pieces, grams, ml), and track purchases with interactive checkboxes.
- **Favorites Collection**: Quick bookmarking of cherished recipes.
- **Strict Business Rules**: Service-layer rules enforcing that meal plans MUST reference existing recipes and maintaining relational integrity.
- **Modern Thymeleaf UI**: A warm food-tech design system built with Thymeleaf, HTML5, CSS3, JavaScript, and Bootstrap 5.

---

## 2. Technology Stack

- **Backend**:
  - **Language**: Java 17
  - **Framework**: Spring Boot 4.1.1 / Spring Framework 6
  - **Data Persistence**: Spring Data JPA & Hibernate
  - **Database**: MySQL (production & dev) & H2 (in-memory mode for tests and standalone preview)
  - **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Positive`, etc.)
  - **API Documentation**: Swagger / OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 2.5.0)
  - **Testing**: JUnit 5, Spring Boot Test, Spring MVC MockMvc (27 passing tests)
  - **Build Tool**: Apache Maven (`mvnw`)
- **Frontend**:
  - **Template Engine**: Thymeleaf (`spring-boot-starter-thymeleaf`, `thymeleaf-spring6`)
  - **Styling**: Modern Custom Food-Tech CSS (`/css/style.css`) & Bootstrap 5.3
  - **Typography**: Google Font *'Plus Jakarta Sans'*
  - **Icons**: Bootstrap Icons
  - **Client-Side Interactions**: Vanilla JavaScript (`/js/app.js`)

---

## 3. Design System & Aesthetics

RecipeBox uses a warm, food-inspired, SaaS-grade visual design:
- **Primary Color**: `#E85D04` (Warm Spice Orange)
- **Secondary Color**: `#F48C06` (Golden Tangerine)
- **Accent**: `#FAA307` (Amber Honey)
- **Background**: `#FFF9F3` (Warm Ivory Cream)
- **Text**: `#2B2B2B` (Soft Charcoal)
- **Muted**: `#6B7280` (Neutral Gray)
- **Cards**: Soft rounded corners (`border-radius: 16px`), subtle warm elevation shadows, and micro-hover lifts.

---

## 4. Architectural Separation: REST API vs. Thymeleaf Web Views

RecipeBox maintains a strict separation of concerns:
1. **REST API Controllers** (`com.recipebox.controller.*`):
   - Handle programmatic JSON endpoints mapped under `/api/**`
   - Complete Swagger / OpenAPI documentation at `/swagger-ui.html`
2. **Spring MVC Web View Controllers** (`com.recipebox.controller.web.*`):
   - Return server-rendered Thymeleaf HTML templates:
     - `DashboardWebController`: `/` and `/dashboard`
     - `RecipeWebController`: `/recipes`, `/recipes/add`, `/recipes/{id}`, `/recipes/{id}/edit`, `/favorites`
     - `MealPlanWebController`: `/meal-planner`, `/meal-planner/add`, `/meal-planner/{id}/delete`
     - `ShoppingListWebController`: `/shopping-list`
     - `AuthWebController`: `/login`, `/register`, `/logout`, `/profile`

---

## 5. Application Pages & Features

| Page | URL Path | Key Features |
|---|---|---|
| **Dashboard** | `/dashboard` | Welcome banner, 4 live stat cards, Quick actions, Upcoming meals, This week overview, Favorite recipes. |
| **Recipe Collection** | `/recipes` | Search by recipe name, cuisine, or ingredient; Quick cuisine filters (All, Indian, Italian, Chinese, etc.); Recipe cards with prep time, favorite toggle, view/edit/delete. |
| **Recipe Details** | `/recipes/{id}` | Hero banner, detailed ingredients list with quantity badges, step-by-step instructions, Quick Add to Meal Plan modal. |
| **Add / Edit Recipe** | `/recipes/add`, `/recipes/{id}/edit` | Dynamic "+ Add Ingredient" rows, dynamic "+ Add Step" rows, form pre-population, Jakarta validation feedback. |
| **Favorites** | `/favorites` | Handpicked recipes, instant favorite toggle, empty state with "Explore Recipes" CTA. |
| **Meal Planner** | `/meal-planner` | Weekly 7-day calendar matrix (Monday–Sunday) with Breakfast, Lunch, Dinner, Snack slots; Date picker modal with strict recipe existence validation. |
| **Shopping List** | `/shopping-list` | Date range selector, consolidated grocery items, smart unit aggregation, interactive purchased checkboxes, print stylesheet. |
| **Chef Profile** | `/profile` | User stats, profile information, name & password updates, sign out. |
| **Authentication** | `/login`, `/register` | Modern centered cards, password visibility toggle, seed user quick login (`alex@example.com` / `password123`). |
| **Error Handling** | `/error/404`, `/error/500`, `/error/403` | Customized food-themed error screens with return navigation. |

---

## 6. How to Run the Application

### Prerequisites
- Java 17+ installed (`java -version`)
- MySQL 8.0+ (optional: in-memory H2 profile available for instant demo)

### Option A: Running with MySQL (Default)
1. Ensure MySQL is running on `localhost:3306`.
2. Configure credentials in `src/main/resources/application.properties` or set environment variables:
   ```powershell
   $env:DB_URL="jdbc:mysql://localhost:3306/recipebox?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   ```
3. Start the application:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

### Option B: Running Standalone / Demo (Instant H2 In-Memory)
Run the application using the included `h2` profile without needing a local MySQL server:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

### Accessing the Web Application
Open your browser and navigate to:
- **Web UI**: [http://localhost:8080](http://localhost:8080) or [http://localhost:8080/dashboard](http://localhost:8080/dashboard)
- **Swagger REST API**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

**Demo Credentials**:
- Email: `alex@example.com` | Password: `password123`
- Email: `sam@example.com` | Password: `password123`

---

## 7. Automated Test Suite (27 Passing Tests)

Run all backend service, controller validation, and Thymeleaf web view controller tests:
```powershell
.\mvnw.cmd test
```

### Test Coverage Breakdown
- `RecipeBoxServiceTest` (11 tests):
  - Recipe creation with ingredients and steps
  - Search by cuisine and ingredient
  - MealPlan creation with existing recipe
  - **Strict validation**: Reject MealPlan with invalid/non-existent recipe ID
  - Shopping list generation with compatible unit aggregation
  - Incompatible unit separation
  - Favorite toggle and user isolation
- `RecipeBoxControllerValidationTest` (6 tests):
  - REST API validation: reject blank recipe name, negative prep time, invalid quantities, empty cooking steps, missing meal date
- `RecipeBoxWebViewControllerTest` (10 tests):
  - Dashboard view and stats model
  - Recipe list view and search attributes
  - Recipe add form and details view
  - Meal planner weekly calendar matrix
  - Meal planner invalid recipe ID submission error handling
  - Consolidated shopping list view
  - Login, register, and profile views

---

## 8. Git Commit Guidelines

For step-by-step modular repository pushing, commits are structured cleanly:
1. Core Entities (`User`, `Ingredient`, `Recipe`, `MealPlan`, `MealType`)
2. Configuration & Repositories (`pom.xml`, `application.properties`, JPA Repositories)
3. Service Layer & Business Validation (`RecipeService`, `MealPlanService`, `ShoppingListService`, etc.)
4. REST API Controllers (`/api/**`)
5. Thymeleaf Web Views & UI Controllers (`/dashboard`, `/recipes`, `/meal-planner`, `/shopping-list`, `/profile`)
6. Comprehensive Automated Tests & Documentation
