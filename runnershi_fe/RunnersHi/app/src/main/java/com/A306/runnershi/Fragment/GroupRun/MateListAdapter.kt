package com.A306.runnershi.Fragment.GroupRun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.A306.runnershi.Openvidu.model.Participant
import com.A306.runnershi.R


class MateListAdapter(private var list:  MutableList<Participant>): RecyclerView.Adapter<MateListAdapter.ListItemViewHolder> () {

    inner class ListItemViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
        var mateName: TextView = itemView!!.findViewById(R.id.mateName)
        var mateDistance: TextView = itemView!!.findViewById(R.id.mateDistance)
        var mateTime: TextView = itemView!!.findViewById(R.id.mateTime)
        var matePace: TextView = itemView!!.findViewById(R.id.matePace)


        fun bind(data: Participant, position: Int) {
            mateName.text = data.participantName

            // 나중에는 기록들도 연결해줄 것
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grouprun_mate, parent, false)
        return ListItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: MateListAdapter.ListItemViewHolder, position: Int) {
        holder.bind(list[position], position)
    }
}