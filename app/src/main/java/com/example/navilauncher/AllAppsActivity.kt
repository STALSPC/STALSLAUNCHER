package com.example.navilauncher

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.GridView

class AllAppsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val apps = Apps.load(this)
        findViewById<GridView>(R.id.grid).apply {
            adapter = AppAdapter(apps)
            setOnItemClickListener { _, _, pos, _ ->
                Apps.launch(this@AllAppsActivity, apps[pos].pkg)
            }
        }
    }
}
