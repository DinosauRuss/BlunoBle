package com.cloudland.blunoble.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.cloudland.blunoble.fragments.CommandFragment
import com.cloudland.blunoble.fragments.ControllerFragment

class ViewPagerAdapter(private val name:String, private val address: String, fm: FragmentManager):
    FragmentPagerAdapter(fm) {

    private val COUNT = 2

    override fun getItem(position: Int): Fragment? {
        var frago: Fragment? = null
        when (position) {
            0 -> frago = CommandFragment.newInstance(name, address)
            1 -> frago = ControllerFragment.newInstance(name, address)
        }
        return frago
    }

    override fun getCount(): Int {
        return COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: CharSequence? = null
        when (position) {
            0 -> title = "Command"
            1 -> title = "Controller"
        }
        return title
    }
}