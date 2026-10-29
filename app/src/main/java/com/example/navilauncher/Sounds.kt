package com.example.navilauncher

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/** 버튼 클릭음 + 런처 시작음 */
object Sounds {
    private var pool: SoundPool? = null
    private var clickId = 0
    private var starter: MediaPlayer? = null

    private fun ensure(ctx: Context) {
        if (pool != null) return
        val sp = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        clickId = sp.load(ctx.applicationContext, R.raw.click, 1)
        pool = sp
    }

    /** 앱 시작 직후 한 번 불러서 클릭음을 미리 로딩 */
    fun init(ctx: Context) = ensure(ctx)

    fun click(ctx: Context) {
        ensure(ctx)
        pool?.play(clickId, 1f, 1f, 1, 0, 1f)
    }

    fun start(ctx: Context) {
        try {
            starter?.release()
            starter = MediaPlayer.create(ctx.applicationContext, R.raw.launcher_start)?.apply {
                setOnCompletionListener { it.release(); if (starter === it) starter = null }
                start()
            }
        } catch (e: Exception) {
            starter = null
        }
    }
}
