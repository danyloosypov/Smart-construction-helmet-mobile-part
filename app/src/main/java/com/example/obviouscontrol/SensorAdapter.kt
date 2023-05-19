package com.example.obviouscontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obviouscontrol.data.Sensor

class SensorAdapter(private val sensorList: List<Sensor>) : RecyclerView.Adapter<SensorAdapter.ViewHolder>() {
    // View holder class to hold the views for each item in the list

    var onItemClick : ((Sensor) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSensorName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvSensorDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sensor_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensor = sensorList[position]
        holder.tvName.text = sensor.name
        holder.tvDescription.text = sensor.description

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(sensor)
        }
    }

    override fun getItemCount(): Int {
        return sensorList.size
    }
}
