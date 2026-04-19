package com.example.sadaguessgame.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.sadaguessgame.R;
import com.example.sadaguessgame.enums.SliderImage;

public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private ImageView sliderImage;
    private TextView sliderTitle;
    private TextView sliderDesc;

    private SliderImage[] sliderAllImages = SliderImage.values();

    private String[] sliderAllTitle;
    private String[] sliderAllDesc;

    public ViewPagerAdapter(Context context) {
        this.context = context;

        sliderAllTitle = context.getResources()
                .getStringArray(R.array.slider_all_title);

        sliderAllDesc = context.getResources()
                .getStringArray(R.array.slider_all_desc);
    }

    @Override
    public int getCount() {
        return sliderAllTitle.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.slider_screen, container, false);

        sliderImage = view.findViewById(R.id.sliderImage);
        sliderTitle = view.findViewById(R.id.sliderTitle);
        sliderDesc = view.findViewById(R.id.sliderDesc);

        sliderImage.setImageResource(sliderAllImages[position].getDrawableResId());
        sliderImage.setContentDescription(sliderAllTitle[position]);

        sliderTitle.setText(sliderAllTitle[position]);
        sliderDesc.setText(sliderAllDesc[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

