package com.example.gargc.avruttilabs.Activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.gargc.avruttilabs.Fragments.Robotics;
import com.example.gargc.avruttilabs.Fragments.BasicComponentsFragment;
import com.example.gargc.avruttilabs.Fragments.Embedded;
import com.example.gargc.avruttilabs.Fragments.Sensors;
import com.example.gargc.avruttilabs.Fragments.Tools;

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
            case 0 : BasicComponentsFragment basicComponentsFragment = new BasicComponentsFragment();
                return basicComponentsFragment;

            case 1 : Sensors sensors = new Sensors();
                return sensors;

            case 2 : Embedded embedded = new Embedded();
                return embedded;

            case 3 : Robotics robotics = new Robotics();
                return robotics;

            case 4 : Tools tools = new Tools();
                    return tools;
//
//            case 5 : BriefsFragment briefsFragment = new BriefsFragment();
//                return briefsFragment;

            default : return null;
        }

    }

    @Override
    public int getCount()
    {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0 : return "BasicComponentsFragment";

            case 1 : return "Sensors";

            case 2 : return "Embedded";

            case 3 :return "Robotics";
//
            case 4 : return "Tools";
//
//            case 5 : return "Briefs";
        }

        return super.getPageTitle(position);
    }

}
