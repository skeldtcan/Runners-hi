package com.A306.runnershi.Fragment.SingleRun

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.A306.runnershi.Activity.MainActivity
import com.A306.runnershi.DI.TrackingUtility
import com.A306.runnershi.Model.Run
import com.A306.runnershi.R
import com.A306.runnershi.Services.Polyline
import com.A306.runnershi.Services.TrackingService
import com.A306.runnershi.ViewModel.SingleRunViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.paceText
import kotlinx.android.synthetic.main.fragment_map.timeText
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
//class MapFragment(var link: SingleRunFragment.mapFragmentToSingleRunFragment) : Fragment(R.layout.fragment_map) {
class MapFragment() : Fragment(R.layout.fragment_map) {
    private val viewModel : SingleRunViewModel by viewModels()

    private var curTimeMillis = 0L
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    var run = Run()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        val mainActivity = activity as MainActivity
        val singleRunFragment = SingleRunFragment()
//        val homeFragment = HomeFragment()
//        val runResultFragment = RunResultFragment(run)

        // 나중에 버튼 관련 넣기 (toggle)

        mapView.getMapAsync{
            map = it
            addAllPolylines()
        }

//        subscribeToObservers(mainActivity, homeFragment, runResultFragment)
        subscribeToObservers(mainActivity)

        // 리팩토링 :
//        val here = "MapFrag"
//        val distanceView = null
//        val timeView = timeText
//        val paceView = paceText
//
//        singleRunFragment.globalSubscribeToObservers(here, mainActivity, distanceView, timeView, paceView)

        toMeterButton.setOnClickListener {
            mainActivity.makeCurrentFragment(singleRunFragment, "hide")
        }
    }

//    private fun subscribeToObservers(activity: MainActivity, homeFragment: HomeFragment, runResultFragment: RunResultFragment){
    private fun subscribeToObservers(activity: MainActivity){

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer{
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer{
            curTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeMillis, false)
            timeText.text = formattedTime
        })

        TrackingService.totalPace.observe(viewLifecycleOwner, Observer {
            if (it > 0){
                val formattedPace = TrackingUtility.getPaceWithMilliAndDistance(it)
                paceText.text = formattedPace
            }else{
                paceText.text = "0' 00''"
            }
        })

        TrackingService.totallyFinished.observe(viewLifecycleOwner, Observer{
            Log.i("토털리피니시드", "백그라운드에서도 작동하나요?")
            if (it > 0){
                if (map != null){
                    Timber.e("서비스를 종료합니다.")
                    zoomToSeeWholeTrack()
//                    endRunAndSaveToDb(activity, homeFragment, runResultFragment)
//                    endRunAndPostToResult(activity)
                }else{
                    TrackingService.totallyFinished.postValue(it+1)
                }
            }
        })
    }

//    private fun endRunAndSaveToDb(activity: MainActivity, homeFragment: HomeFragment, runResultFragment: RunResultFragment) {
//    fun endRunAndPostToResult(activity: MainActivity): Run {
//        // TODO "사실 이 기능을 RunResult에 넣어야한다!"
//
//        map?.snapshot { bmp ->
//            Timber.e("사진 저장")
//            Timber.e(map?.toString())
//            // 지금 시간 ( 저장 시에는 끝났을 때 시간 )
//            val dateTimestamp = Calendar.getInstance().timeInMillis
////            val dateTimestamp = Calendar.getInstance().getTime().toString()
//            val distanceInMeters = TrackingService.totalDistance.value!!.toInt()
//            val avgSpeed = ((distanceInMeters / 1000f) / (curTimeMillis / 1000f / 60 / 60) * 10).roundToInt() / 10f
//            //총 걸린 시간
//            val dateTimeSpent = TrackingUtility.getFormattedStopWatchTime(TrackingService.timeRunInMillis.value!!)
//            val finalPace = TrackingUtility.getPaceWithMilliAndDistance(TrackingService.totalPace.value!!)
//            //val timestamp = Calendar.getInstance().timeInMillis
//
//            val title = "${dateTimestamp}의 달리기"
//
//            run = Run(title, bmp, dateTimestamp, avgSpeed, distanceInK, dateTimeSpent, finalPace)
//
//            // 이 run을 다시 SingleRunFragment로 보내줄거야!
////            link.getRunData(run)
//            Log.i("찍히나?", "제발")
//
////            val runResultFragment = RunResultFragment(run)
////
////            activity.makeCurrentFragment(runResultFragment)
////            viewModel.insertRun(run)
////            Toast.makeText(activity.applicationContext, "달리기가 저장됐습니다.", Toast.LENGTH_LONG).show()
//            activity.sendCommandToService("ACTION_STOP_SERVICE")
////            activity.makeCurrentFragment(homeFragment)
//        }
//        return run
//    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        mapView.width,
                        mapView.height,
                        (mapView.height * 0.05f).toInt()
                )
        )
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    15f
                )
            )
        }
    }

    private fun addAllPolylines(){
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop(){
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.onDestroy()
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}