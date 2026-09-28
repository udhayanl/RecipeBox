package com.recipebox.service;

import com.recipebox.dto.IngredientRequest;
import com.recipebox.dto.IngredientResponse;
import com.recipebox.entity.Ingredient;
import com.recipebox.exception.DuplicateResourceException;
import com.recipebox.exception.ResourceNotFoundException;
import com.recipebox.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public Ingredient getOrCreateIngredient(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        String cleanName = rawName.trim();
        return ingredientRepository.findByNameIgnoreCase(cleanName)
                .orElseGet(() -> ingredientRepository.save(new Ingredient(cleanName)));
    }

    public IngredientResponse createIngredient(IngredientRequest request) {
        String cleanName = request.getName().trim();
        if (ingredientRepository.existsByNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Ingredient already exists with name: " + cleanName);
        }
        Ingredient ingredient = ingredientRepository.save(new Ingredient(cleanName));
        return new IngredientResponse(ingredient.getId(), ingredient.getName());
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(i -> new IngredientResponse(i.getId(), i.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
        return new IngredientResponse(ingredient.getId(), ingredient.getName());
    }

    public void deleteIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
        ingredientRepository.delete(ingredient);
    }
}
