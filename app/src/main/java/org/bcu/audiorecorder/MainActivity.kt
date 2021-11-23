package org.bcu.audiorecorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.media.MediaRecorder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {

    private lateinit var amplitudes: ArrayList<Float>
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var db:AppDatabase
    private var duration=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        db= Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        bottomSheetBehavior=BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.peekHeight=0

        bottomSheetBehavior.state=BottomSheetBehavior.STATE_COLLAPSED


        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        record_btn.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))

        }
        btn_list.setOnClickListener {
            startActivity(Intent(this,GalleryActivity::class.java))
        }
        btn_done.setOnClickListener {

            stopRecording()
            Toast.makeText(this,"Record saved",Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED
            bottom_sheetBG.visibility=View.VISIBLE
            filenameInput.setText(filename)
        }
        buttonCancel.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
           dismiss()
        }
        buttonOkay.setOnClickListener {
            dismiss()
            save()
        }
        bottom_sheetBG.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }
        delete_btn.setOnClickListener {
            stopRecording()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this,"Record deleted",Toast.LENGTH_SHORT).show()
        }

        delete_btn.isClickable=false

    }
    private fun save(){
        val newFileName=filenameInput.text.toString()
        if(newFileName!=filename){
            var newFile=File("$dirPath$newFileName.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }
        var filePath="$dirPath$newFileName.mp3"
        var timestamp=Date().time
        var ampsPath="$dirPath$newFileName"
        try {

            var fos=FileOutputStream(ampsPath)
            var out=ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        }catch (e:IOException){}

        var record=AudioRecord(newFileName,filePath,timestamp,duration,ampsPath)

        GlobalScope.launch {
            db.audioRecordDao().insert(record)
        }
    }
    private fun dismiss(){
        bottom_sheetBG.visibility=View.GONE
        hideKeyBoard(filenameInput)
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state=BottomSheetBehavior.STATE_COLLAPSED
        },100)

    }
    private fun hideKeyBoard(view: View){
        val inp=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inp.hideSoftInputFromWindow(view.windowToken,0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecording() {
        recorder.pause()
        isPaused = true
        record_btn.setImageResource(R.drawable.ic_record)
        timer.pause()
    }

    private fun resumeRecording() {
        recorder.resume()
        isPaused = false
        record_btn.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            try {
                prepare()
            } catch (e: IOException) {
            }
            start()
        }

            record_btn.setImageResource(R.drawable.ic_pause)
            isRecording = true
            isPaused = false
            timer.start()

            delete_btn.isClickable=true
            delete_btn.setImageResource(R.drawable.ic_delete)
            btn_list.visibility=View.GONE
            btn_done.visibility=View.VISIBLE
        }

    private fun stopRecording() {
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused=false
        isRecording=false
        btn_list.visibility= View.VISIBLE
        btn_done.visibility=View.GONE
        delete_btn.isClickable=false
        delete_btn.setImageResource(R.drawable.ic_delete_disabled)
        record_btn.setImageResource(R.drawable.ic_record)
        tvTimer.text="00.00.00"
        amplitudes=waveformView.clear()
    }


    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
        this.duration=duration.dropLast(3)
        waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }
    }



