package com.A306.runnershi.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.A306.runnershi.Fragment.HomeFragment
import com.A306.runnershi.Fragment.ProfileFragment
import com.A306.runnershi.Fragment.RankingFragment
import com.A306.runnershi.Fragment.UserSearchFragment
import com.A306.runnershi.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Fragment 할당
        val homeFragment = HomeFragment()
        val userSearchFragment = UserSearchFragment()
        val rankingFragment = RankingFragment()
        val profileFragment = ProfileFragment()

        // 첫 시작 Fragment
        makeCurrentFragment(homeFragment)

        // 하단 메뉴에 따른 Fragment 변경
        bottom_navigation.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.navigation_home -> makeCurrentFragment(homeFragment)
                R.id.navigation_search -> makeCurrentFragment(userSearchFragment)
                R.id.navigation_ranking -> makeCurrentFragment(rankingFragment)
                R.id.navigation_profile -> makeCurrentFragment(profileFragment)
            }
            true
        }
    }

    // Fragment 변경을 위한 함수
    private fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.main_fragment, fragment)
            commit()
        }
    }
}