package com.example.navilauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("slots", Context.MODE_PRIVATE) }
    private val slotIds = intArrayOf(R.id.slot0, R.id.slot1, R.id.slot2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 위 3개 버튼: 짧게 = 실행, 꾹 = 앱 지정
        slotIds.forEachIndexed { index, id ->
            val slot = findViewById<View>(id)
            slot.setOnClickListener { onSlotClick(index) }
            slot.setOnLongClickListener { pickApp(index); true }
        }

        // 맨 아래 버튼: 전체 앱
        findViewById<View>(R.id.btnAllApps).setOnClickListener {
            startActivity(Intent(this, AllAppsActivity::class.java))
        }

        findViewById<View>(R.id.searchBox).setOnClickListener { toast("통합검색은 준비 중이에요") }
        findViewById<View>(R.id.btnEdit).setOnClickListener { toast("왼쪽 위 3개 버튼을 꾹 눌러 앱을 지정하세요") }

        refreshSlots()
    }

    override fun onResume() {
        super.onResume()
        refreshSlots()
    }

    private fun onSlotClick(index: Int) {
        val pkg = prefs.getString("slot$index", null)
        if (pkg == null) {
            toast("버튼을 꾹 눌러 앱을 지정하세요")
        } else if (!Apps.launch(this, pkg)) {
            toast("앱을 실행할 수 없어요 (삭제됐을 수 있어요)")
        }
    }

    private fun pickApp(index: Int) {
        val apps = Apps.load(this)
        val grid = GridView(this).apply {
            numColumns = 4
            adapter = AppAdapter(apps)
            setPadding(8, 8, 8, 8)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("바로가기에 넣을 앱 선택")
            .setView(grid)
            .setNegativeButton("취소", null)
            .setNeutralButton("비우기") { _, _ ->
                prefs.edit().remove("slot$index").apply()
                refreshSlots()
            }
            .create()
        grid.setOnItemClickListener { _, _, pos, _ ->
            prefs.edit().putString("slot$index", apps[pos].pkg).apply()
            refreshSlots()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun refreshSlots() {
        slotIds.forEachIndexed { index, id ->
            val slot = findViewById<View>(id)
            val icon = slot.findViewById<ImageView>(R.id.slotIcon)
            val label = slot.findViewById<TextView>(R.id.slotLabel)
            val pkg = prefs.getString("slot$index", null)
            try {
                if (pkg == null) throw IllegalStateException()
                val ai = packageManager.getApplicationInfo(pkg, 0)
                icon.setImageDrawable(packageManager.getApplicationIcon(ai))
                label.text = packageManager.getApplicationLabel(ai)
            } catch (e: Exception) {
                icon.setImageResource(R.drawable.ic_add)
                label.text = "꾹 눌러 설정"
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* 런처는 뒤로가기로 종료하지 않음 */ }
}
