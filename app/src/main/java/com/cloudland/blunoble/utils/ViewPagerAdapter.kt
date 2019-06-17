package com.cloudland.blunoble.utils

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.cloudland.blunoble.R
import com.cloudland.blunoble.fragments.TerminalFragment
import com.cloudland.blunoble.fragments.ControllerFragment
import com.cloudland.blunoble.fragments.CustomListFragment

class ViewPagerAdapter(private val activity: Activity, fm: FragmentManager): FragmentPagerAdapter(fm) {

    private val COUNT = 3

    override fun getItem(position: Int): Fragment? {
        var frago: Fragment? = null
        when (position) {
            0 -> frago = TerminalFragment()
            1 -> frago = ControllerFragment()
            2 -> frago = CustomListFragment()
        }
        return frago
    }

    override fun getCount(): Int {
        return COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: CharSequence? = null
        when (position) {
            0 -> title = activity.getString(R.string.tab_title_Terminal)
            1 -> title = activity.getString(R.string.tab_title_Controller)
            2 -> title = activity.getString(R.string.tab_title_list)
        }
        return title
    }
}