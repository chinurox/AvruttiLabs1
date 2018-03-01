package com.example.gargc.avruttilabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.gargc.avruttilabs.Fragments.Accessories;
import com.example.gargc.avruttilabs.Fragments.Electronics;
import com.example.gargc.avruttilabs.Fragments.Lifestyle;
import com.example.gargc.avruttilabs.Fragments.Offers;

/**
 * Created by gargc on 09-02-2018.
 */

public class MainPageAdapter extends FragmentPagerAdapter {
    public MainPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position)
        {
            case 0 : Offers offers = new Offers();
                return offers;

            case 1 : Electronics electronics = new Electronics();
                return electronics;

            case 2 : Lifestyle lifestyle = new Lifestyle();
                return  lifestyle;

            case 3 : Accessories accessories = new Accessories();
                return accessories;
//
//            case 4 : HighCourtFragment highCourtFragment = new HighCourtFragment();
//                return highCourtFragment;
//
//            case 5 : BriefsFragment briefsFragment = new BriefsFragment();
//                return briefsFragment;

            default : return null;
        }

    }

    @Override
    public int getCount()
    {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0 : return "Offers";

            case 1 : return "Electronics";

            case 2 : return "Lifestyle";

            case 3 :return "Accessories";
//
//            case 4 : return "High Court";
//
//            case 5 : return "Briefs";
        }

        return super.getPageTitle(position);
    }

}
