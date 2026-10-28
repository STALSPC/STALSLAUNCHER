package com.example.navilauncher

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

data class AppInfo(val label: String, val pkg: String, val icon: Drawable)

object Apps {
    /** 설치된 앱 중 실행 가능한 것들 (자기 자신 제외), 이름순 */
    fun load(ctx: Context): List<AppInfo> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
            .filter { it.pkg != ctx.packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(ctx: Context, pkg: String): Boolean {
        val i = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
        return true
    }
}
