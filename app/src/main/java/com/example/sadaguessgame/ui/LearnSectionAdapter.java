package com.example.sadaguessgame.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sadaguessgame.R;

public class LearnSectionAdapter extends RecyclerView.Adapter<LearnSectionAdapter.ViewHolder> {

    public static class Section {
        public final String title;
        public final String description;
        public final int imageRes;

        public Section(String title, String description, int imageRes) {
            this.title = title;
            this.description = description;
            this.imageRes = imageRes;
        }
    }

    private final Context context;
    private final Section[] sections;

    public LearnSectionAdapter(Context context, Section[] sections) {
        this.context = context;
        this.sections = sections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_learn_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sections[position];
        holder.title.setText(section.title);
        holder.description.setText(section.description);
        holder.image.setImageResource(section.imageRes);
        holder.stepNumber.setText(String.valueOf(position + 1));

        // Alternate card alignment for visual variety
        boolean isEven = (position % 2 == 0);
        holder.image.setScaleX(isEven ? 1f : -1f);
    }

    @Override
    public int getItemCount() {
        return sections == null ? 0 : sections.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, stepNumber;
        ImageView image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.learnSectionTitle);
            description = itemView.findViewById(R.id.learnSectionDesc);
            image = itemView.findViewById(R.id.learnSectionImage);
            stepNumber = itemView.findViewById(R.id.learnStepNumber);
        }
    }
}