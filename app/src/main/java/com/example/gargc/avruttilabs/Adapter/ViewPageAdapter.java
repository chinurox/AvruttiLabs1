package com.example.gargc.avruttilabs.Adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.gargc.avruttilabs.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by gargc on 02-03-2018.
 */

public class ViewPageAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    ArrayList<String> images;

    public ViewPageAdapter(Context context, ArrayList<String> images){
        this.context=context;
        this.images=images;
    }
    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.image_slider_layout,null);
        ImageView imageView=(ImageView) view.findViewById(R.id.imageSlider);
        Picasso.with(context).load(images.get(position)).placeholder(R.mipmap.image_not_available).into(imageView);

        ViewPager viewPager=(ViewPager) container;
        viewPager.addView(view,0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager=(ViewPager) container;
        View view=(View) object;
        viewPager.removeView(view);
    }
}
