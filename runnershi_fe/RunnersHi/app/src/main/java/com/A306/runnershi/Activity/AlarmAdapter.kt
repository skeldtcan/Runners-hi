package com.A306.runnershi.Activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.A306.runnershi.Model.Alarm
import com.A306.runnershi.R
import timber.log.Timber

class AlarmAdapter(private val alarmList:Array<Alarm>, var link: AlarmActivity.AlarmFragment.acceptFriendAdapterToList, private val alarmFragment: AlarmActivity.AlarmFragment): RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val alarm_nick: TextView = itemView.findViewById(R.id.alarm_nick)
        val alarm_content: TextView = itemView.findViewById(R.id.alarm_content)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmAdapter.AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)

        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmAdapter.AlarmViewHolder, position: Int) {
        val user = alarmList[position]
        Timber.e("user")

        holder.alarm_nick.text = alarmList[position].userName.toString()
        holder.alarm_content.text = alarmList[position].content.toString()

        holder.acceptButton.setOnClickListener {
            Timber.e("여기 뜨나? ${user.userId}")
            link.getFriendId(user)
        }
    }

    override fun getItemCount(): Int = alarmList.size
}