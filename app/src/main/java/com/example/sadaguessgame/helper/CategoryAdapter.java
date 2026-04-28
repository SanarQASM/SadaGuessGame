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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private static final String TAG = "CategoryAdapter";

    private final Context context;
    private final List<Category> categories;
    private final Set<Integer> selectedPositions;
    @Nullable
    private OnCategorySelectionChangeListener selectionChangeListener;

    public CategoryAdapter(@NonNull Context context, @NonNull List<Category> categories) {
        this.context = context.getApplicationContext();
        this.categories = categories;
        this.selectedPositions = new HashSet<>();
        loadSavedSelections();
    }

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

    private void loadSavedSelections() {
        GameState currentGame = ScoreStorage.getInstance(context).getCurrentGame();
        if (currentGame == null || currentGame.categories == null || currentGame.categories.isEmpty()) return;

        List<String> savedCategories = currentGame.categories;
        for (int i = 0; i < categories.size(); i++) {
            if (savedCategories.contains(categories.get(i).englishName)) {
                selectedPositions.add(i);
            }
        }
        autoSelectAllIfNeeded();
    }

    private void handleCategoryClick(int position) {
        if (!isValidPosition(position)) return;

        int selectAllIndex = getSelectAllIndex();
        boolean wasAllSelected = isAllSelected();

        if (position == selectAllIndex) {
            handleSelectAllClick();
        } else {
            handleRegularCategoryClick(position);
        }

        notifyItemChanged(position);

        if (position != selectAllIndex && wasAllSelected != isAllSelected()) {
            notifyItemChanged(selectAllIndex);
        }

        notifySelectionChanged();
    }

    private void handleSelectAllClick() {
        if (isAllSelected()) deselectAll();
        else selectAll();
    }

    private void handleRegularCategoryClick(int position) {
        int selectAllIndex = getSelectAllIndex();
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
            selectedPositions.remove(selectAllIndex);
        } else {
            selectedPositions.add(position);
            autoSelectAllIfNeeded();
        }
    }

    private void autoSelectAllIfNeeded() {
        int selectAllIndex = getSelectAllIndex();
        if (getSelectedCount() == categories.size() - 1) {
            selectedPositions.add(selectAllIndex);
        }
    }

    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < categories.size(); i++) selectedPositions.add(i);
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void deselectAll() {
        selectedPositions.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void selectByEnglishNames(@Nullable List<String> englishNames) {
        selectedPositions.clear();
        if (englishNames == null || englishNames.isEmpty()) {
            notifyDataSetChanged();
            notifySelectionChanged();
            return;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (englishNames.contains(categories.get(i).englishName)) selectedPositions.add(i);
        }
        autoSelectAllIfNeeded();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public int getSelectedCount() {
        int count = selectedPositions.size();
        if (selectedPositions.contains(getSelectAllIndex())) count--;
        return Math.max(0, count);
    }

    public boolean hasSelection() { return getSelectedCount() > 0; }

    private boolean isAllSelected() { return selectedPositions.size() == categories.size(); }

    @NonNull
    public List<String> getSelectedCategoryNames() {
        List<String> englishNames = new ArrayList<>();
        int selectAllIndex = getSelectAllIndex();
        for (int position : selectedPositions) {
            if (position == selectAllIndex) continue;
            if (isValidPosition(position)) englishNames.add(categories.get(position).englishName);
        }
        return englishNames;
    }

    private int getSelectAllIndex() { return categories.size() - 1; }

    private boolean isValidPosition(int position) {
        return position >= 0 && position < categories.size();
    }

    public void setOnCategorySelectionChangeListener(@Nullable OnCategorySelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    private void notifySelectionChanged() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(getSelectedCount(), hasSelection());
        }
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private final LinearLayout container;
        private final ImageView imgCategory;
        private final TextView txtTitle;
        @Nullable
        private Runnable clickListener;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            context     = itemView.getContext();
            container   = itemView.findViewById(R.id.categoryContainerLayout);
            imgCategory = itemView.findViewById(R.id.cardCategory);
            txtTitle    = itemView.findViewById(R.id.txtCardCategory);

            container.setOnClickListener(v -> {
                if (clickListener != null) clickListener.run();
            });
        }

        void bind(@NonNull Category category, boolean isSelected) {
            txtTitle.setText(category.title);

            Glide.with(context)
                    .load(category.imageRes)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_category_unselected)
                    .error(R.drawable.bg_category_unselected)
                    .into(imgCategory);

            updateSelectionUI(isSelected);
        }

        void setClickListener(@Nullable Runnable listener) {
            this.clickListener = listener;
        }

        private void updateSelectionUI(boolean isSelected) {
            int backgroundRes = isSelected
                    ? R.drawable.bg_category_selected
                    : R.drawable.bg_category_unselected;
            container.setBackground(ContextCompat.getDrawable(context, backgroundRes));

            // Accessibility: use localised string resource instead of hardcoded concatenation
            String title = txtTitle.getText().toString();
            container.setContentDescription(isSelected
                    ? context.getString(R.string.category_content_desc_selected, title)
                    : context.getString(R.string.category_content_desc_not_selected, title));
        }
    }

    // ─── Listener interface ──────────────────────────────────────────────────

    public interface OnCategorySelectionChangeListener {
        void onSelectionChanged(int selectedCount, boolean hasSelection);
    }

    // ─── DiffUtil callback ───────────────────────────────────────────────────

    public static class CategoryDiffCallback extends DiffUtil.ItemCallback<Category> {
        @Override
        public boolean areItemsTheSame(@NonNull Category o, @NonNull Category n) {
            return o.englishName.equals(n.englishName);
        }
        @Override
        public boolean areContentsTheSame(@NonNull Category o, @NonNull Category n) {
            return o.englishName.equals(n.englishName)
                    && o.title.equals(n.title)
                    && o.imageRes == n.imageRes;
        }
    }
}