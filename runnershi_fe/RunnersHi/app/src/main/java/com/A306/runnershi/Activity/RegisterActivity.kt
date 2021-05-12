package com.A306.runnershi.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.A306.runnershi.Fragment.Register.NormalRegisterFragment
import com.A306.runnershi.Fragment.Register.SocialRegisterFragment
import com.A306.runnershi.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    var userId = ""
    var token = ""
    var userName = ""
    var runningType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Fragment 할당
        val normalRegisterFragment = NormalRegisterFragment()
        val socialRegisterFragment = SocialRegisterFragment()

        // 로그인 페이지에서 넘어온 경로 확인
        when (intent.getStringExtra("loginType")) {
            "Normal" -> {
                makeCurrentFragment(normalRegisterFragment)
            }
            "Social" -> {
                userId = intent.getStringExtra("userId").toString()
                userName = intent.getStringExtra("userName").toString()
                token = intent.getStringExtra("token").toString()
                runningType = intent.getStringExtra("runningType").toString()
                makeCurrentFragment(socialRegisterFragment)
            }
            else -> {
                Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    // Fragment 변경을 위한 함수
    private fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.registerFragment, fragment)
            commit()
        }
    }
}