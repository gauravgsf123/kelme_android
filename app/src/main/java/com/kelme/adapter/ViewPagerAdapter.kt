package com.kelme.adapter

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kelme.custom.DynamicHeightViewPager


/**
 * Created by Gaurav Kumar on 02/07/21.
 */
class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    private val titleList: ArrayList<String>,
    private val fragmentList: ArrayList<out Fragment>
) : FragmentStatePagerAdapter(
    fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        /*var fragment:Fragment? = null
        for (i in 0..titleList.size){
            if (position === 1) fragment = fragmentList[i]
        }*/

       /* var fragment:Fragment? = null
        for (i in 0 until titleList.size-1){
            if (position==i && !fragmentList[i].isAdded) fragment = fragmentList[i]
        }

        return fragment!!*/
        // or throw some exception


         return fragmentList[position]
    }
    override fun getPageTitle(position: Int): CharSequence? {

        /*var charSequence:CharSequence=""
        for (i in 0 until titleList.size-1){
            if (position==i && !fragmentList[i].isAdded) charSequence = titleList[i]
        }

        return charSequence*/
        return titleList[position]
    }


   /* fun getTabView(position: Int): View?
    {
        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
        val v: View = LayoutInflater.from(View).inflate(R.layout.item_custom_tabview, null)
        val tv = v.findViewById(R.id.textView) as TextView
        tv.setText(tabTitles.get(position))
        return v
    }*/



    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
        if (container is DynamicHeightViewPager) {
            val fragment = item as Fragment
            if (fragment.view != null) {
                container.measureCurrentView(fragment.view)
            }
        }
    }
}