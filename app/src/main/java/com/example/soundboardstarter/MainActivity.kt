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
    var dbNote = 0
    var dNote = 0
    var ebNote = 0
    var eNote = 0
    var fNote = 0
    var gbNote = 0
    var gNote = 0
    var abNote = 0

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
        //val testSong = "Bb-D-F 500 Bb3 500 Gs5 500"
        val virtualInsanity1 = "Bb3 150 Eb4-Eb3 300 Bb4-Eb-Gb 800 Eb3 150 Ab2-Ab3 300 C4-Eb-Gb 800 Ab2 150 Db2-Db3 300 B4-Eb-F-Ab 800 Db3 150 Gb2-Gb3 300 Bb4-Db-F-Ab 800 Gb2 150 C2-C3 300 Bb4-Eb-Gb 800 C3 150 B2-B3 300 B4-Eb-Gb 800 B3 150 Bb2-Bb3 300 Bb4-D-F 600"
        val virtualInsanity2 = "Bb2-Bb3-Bb5-Db-Gb 200 Bb2-Bb3-Ab5 100 Db5-Bb6 200 Bb3-Db5-Bb6 100 Eb2-Eb3 200 Bb5 100 Db5-Ab 200 Eb3-Bb5 100 Eb4-Db5-Gb5 200 Bb4-Bb5 100 Eb3-Db5 200 Gb4-Ab-Bb5-Eb 100 Ab2-Gb3 200 Gb4-Ab-Bb5-Eb 300 Ab3-Gb5 150 Gb4 150 Eb4 150 Ab3-Db5 250 Bb2-Bb3-Bb5-Eb-F-Ab 300 Ab5 200 Db3-Gb5 150 Db4 130 Ab3-Db5 150 Db3-Eb5 150 F4-Bb5-Eb5 150 Gb2-F3 150 F4-Bb5-Eb5 300 Gb3-F4-Bb5 130 F4 130 Db4 100 Gb3 220 Bb5 100 C2-C3-Db5 200 Eb5 100 Bb5 200 C3-Bb5-C-Eb-Gb 300 C4 100 Bb4 100 C3 100 Bb5 200 B2-B3-Db5 200 Eb5 100 Bb5 200 B3-Bb5-B-Eb-Gb 300 Eb4 100 Bb4 100 B3 200 Ab4-B5-D5-Gb5"
        //not an ounce of math or logic was used here

        playSong(virtualInsanity1)
        playSong(virtualInsanity2)
        //listen to the rest here! https://www.youtube.com/watch?v=4JkIs37a2JE
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
        dbNote = soundPool.load(this, R.raw.scalecs, 1)
        dNote = soundPool.load(this, R.raw.scaled, 1)
        ebNote = soundPool.load(this, R.raw.scaleds, 1)
        eNote =  soundPool.load(this, R.raw.scalee, 1)
        fNote = soundPool.load(this, R.raw.scalef, 1)
        gbNote = soundPool.load(this, R.raw.scalefs, 1)
        gNote = soundPool.load(this, R.raw.scaleg, 1)
        abNote =  soundPool.load(this, R.raw.scalegs, 1)

        noteMap["A"] = aNote
        noteMap["Bb"] = bbNote
        noteMap["B"] = bNote
        noteMap["C"] = cNote
        noteMap["Db"] = dbNote
        noteMap["D"] = dNote
        noteMap["Eb"] = ebNote
        noteMap["E"] = eNote
        noteMap["F"] = fNote
        noteMap["Gb"] = gbNote
        noteMap["G"] = gNote
        noteMap["Ab"] = abNote
    }


    private fun setListeners() {
        val soundBoardListener = SoundBoardListener()
        binding.buttonMainA.setOnClickListener(soundBoardListener)
        binding.buttonMainBb.setOnClickListener(soundBoardListener)
        binding.buttonMainB.setOnClickListener(soundBoardListener)
        binding.buttonMainC.setOnClickListener(soundBoardListener)
        binding.buttonMainDb.setOnClickListener(soundBoardListener)
        binding.buttonMainD.setOnClickListener(soundBoardListener)
        binding.buttonMainEb.setOnClickListener(soundBoardListener)
        binding.buttonMainE.setOnClickListener(soundBoardListener)
        binding.buttonMainF.setOnClickListener(soundBoardListener)
        binding.buttonMainGb.setOnClickListener(soundBoardListener)
        binding.buttonMainG.setOnClickListener(soundBoardListener)
        binding.buttonMainAb.setOnClickListener(soundBoardListener)

        binding.buttonMainPlaySong.setOnClickListener {
            if (!isPlaying) {
                isPlaying = true
                shouldStop = false
                binding.buttonMainPlaySong.text = "stop song"

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
                isPlaying = false
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
                R.id.button_main_db -> playNote(dbNote)
                R.id.button_main_d -> playNote(dNote)
                R.id.button_main_eb -> playNote(ebNote)
                R.id.button_main_e -> playNote(eNote)
                R.id.button_main_f -> playNote(fNote)
                R.id.button_main_gb -> playNote(gbNote)
                R.id.button_main_g -> playNote(gNote)
                R.id.button_main_ab -> playNote(abNote)
            }
        }
    }
}