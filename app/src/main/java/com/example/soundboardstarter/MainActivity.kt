package com.example.soundboardstarter

import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.soundboardstarter.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    private lateinit var soundPool : SoundPool
    var aNote = 0
    var bbNote = 0
    var bNote = 0
    var cNote = 0
    var csNote = 0
    var dNote = 0
    var dsNote = 0
    var eNote = 0
    var fNote = 0
    var fsNote = 0
    var gNote = 0
    var gsNote = 0

    private val noteMap = HashMap<String, Int>()
    private lateinit var binding: ActivityMainBinding

    private var currentOctave = 4
    private val minOctave = 1
    private val maxOctave = 7

    private var isPlaying = false
    private var shouldStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var gson = Gson()
        val inputStream = resources.openRawResource(R.raw.song)
        val jsonString = inputStream.bufferedReader().use {
            it.readText()
        }
        val type = object : TypeToken<List<Note>>(){}.type
        val notes = gson.fromJson<List<Note>>(jsonString, type)
        Log.d(TAG, "onCreate: $notes")

        initializeSoundPool()
        setListeners()
        updateOctaveButtons()
    }

    private fun stringToNotes(song : String): List<Note>{
        val trimmed = song.split(" ")
        val notes = ArrayList<Note>()

        /*for (i in trimmed.indices step 2){
            notes.add(Note(trimmed[i + 1].toInt(), trimmed[i]))*/
        var i = 0
        while (i + 1 < trimmed.size) {
            val name = trimmed[i]
            val duration = trimmed[i + 1].toInt()
            notes.add(Note(duration, name))
            i += 2
        }

        return notes
    }


    private fun stringToChords(song : String): List<Chord> {
        val trimmed = song.split(" ")
        val chords = ArrayList<Chord>()

        var i = 0
        while (i + 1 < trimmed.size) {
            val chord = trimmed[i]
            val notes = chord.split("-")
            val duration = trimmed[i + 1].toInt()
            chords.add(Chord(duration, notes))
            i += 2
        }
        return chords
    }

    private suspend fun playSong(song: String) {
        val trimmed = song.split(" ")

        var i = 0
        var lastOctave = 4

        while (i + 1 < trimmed.size && !shouldStop) {
            val notes = trimmed[i]
            val duration = trimmed[i + 1].toLong()

            val note = notes.split("-")
            for (i in note) {
                lastOctave = playSongNote(i, lastOctave)
            }

            delay(duration)
            i += 2
        }
    }

    private fun playSongNote(note: String, lastOctave: Int): Int {
        var base: String
        var octave: String

        if (note.length >= 2 && (note[1] == 'b' || note[1] == 's')) {
            base = note.substring(0, 2)
            if (note.length > 2) octave = note.substring(2)
            else octave = ""
        } else {
            base = note.substring(0, 1)
            if (note.length > 1) octave = note.substring(1)
            else octave = ""
        }

        val noteId = noteMap[base] ?: 0

        if (octave == "") {
            val rate = octaveToRate(lastOctave)
            soundPool.play(noteId, 1f, 1f, 1, 0, rate)

            return lastOctave
        } else {
            val rate = octaveToRate(octave.toInt())
            soundPool.play(noteId, 1f, 1f, 1, 0, rate)

            return octave.toInt()
        }
    }


    private suspend fun playSong(song: List<Note>) {
        /*withContext(Dispatchers.Main) {
            binding.buttonMainPlaySong.text = "playing song"
        }
        */


        for (i in song) {
            if (shouldStop) break
            playNote(i.note)
            delay(i.duration.toLong())
        }

        /*withContext(Dispatchers.Main) {
            binding.buttonMainPlaySong.text = "play song"
        }
        */
    }

    private suspend fun playTest(){
        //val testSong = "A 500 B 500 A 500 A 250 G 250 A 500"
        //playSong(stringToNotes(testSong))
        val testSong = "Bb-D-F 500 Bb3 500 Gs5 500"
        playSong(testSong)
    }

    private fun delay(time: Long) {
        try {
            Thread.sleep(time)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
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
        csNote = soundPool.load(this, R.raw.scalecs, 1)
        dNote = soundPool.load(this, R.raw.scaled, 1)
        dsNote = soundPool.load(this, R.raw.scaleds, 1)
        eNote =  soundPool.load(this, R.raw.scalee, 1)
        fNote = soundPool.load(this, R.raw.scalef, 1)
        fsNote = soundPool.load(this, R.raw.scalefs, 1)
        gNote = soundPool.load(this, R.raw.scaleg, 1)
        gsNote =  soundPool.load(this, R.raw.scalegs, 1)

        noteMap["A"] = aNote
        noteMap["Bb"] = bbNote
        noteMap["B"] = bNote
        noteMap["C"] = cNote
        noteMap["Cs"] = csNote
        noteMap["D"] = dNote
        noteMap["Ds"] = dsNote
        noteMap["E"] = eNote
        noteMap["F"] = fNote
        noteMap["Fs"] = fsNote
        noteMap["G"] = gNote
        noteMap["Gs"] = gsNote
    }


    private fun setListeners() {
        val soundBoardListener = SoundBoardListener()
        binding.buttonMainA.setOnClickListener(soundBoardListener)
        binding.buttonMainBb.setOnClickListener(soundBoardListener)
        binding.buttonMainB.setOnClickListener(soundBoardListener)
        binding.buttonMainC.setOnClickListener(soundBoardListener)
        binding.buttonMainCs.setOnClickListener(soundBoardListener)
        binding.buttonMainD.setOnClickListener(soundBoardListener)
        binding.buttonMainDs.setOnClickListener(soundBoardListener)
        binding.buttonMainE.setOnClickListener(soundBoardListener)
        binding.buttonMainF.setOnClickListener(soundBoardListener)
        binding.buttonMainFs.setOnClickListener(soundBoardListener)
        binding.buttonMainG.setOnClickListener(soundBoardListener)
        binding.buttonMainGs.setOnClickListener(soundBoardListener)

        binding.buttonMainPlaySong.setOnClickListener {
            if (!isPlaying) {
                isPlaying = true
                shouldStop = false
                binding.buttonMainPlaySong.text = "pause song"

                CoroutineScope(Dispatchers.IO).launch {
                    playTest()

                    withContext(Dispatchers.Main) {
                        isPlaying = false
                        shouldStop = false
                        binding.buttonMainPlaySong.text = "play song"
                    }
                }
            } else {
                shouldStop = true
                binding.buttonMainPlaySong.text = "play song"
            }
        }

        binding.buttonMainOctaveMinus.setOnClickListener {
            if (currentOctave > minOctave) {
                currentOctave--
                updateOctaveButtons()
            }
        }

        binding.buttonMainOctavePlus.setOnClickListener {
            if (currentOctave < maxOctave) {
                currentOctave++
                updateOctaveButtons()
            }
        }
    }

    private fun updateOctaveButtons() {
        var minText = ""
        var maxText = ""

        if (currentOctave == minOctave) {
            minText = "min"
        } else minText = "octave ${currentOctave - 1}"

        if (currentOctave == maxOctave) {
            maxText = "max"
        } else maxText = "octave ${currentOctave + 1}"

        binding.buttonMainOctaveMinus.text = minText
        binding.buttonMainOctavePlus.text = maxText

        binding.buttonMainOctaveMinus.isEnabled = currentOctave > minOctave
        binding.buttonMainOctavePlus.isEnabled = currentOctave < maxOctave
    }

    private fun playNote(note: String) {
        playNote(noteMap[note] ?: 0)
    }

    private fun playNote(noteId : Int) {
        //if (noteId == 0) return
        val rate = octaveToRate(currentOctave)

        soundPool.play(noteId, 1f, 1f, 1, 0, rate)
    }

    private fun octaveToRate(octave: Int): Float {
        return when (octave) {
            1 -> 0.125f
            2 -> 0.25f
            3 -> 0.5f
            4 -> 1f
            5 -> 2f
            6 -> 4f
            7 -> 8f
            else -> 1f
        }
    }

    private inner class SoundBoardListener : View.OnClickListener {
        override fun onClick(v: View?) {
            when(v?.id) {
                R.id.button_main_a -> playNote(aNote)
                R.id.button_main_bb -> playNote(bbNote)
                R.id.button_main_b -> playNote(bNote)
                R.id.button_main_c -> playNote(cNote)
                R.id.button_main_cs -> playNote(csNote)
                R.id.button_main_d -> playNote(dNote)
                R.id.button_main_ds -> playNote(dsNote)
                R.id.button_main_e -> playNote(eNote)
                R.id.button_main_f -> playNote(fNote)
                R.id.button_main_fs -> playNote(fsNote)
                R.id.button_main_g -> playNote(gNote)
                R.id.button_main_gs -> playNote(gsNote)
            }
        }
    }
}