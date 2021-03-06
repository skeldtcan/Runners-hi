package com.A306.runnershi.Fragment.Profile

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.A306.runnershi.Activity.MainActivity
import com.A306.runnershi.Fragment.Ranking.RankingFragment
import com.A306.runnershi.Helper.RetrofitClient
import com.A306.runnershi.Model.Detail
import com.A306.runnershi.Model.Run
import com.A306.runnershi.R
import com.A306.runnershi.ViewModel.SingleRunViewModel
import com.A306.runnershi.ViewModel.UserViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.history.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.random.Random

@AndroidEntryPoint
class ProfileFragment : Fragment() { //, View.OnClickListener

    companion object {
        var profileImgList = intArrayOf(
            R.drawable.profile_1, R.drawable.profile_2, R.drawable.profile_3, R.drawable.profile_4, R.drawable.profile_5,
            R.drawable.profile_6, R.drawable.profile_7, R.drawable.profile_8, R.drawable.profile_9, R.drawable.profile_10,
            R.drawable.profile_11, R.drawable.profile_12, R.drawable.profile_13, R.drawable.profile_14, R.drawable.profile_17,
            R.drawable.profile_18, R.drawable.profile_19
        )
    }

    private val userViewModel: UserViewModel by viewModels()
    private val singleRunViewModel : SingleRunViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter

    var place = arrayOf(
            "노원구" , "강동구", "송파구", "강남구"
    )
    var time = arrayOf(
            "55", "48", "12", "37"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 랜덤 프로필 처리
        var num = Math.random() * profileImgList.size
        profileImg.setImageResource(profileImgList[num.toInt()])

        rankingClick()
        freindClick()
        setupProfile()
        setFontColor()
        setupRecyclerView()

        singleRunViewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })
    }

    private fun setupProfile(){
        userViewModel.userInfo.observe(viewLifecycleOwner, Observer {
            val token = it.token
            val userName = it.userName
            profileTab.text = userName

            if (token != null) {
                RetrofitClient.getInstance().userProfile(token).enqueue(object:Callback<ResponseBody>{
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {


                        val user = Gson().fromJson(response.body()?.string(), Map::class.java)
                        Timber.e("프로필 데이터 받아오기는 하나?, ${user["totalDistance"].toString()}")
                        ranking_num.text = user["totalRank"].toString().replace(".0","")


                        if(user["totalDistance"].toString().equals("null")){
                            distance.text = "달리기를"
                        } else {
                            val distanceData = user["totalDistance"]
                            distance.text = String.format("%.2f", distanceData)+"K"
                        }
                        if(user["bestPace"].toString().equals("null")){
                            pace.text = "시작하세요"
                        } else {
                            // 페이스 텍스트
//                            val paceData: List<String> = user["bestPace"].toString().split("\\.")
                            val paceData = user["bestPace"].toString().replace(".", "' ") + "''"
                            Timber.e("페이스 텍스트 어떻게 나오나요? $paceData")
//                            val paceText = "${paceData[0]}' ${paceData[1]}''"
                            pace.text = paceData
                        }
                    }
                })

                RetrofitClient.getInstance().getFriendList(token).enqueue(object:Callback<ResponseBody>{
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val friend = Gson().fromJson(response.body()?.string(), Map::class.java)
                        if (friend != null) {
                            friend_num.text = friend["friendNum"].toString().replace(".0","")
                        }
                    }

                })
            }
        })
    }

    fun setFontColor() {
        profileTab.setTextColor(Color.rgb(82,40,136))
        ranking_text.setTextColor(Color.rgb(82,40,136))
        friend_text.setTextColor(Color.rgb(82,40,136))
        ranking_num.setTextColor(Color.rgb(82,40,136))
        friend_num.setTextColor(Color.rgb(82,40,136))
        profiledistance.setTextColor(Color.rgb(82,40,136))
        profilepace.setTextColor(Color.rgb(82,40,136))
        recent.setTextColor(Color.rgb(82,40,136))
    }

    private fun setupRecyclerView() = historyListView.apply {
        var link = runAdapterToList()

        runAdapter = RunAdapter(link)
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    // RecyclerView의 Adapter 클래스
    inner class RecyclerAdapter :RecyclerView.Adapter<RecyclerAdapter.ViewHolderClass>(){

        // 항목 구성을 위해 사용할 ViewHolder 객체가 필요할 때 호출되는 메서드
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
            // 항목으로 사용할 View 객체를 생성한다.
            val itemView = layoutInflater.inflate(R.layout.history, null)
            val holder = ViewHolderClass(itemView)

            return holder
        }

        // ViewHolder를 통해 항목을 구성할 때 항목 내의 View 객체에 데이터를 셋팅한다.
        override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
            holder.placeTextView.text = place[position]
            holder.timeTextView.text = time[position]
        }

        // RecyclerView의 항목 개수를 반환한다.
        override fun getItemCount(): Int {
            return place.size
        }

        // ViewHolder 클래스
        inner class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView){
            // 항목 View 내부의 View 객체의 주소값을 담는다.
            val placeTextView = itemView.placeTextView
            val timeTextView = itemView.timeTextView
        }
    }

//    override fun onClick(v: View?) {
//        when(v?.id){
//            R.id.achievement_layout -> if(achievement_layout.visibility == View.VISIBLE){
//                achievement_layout.visibility = View.GONE
//            }else {
//                achievement_layout.visibility = View.VISIBLE
//            }
//        }
//    }

    private fun freindClick(){
        val mainActivity = activity as MainActivity
        val friendFragment = FriendFragment()
        friend_text.setOnClickListener{
            mainActivity.makeCurrentFragment(friendFragment)
        }

        friend_num.setOnClickListener{
            mainActivity.makeCurrentFragment(friendFragment)
        }
    }

    private fun rankingClick(){
        val mainActivity = activity as MainActivity
        val rankingFragment = RankingFragment()

        ranking_text.setOnClickListener{
            mainActivity.makeCurrentFragment(rankingFragment)
        }

        ranking_num.setOnClickListener{
            mainActivity.makeCurrentFragment(rankingFragment)
        }
    }

    inner class runAdapterToList {

        fun getRunningDetailId(run:Run) {
            openRunningDetail(run)
        }
    }

    private fun openRunningDetail(run:Run) {
        val mainActivity = activity as MainActivity
        val runningDetailFragment = RunningDetailFragment(run)

        mainActivity.makeCurrentFragment(runningDetailFragment)
    }
}