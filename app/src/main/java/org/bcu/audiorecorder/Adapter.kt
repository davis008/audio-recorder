package org.bcu.audiorecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Adapter(var records: ArrayList<AudioRecord>,var listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    inner class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) ,View.OnClickListener,View.OnLongClickListener{
        var tfFileName: TextView = itemview.findViewById(R.id.tvFileName)
        var tfMeta: TextView = itemview.findViewById(R.id.tvMeta)
        var checkBox: CheckBox = itemview.findViewById(R.id.checkbox)

        init {

            itemview.setOnClickListener(this)
            itemview.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position=adapterPosition
            if(position!=RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)

        }

        override fun onLongClick(v: View?): Boolean {
            val position=adapterPosition
            if(position!=RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            var record = records[position]
            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)
            holder.tfFileName.text = record.filename
            holder.tfMeta.text = "${record.duration}$strDate"
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }
}