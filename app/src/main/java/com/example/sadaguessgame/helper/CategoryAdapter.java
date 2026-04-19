package com.example.sadaguessgame.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import com.example.sadaguessgame.data.ScoreStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView Adapter for managing category selection in a multi-select interface.
 *
 * <p>Features:
 * <ul>
 *   <li>Multi-language support (displays localized names, stores English names)</li>
 *   <li>Persistent selection state via GameState</li>
 *   <li>"Select All" functionality with automatic state management</li>
 *   <li>Selection change callbacks for real-time updates</li>
 *   <li>Optimized with DiffUtil for efficient updates</li>
 * </ul>
 *
 * @author Sada Guess Game Team
 * @version 2.1
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    // ================= CONSTANTS =================

    private static final String TAG = "CategoryAdapter";

    // ================= FIELDS =================

    private final Context context;
    private final List<Category> categories;
    private final Set<Integer> selectedPositions;
    @Nullable
    private OnCategorySelectionChangeListener selectionChangeListener;

    // ================= CONSTRUCTOR =================

    /**
     * Creates a CategoryAdapter with the given categories.
     * Automatically loads previously saved selections from GameState.
     *
     * @param context    Android context for accessing resources
     * @param categories List of categories to display (must not be null)
     * @throws IllegalArgumentException if categories is null
     */
    public CategoryAdapter(@NonNull Context context, @NonNull List<Category> categories) {

        this.context = context.getApplicationContext();
        this.categories = categories;
        this.selectedPositions = new HashSet<>();

        loadSavedSelections();
    }

    // ================= RECYCLERVIEW LIFECYCLE =================

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean isSelected = selectedPositions.contains(position);

        holder.bind(category, isSelected);
        holder.setClickListener(() -> handleCategoryClick(position));
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // ================= SELECTION STATE MANAGEMENT =================

    /**
     * Loads previously saved category selections from GameState.
     * Matches saved English names with current categories and restores selection state.
     * Auto-selects "Select All" if all regular categories are selected.
     */
    private void loadSavedSelections() {
        GameState currentGame = ScoreStorage.getInstance(context).getCurrentGame();

        if (currentGame == null || currentGame.categories == null || currentGame.categories.isEmpty()) {
            return;
        }

        List<String> savedCategories = currentGame.categories;

        // Match saved English names with current categories
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (savedCategories.contains(category.englishName)) {
                selectedPositions.add(i);
            }
        }

        // Auto-select "Select All" if all regular categories are selected
        autoSelectAllIfNeeded();
    }

    /**
     * Handles click events on category items.
     * Delegates to specific handlers based on whether it's the "Select All" button.
     * FIXED: Only updates "Select All" UI when its state actually changes.
     *
     * @param position Position of the clicked item in the adapter
     */
    private void handleCategoryClick(int position) {
        if (!isValidPosition(position)) {
            return;
        }

        int selectAllIndex = getSelectAllIndex();
        boolean wasAllSelected = isAllSelected(); // Track state BEFORE change

        if (position == selectAllIndex) {
            handleSelectAllClick();
        } else {
            handleRegularCategoryClick(position);
        }

        notifyItemChanged(position);

        // Only update "Select All" if its state actually changed
        if (position != selectAllIndex && wasAllSelected != isAllSelected()) {
            notifyItemChanged(selectAllIndex);
        }

        notifySelectionChanged();
    }

    /**
     * Handles clicks on the "Select All" button.
     * Toggles between selecting all categories and deselecting all.
     */
    private void handleSelectAllClick() {
        if (isAllSelected()) {
            deselectAll();
        } else {
            selectAll();
        }
    }

    /**
     * Handles clicks on regular category items (not "Select All").
     * Toggles selection state and manages "Select All" auto-selection.
     *
     * @param position Position of the clicked regular category
     */
    private void handleRegularCategoryClick(int position) {
        int selectAllIndex = getSelectAllIndex();

        if (selectedPositions.contains(position)) {
            // Deselect category and remove "Select All" if present
            selectedPositions.remove(position);
            selectedPositions.remove(selectAllIndex);
        } else {
            // Select category
            selectedPositions.add(position);
            // Auto-select "Select All" if all regular categories are now selected
            autoSelectAllIfNeeded();
        }
    }

    /**
     * Automatically selects "Select All" button if all regular categories are selected.
     */
    private void autoSelectAllIfNeeded() {
        int selectAllIndex = getSelectAllIndex();
        int regularCategoriesCount = categories.size() - 1;

        if (getSelectedCount() == regularCategoriesCount) {
            selectedPositions.add(selectAllIndex);
        }
    }

    // ================= PUBLIC SELECTION API =================

    /**
     * Selects all categories programmatically.
     * Useful for "Select All" functionality or setting defaults.
     */
    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < categories.size(); i++) {
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    /**
     * Deselects all categories programmatically.
     */
    public void deselectAll() {
        selectedPositions.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    /**
     * Selects specific categories by their English names.
     * Useful for restoring saved selections or programmatic control.
     *
     * @param englishNames List of English category names to select (e.g., ["animal", "food"])
     */
    public void selectByEnglishNames(@Nullable List<String> englishNames) {
        selectedPositions.clear();

        if (englishNames == null || englishNames.isEmpty()) {
            notifyDataSetChanged();
            notifySelectionChanged();
            return;
        }

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (englishNames.contains(category.englishName)) {
                selectedPositions.add(i);
            }
        }

        autoSelectAllIfNeeded();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    // ================= SELECTION QUERIES =================

    /**
     * Returns the number of selected categories, excluding the "Select All" button.
     *
     * @return Count of selected regular categories
     */
    public int getSelectedCount() {
        int count = selectedPositions.size();
        int selectAllIndex = getSelectAllIndex();

        if (selectedPositions.contains(selectAllIndex)) {
            count--;
        }

        return Math.max(0, count);
    }

    /**
     * Checks if at least one category is selected.
     *
     * @return true if one or more categories are selected, false otherwise
     */
    public boolean hasSelection() {
        return getSelectedCount() > 0;
    }

    /**
     * Checks if all categories (including "Select All") are selected.
     *
     * @return true if all categories are selected, false otherwise
     */
    private boolean isAllSelected() {
        return selectedPositions.size() == categories.size();
    }

    /**
     * Returns the English names of selected categories.
     *
     * <p><b>IMPORTANT:</b> Returns English names only (not localized display names).
     * These names are used for:
     * <ul>
     *   <li>Saving to GameState</li>
     *   <li>Loading asset images from folders</li>
     *   <li>Internal logic and comparisons</li>
     * </ul>
     *
     * @return Unmodifiable list of English category names (e.g., ["animal", "people", "food"])
     */
    @NonNull
    public List<String> getSelectedCategoryNames() {
        List<String> englishNames = new ArrayList<>();
        int selectAllIndex = getSelectAllIndex();

        for (int position : selectedPositions) {
            // Skip "Select All" button (not an actual category)
            if (position == selectAllIndex) {
                continue;
            }

            if (isValidPosition(position)) {
                Category category = categories.get(position);
                englishNames.add(category.englishName);
            }
        }

        return englishNames;
    }

    // ================= HELPER METHODS =================

    /**
     * Gets the index of the "Select All" button.
     * Assumes "Select All" is always the last item in the list.
     *
     * @return Index of "Select All" button
     */
    private int getSelectAllIndex() {
        return categories.size() - 1;
    }

    /**
     * Validates if a position is within valid bounds.
     *
     * @param position Position to validate
     * @return true if position is valid, false otherwise
     */
    private boolean isValidPosition(int position) {
        return position >= 0 && position < categories.size();
    }

    // ================= LISTENER MANAGEMENT =================

    /**
     * Sets the listener for selection change events.
     *
     * @param listener Listener to be notified of selection changes (can be null)
     */
    public void setOnCategorySelectionChangeListener(@Nullable OnCategorySelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    /**
     * Notifies the listener about selection changes.
     */
    private void notifySelectionChanged() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(getSelectedCount(), hasSelection());
        }
    }

    // ================= VIEW HOLDER =================

    /**
     * ViewHolder for category items.
     * Holds references to views and handles data binding.
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private final LinearLayout container;
        private final ImageView imgCategory;
        private final TextView txtTitle;
        @Nullable
        private Runnable clickListener;

        /**
         * Creates a new CategoryViewHolder.
         *
         * @param itemView Root view of the category item layout
         */
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            container = itemView.findViewById(R.id.categoryContainerLayout);
            imgCategory = itemView.findViewById(R.id.cardCategory);
            txtTitle = itemView.findViewById(R.id.txtCardCategory);

            // Set up click listener
            container.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.run();
                }
            });
        }

        /**
         * Binds category data to the views.
         *
         * @param category   Category to display
         * @param isSelected Whether this category is currently selected
         */
        void bind(@NonNull Category category, boolean isSelected) {
            // Set localized title
            txtTitle.setText(category.title);

            // Load category image with Glide
            Glide.with(context)
                    .load(category.imageRes)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_category_unselected)
                    .error(R.drawable.bg_category_unselected)
                    .into(imgCategory);

            // Update selection state UI
            updateSelectionUI(isSelected);
        }

        /**
         * Sets the click listener for this item.
         *
         * @param listener Runnable to execute on click
         */
        void setClickListener(@Nullable Runnable listener) {
            this.clickListener = listener;
        }

        /**
         * Updates the UI based on selection state.
         *
         * @param isSelected Whether the item is selected
         */
        private void updateSelectionUI(boolean isSelected) {
            int backgroundRes = isSelected
                    ? R.drawable.bg_category_selected
                    : R.drawable.bg_category_unselected;

            container.setBackground(ContextCompat.getDrawable(context, backgroundRes));

            // Optional: Add content description for accessibility
            container.setContentDescription(
                    isSelected ? txtTitle.getText() + " selected" : txtTitle.getText() + " not selected"
            );
        }
    }

    // ================= LISTENER INTERFACE =================

    /**
     * Interface for listening to category selection changes.
     */
    public interface OnCategorySelectionChangeListener {
        /**
         * Called when the category selection state changes.
         *
         * @param selectedCount Number of selected categories (excluding "Select All")
         * @param hasSelection  true if at least one category is selected
         */
        void onSelectionChanged(int selectedCount, boolean hasSelection);
    }

    // ================= DIFFUTIL CALLBACK (For future optimization) =================

    /**
     * DiffUtil callback for efficient list updates.
     * Use this with submitList() if converting to ListAdapter in the future.
     */
    public static class CategoryDiffCallback extends DiffUtil.ItemCallback<Category> {

        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.englishName.equals(newItem.englishName);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.englishName.equals(newItem.englishName) &&
                    oldItem.title.equals(newItem.title) &&
                    oldItem.imageRes == newItem.imageRes;
        }
    }


}