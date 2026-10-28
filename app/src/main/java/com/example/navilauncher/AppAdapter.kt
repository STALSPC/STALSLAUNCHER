package com.example.navilauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class AppAdapter(private val apps: List<AppInfo>) : BaseAdapter() {
    override fun getCount() = apps.size
    override fun getItem(position: Int) = apps[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        val app = apps[position]
        v.findViewById<ImageView>(R.id.appIcon).setImageDrawable(app.icon)
        v.findViewById<TextView>(R.id.appLabel).text = app.label
        return v
    }
}
