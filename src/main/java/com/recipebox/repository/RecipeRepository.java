package com.recipebox.repository;

import com.recipebox.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByUserId(Long userId);

    List<Recipe> findByCuisineIgnoreCase(String cuisine);

    List<Recipe> findByUserIdAndCuisineIgnoreCase(Long userId, String cuisine);

    List<Recipe> findByFavoriteTrue();

    List<Recipe> findByUserIdAndFavoriteTrue(Long userId);

    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.recipeIngredients ri JOIN ri.ingredient i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredient, '%'))")
    List<Recipe> searchByIngredient(@Param("ingredient") String ingredient);

    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.recipeIngredients ri JOIN ri.ingredient i WHERE r.user.id = :userId AND LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredient, '%'))")
    List<Recipe> searchByUserIdAndIngredient(@Param("userId") Long userId, @Param("ingredient") String ingredient);
}
