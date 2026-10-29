package com.example.navilauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("slots", Context.MODE_PRIVATE) }
    private val recentPrefs by lazy { getSharedPreferences("recent", Context.MODE_PRIVATE) }
    private val slotIds = intArrayOf(R.id.slot0, R.id.slot1, R.id.slot2)

    private lateinit var searchInput: EditText
    private lateinit var recentList: LinearLayout
    private lateinit var digitalTime: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val timeFmt = SimpleDateFormat("hh:mm a", Locale.US) // 12시간제, AM/PM
    private val timeTick = object : Runnable {
        override fun run() {
            updateDigitalTime()
            handler.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Sounds.init(this)
        Sounds.start(this) // 런처가 켜질 때
        digitalTime = findViewById(R.id.digitalTime)

        // 위 3개 버튼: 짧게 = 실행, 꾹 = 앱 지정
        slotIds.forEachIndexed { index, id ->
            val slot = findViewById<View>(id)
            slot.setOnClickListener { onSlotClick(index) }
            slot.setOnLongClickListener { pickApp(index); true }
        }

        // 맨 아래 버튼: 전체 앱
        findViewById<View>(R.id.btnAllApps).setOnClickListener {
            Sounds.click(this)
            startActivity(Intent(this, AllAppsActivity::class.java))
        }

        // 구글 검색
        searchInput = findViewById(R.id.searchInput)
        recentList = findViewById(R.id.recentList)
        searchInput.setOnEditorActionListener { _, actionId, event ->
            val enter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                doSearch(searchInput.text.toString()); true
            } else false
        }
        findViewById<View>(R.id.searchIcon).setOnClickListener {
            Sounds.click(this); doSearch(searchInput.text.toString())
        }

        // 휴지통 버튼: 최근 검색 전체 삭제
        findViewById<View>(R.id.btnClear).setOnClickListener {
            Sounds.click(this)
            if (loadRecent().isEmpty()) return@setOnClickListener
            AlertDialog.Builder(this)
                .setMessage("최근 검색을 모두 지울까요?")
                .setPositiveButton("지우기") { _, _ -> saveRecent(emptyList()); renderRecent() }
                .setNegativeButton("취소", null)
                .show()
        }

        refreshSlots()
        renderRecent()
    }

    override fun onResume() {
        super.onResume()
        refreshSlots()
        renderRecent()
        handler.removeCallbacks(timeTick)
        handler.post(timeTick)
    }

    override fun onPause() {
        handler.removeCallbacks(timeTick)
        super.onPause()
    }

    /** 이미 켜져 있는 상태에서 홈 버튼으로 런처가 다시 올라올 때 */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            Sounds.start(this)
        }
    }

    private fun updateDigitalTime() {
        val text = timeFmt.format(Date())          // 예: "11:24 PM"
        val sp = SpannableString(text)
        val ampm = text.indexOf(' ') + 1           // AM/PM 부분은 작게
        sp.setSpan(RelativeSizeSpan(0.5f), ampm, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        digitalTime.text = sp
    }

    // ---------- 최근 검색 ----------
    private fun loadRecent(): MutableList<String> =
        (recentPrefs.getString("list", "") ?: "").split("\n").filter { it.isNotBlank() }.toMutableList()

    private fun saveRecent(list: List<String>) {
        recentPrefs.edit().putString("list", list.joinToString("\n")).apply()
    }

    private fun doSearch(raw: String) {
        val query = raw.trim()
        if (query.isEmpty()) return
        val list = loadRecent()
        list.remove(query)
        list.add(0, query)
        saveRecent(list.take(8))

        searchInput.setText("")
        searchInput.clearFocus()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(searchInput.windowToken, 0)
        renderRecent()
        openGoogle(query)
    }

    private fun openGoogle(query: String) {
        val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            toast("검색을 열 수 없어요")
        }
    }

    private fun renderRecent() {
        recentList.removeAllViews()
        val list = loadRecent()
        if (list.isEmpty()) {
            recentList.addView(row("최근 검색 기록이 없어요", dim = true))
            return
        }
        list.forEach { q ->
            val tv = row(q, dim = false)
            tv.setOnClickListener { Sounds.click(this); openGoogle(q); bump(q) }
            tv.setOnLongClickListener {
                val l = loadRecent(); l.remove(q); saveRecent(l); renderRecent(); true
            }
            recentList.addView(tv)
        }
    }

    /** 다시 눌러 검색한 항목을 맨 위로 */
    private fun bump(q: String) {
        val l = loadRecent(); l.remove(q); l.add(0, q); saveRecent(l); renderRecent()
    }

    private fun row(text: String, dim: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(getColor(if (dim) R.color.sub else R.color.text))
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        setPadding(dp(14), dp(11), dp(14), dp(11))
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    // ---------- 바로가기 슬롯 ----------
    private fun onSlotClick(index: Int) {
        Sounds.click(this)
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
            Sounds.click(this)
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
