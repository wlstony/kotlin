package com.example.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapp.R

class DeviceAdapter(private val devices: List<BluetoothDevice>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    private val blueDebug = "blue_debug"
    private var selectedPosition = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(blueDebug,"onBindViewHolder")

        // 设置点击监听器
        holder.itemView.setOnClickListener {
            // 清除之前选中的项（如果有的话）
            notifyItemChanged(selectedPosition)

            // 更新选中项并通知适配器更新
            selectedPosition = position
            notifyItemChanged(position)
        }

        // 根据当前项是否被选中来设置外观
        holder.itemView.isSelected = position == selectedPosition
        // 你可以根据 isSelected 来改变背景颜色、图标等
        if (holder.itemView.isSelected) {
            holder.itemView.setBackgroundColor(Color.BLUE) // 假设选中项的背景是蓝色
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE) // 未选中项的背景是白色
        }

        val device = devices[position]

        holder.deviceName.text = device.address
    }

    override fun getItemCount(): Int = devices.size

    fun getSelectedPosition():Int {
        return selectedPosition
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.tv_item)
        // 如果有其他视图，可以在这里初始化它们
    }

}