package com.example.ch15_outer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*

class MyMessengerService : Service() {

    //activity 의 데이터를 받기 위한 messenger
    lateinit var messenger: Messenger
    //activity 에 데이터를 전달하기 위한 messenger
    //activity 에서 만들어 전달해준다.
    lateinit var replyMessenger: Messenger

    lateinit var player: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    //activity 로부터 메시지가 전달 되었을때..
    inner class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                10 -> {
                    //service connection 되자마자 전달되는 메시지다.
                    replyMessenger = msg.replyTo

                    if (!player.isPlaying) {
                        player = MediaPlayer.create(this@MyMessengerService, R.raw.music)
                        try {
                            //duration 전송....
                            val replyMsg= Message()
                            replyMsg.what=10

                            val replyBundle= Bundle()
                            replyBundle.putInt("duration", player.duration)
                            replyMsg.obj=replyBundle

                            replyMessenger.send(replyMsg)

                            //음악 play.....
                            player.start()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                20 ->{
                    //stop 메시지
                    if (player.isPlaying)
                        player.stop()
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        messenger = Messenger(IncomingHandler(this))
        return messenger.binder
    }

}