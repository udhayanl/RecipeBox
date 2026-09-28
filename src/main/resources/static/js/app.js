/**
 * RecipeBox - Frontend Interactions & Dynamic UI Script
 */

document.addEventListener('DOMContentLoaded', () => {
    initSidebarToggle();
    initDynamicIngredients();
    initDynamicSteps();
    initShoppingListCheckboxes();
    initMealPlanModal();
    initPasswordToggle();
    initFavoriteButtons();
    initDeleteModals();
});

/* ==========================================================================
   Mobile Sidebar Toggle
   ========================================================================== */
function initSidebarToggle() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const appSidebar = document.querySelector('.app-sidebar');
    const backdrop = document.getElementById('sidebarBackdrop');

    if (sidebarToggle && appSidebar) {
        sidebarToggle.addEventListener('click', () => {
            appSidebar.classList.toggle('show');
            if (backdrop) backdrop.classList.toggle('show');
        });
    }

    if (backdrop && appSidebar) {
        backdrop.addEventListener('click', () => {
            appSidebar.classList.remove('show');
            backdrop.classList.remove('show');
        });
    }
}

/* ==========================================================================
   Dynamic Ingredient Rows for Recipe Form
   ========================================================================== */
function initDynamicIngredients() {
    const container = document.getElementById('ingredientsContainer');
    const addBtn = document.getElementById('addIngredientBtn');

    if (!container || !addBtn) return;

    addBtn.addEventListener('click', () => {
        const rows = container.querySelectorAll('.ingredient-row');
        const nextIndex = rows.length;

        const newRow = document.createElement('div');
        newRow.className = 'row g-2 mb-2 align-items-center ingredient-row dynamic-row';
        newRow.innerHTML = `
            <div class="col-md-5 col-12">
                <input type="text" 
                       name="ingredients[${nextIndex}].ingredientName" 
                       class="form-control" 
                       placeholder="e.g. Tomatoes" required />
            </div>
            <div class="col-md-3 col-6">
                <input type="number" step="any" min="0.1" 
                       name="ingredients[${nextIndex}].quantity" 
                       class="form-control" 
                       placeholder="Qty" required />
            </div>
            <div class="col-md-3 col-4">
                <input type="text" 
                       name="ingredients[${nextIndex}].unit" 
                       class="form-control" 
                       placeholder="Unit (e.g. pcs, grams, tbsp)" required />
            </div>
            <div class="col-md-1 col-2 text-end">
                <button type="button" class="btn btn-outline-danger btn-sm rounded-circle remove-ingredient-btn" title="Remove ingredient">
                    <i class="bi bi-trash3"></i>
                </button>
            </div>
        `;
        container.appendChild(newRow);
    });

    container.addEventListener('click', (e) => {
        const removeBtn = e.target.closest('.remove-ingredient-btn');
        if (removeBtn) {
            const rows = container.querySelectorAll('.ingredient-row');
            if (rows.length > 1) {
                removeBtn.closest('.ingredient-row').remove();
                reindexIngredients();
            } else {
                alert('A recipe must have at least one ingredient.');
            }
        }
    });
}

function reindexIngredients() {
    const container = document.getElementById('ingredientsContainer');
    if (!container) return;
    const rows = container.querySelectorAll('.ingredient-row');
    rows.forEach((row, idx) => {
        const nameInput = row.querySelector('input[name*="ingredientName"]');
        const qtyInput = row.querySelector('input[name*="quantity"]');
        const unitInput = row.querySelector('input[name*="unit"]');

        if (nameInput) nameInput.name = `ingredients[${idx}].ingredientName`;
        if (qtyInput) qtyInput.name = `ingredients[${idx}].quantity`;
        if (unitInput) unitInput.name = `ingredients[${idx}].unit`;
    });
}

/* ==========================================================================
   Dynamic Cooking Steps for Recipe Form
   ========================================================================== */
function initDynamicSteps() {
    const container = document.getElementById('stepsContainer');
    const addBtn = document.getElementById('addStepBtn');

    if (!container || !addBtn) return;

    addBtn.addEventListener('click', () => {
        const rows = container.querySelectorAll('.step-row');
        const nextIndex = rows.length;

        const newRow = document.createElement('div');
        newRow.className = 'd-flex gap-2 mb-2 align-items-start step-row dynamic-row';
        newRow.innerHTML = `
            <span class="badge bg-rb-primary rounded-circle d-flex align-items-center justify-content-center mt-2 step-badge" style="width: 28px; height: 28px; flex-shrink: 0;">
                ${nextIndex + 1}
            </span>
            <div class="flex-grow-1">
                <textarea class="form-control step-input" rows="2" placeholder="Describe this cooking step..." required></textarea>
            </div>
            <button type="button" class="btn btn-outline-danger btn-sm rounded-circle mt-1 remove-step-btn" title="Remove step">
                <i class="bi bi-trash3"></i>
            </button>
        `;
        container.appendChild(newRow);
    });

    container.addEventListener('click', (e) => {
        const removeBtn = e.target.closest('.remove-step-btn');
        if (removeBtn) {
            const rows = container.querySelectorAll('.step-row');
            if (rows.length > 1) {
                removeBtn.closest('.step-row').remove();
                reindexSteps();
            } else {
                alert('A recipe must have at least one cooking step.');
            }
        }
    });

    // Before form submit, combine dynamic step rows into single cookingSteps textarea
    const recipeForm = document.getElementById('recipeForm');
    if (recipeForm) {
        recipeForm.addEventListener('submit', (e) => {
            const stepInputs = container.querySelectorAll('.step-input');
            const steps = [];
            stepInputs.forEach((input, index) => {
                const val = input.value.trim();
                if (val) {
                    steps.push(`Step ${index + 1}: ${val}`);
                }
            });

            let hiddenInput = recipeForm.querySelector('input[name="cookingSteps"]');
            if (!hiddenInput) {
                hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'cookingSteps';
                recipeForm.appendChild(hiddenInput);
            }
            hiddenInput.value = steps.join('\n\n');
        });
    }
}

function reindexSteps() {
    const container = document.getElementById('stepsContainer');
    if (!container) return;
    const rows = container.querySelectorAll('.step-row');
    rows.forEach((row, idx) => {
        const badge = row.querySelector('.step-badge');
        if (badge) badge.textContent = (idx + 1);
    });
}

/* ==========================================================================
   Shopping List Interactive Checklist
   ========================================================================== */
function initShoppingListCheckboxes() {
    const checkboxes = document.querySelectorAll('.shopping-checkbox');
    const checkedCountEl = document.getElementById('checkedItemsCount');

    function updateCount() {
        if (!checkedCountEl) return;
        const total = checkboxes.length;
        const checked = document.querySelectorAll('.shopping-checkbox:checked').length;
        checkedCountEl.textContent = `${checked} / ${total} purchased`;
    }

    checkboxes.forEach(cb => {
        // Restore state from sessionStorage if available
        const key = 'shop_item_' + cb.dataset.itemId;
        if (sessionStorage.getItem(key) === 'true') {
            cb.checked = true;
            cb.closest('.shopping-item-row').classList.add('is-purchased');
        }

        cb.addEventListener('change', () => {
            const row = cb.closest('.shopping-item-row');
            if (cb.checked) {
                row.classList.add('is-purchased');
                sessionStorage.setItem(key, 'true');
            } else {
                row.classList.remove('is-purchased');
                sessionStorage.removeItem(key);
            }
            updateCount();
        });
    });

    updateCount();
}

/* ==========================================================================
   Meal Plan Modal Prefill
   ========================================================================== */
function initMealPlanModal() {
    const addButtons = document.querySelectorAll('.add-meal-slot-btn');
    const modalDateInput = document.getElementById('modalMealDate');
    const modalTypeSelect = document.getElementById('modalMealType');

    addButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const date = btn.dataset.date;
            const type = btn.dataset.type;

            if (modalDateInput && date) {
                modalDateInput.value = date;
            }
            if (modalTypeSelect && type) {
                modalTypeSelect.value = type;
            }
        });
    });
}

/* ==========================================================================
   Password Visibility Toggle
   ========================================================================== */
function initPasswordToggle() {
    const toggles = document.querySelectorAll('.toggle-password-btn');
    toggles.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.dataset.target;
            const input = document.getElementById(targetId);
            if (!input) return;

            const icon = btn.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                if (icon) {
                    icon.classList.remove('bi-eye');
                    icon.classList.add('bi-eye-slash');
                }
            } else {
                input.type = 'password';
                if (icon) {
                    icon.classList.remove('bi-eye-slash');
                    icon.classList.add('bi-eye');
                }
            }
        });
    });
}

/* ==========================================================================
   Favorite Toggle
   ========================================================================== */
function initFavoriteButtons() {
    const favButtons = document.querySelectorAll('.favorite-toggle-btn');
    favButtons.forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const recipeId = btn.dataset.recipeId;
            const form = document.getElementById(`fav-form-${recipeId}`);
            if (form) {
                form.submit();
            }
        });
    });
}

/* ==========================================================================
   Delete Recipe Confirmation
   ========================================================================== */
function initDeleteModals() {
    const deleteBtns = document.querySelectorAll('.trigger-delete-recipe');
    const form = document.getElementById('confirmDeleteForm');
    const recipeNameSpan = document.getElementById('deleteRecipeName');

    deleteBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const recipeId = btn.dataset.recipeId;
            const recipeName = btn.dataset.recipeName;

            if (form) {
                form.action = `/recipes/${recipeId}/delete`;
            }
            if (recipeNameSpan) {
                recipeNameSpan.textContent = recipeName || 'this recipe';
            }
        });
    });
}
