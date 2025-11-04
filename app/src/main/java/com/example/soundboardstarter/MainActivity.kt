package com.example.soundboardstarter

import android.media.AudioManager
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    lateinit var buttonA : Button
    lateinit var buttonBb : Button
    lateinit var buttonB : Button
    lateinit var buttonC : Button
    lateinit var soundPool : SoundPool
    var aNote = 0
    var bbNote = 0
    var bNote = 0
    var cNote = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wireWidgets()
        initializeSoundPool()
        setListeners()
    }

    private fun initializeSoundPool() {

        this.volumeControlStream = AudioManager.STREAM_MUSIC
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
//        soundPool.setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
//           // isSoundPoolLoaded = true
//        })
        aNote = soundPool.load(this, R.raw.scalea, 1)
        bbNote = soundPool.load(this, R.raw.scalebb, 1)
        bNote = soundPool.load(this, R.raw.scaleb, 1)
        cNote =  soundPool.load(this, R.raw.scalec, 1)
    }

    private fun wireWidgets() {
        buttonA = findViewById(R.id.button_main_a)
        buttonBb = findViewById(R.id.button_main_bb)
        buttonB = findViewById(R.id.button_main_b)
        buttonC = findViewById(R.id.button_main_c)
    }

    private fun setListeners() {
        val soundBoardListener = SoundBoardListener()
        buttonA.setOnClickListener(soundBoardListener)
        buttonBb.setOnClickListener(soundBoardListener)
        buttonB.setOnClickListener(soundBoardListener)
        buttonC.setOnClickListener(soundBoardListener)
    }


    private fun playNote(noteId : Int) {
        soundPool.play(noteId, 1f, 1f, 1, 0, 1f)
    }

    private inner class SoundBoardListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.button_main_a -> playNote(aNote)
                R.id.button_main_bb -> playNote(bbNote)
                R.id.button_main_b -> playNote(bNote)
                R.id.button_main_c -> playNote(cNote)
            }
        }
    }
}