package fastcampus.aop.part2.chapter07

import android.content.pm.PackageManager
import android.media.MediaParser
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val soundVisualizerView: SoundVisualizerView by lazy{
        findViewById(R.id.soundVisualizerView)
    }

    private val resetButton:Button by lazy {
        findViewById(R.id.resetButton)
    }

    private val recordButton:RecordButton by lazy {
        findViewById(R.id.recorButton)
    }

    private val requiredPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    private val recordingFilePath:String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private val CountUpTextView : CountUpTextView by lazy {
        findViewById(R.id.recordTimeTextView)
    }
    private var player :MediaPlayer? = null
    private var recorder:MediaRecorder? = null
    private var state = State.BEFORE_RECORDING

    set(value) {
        field = value
        resetButton.isEnabled= (value == State.AFTER_RECORDING) || (value == State.ON_RECORDING)
        recordButton.updateIconWithState(value)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()

        initView()
        bindView()
        initVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(!audioRecordPermissionGranted){
            finish()
        }

    }

    private fun requestAudioPermission(){
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initView(){
        recordButton.updateIconWithState(state)
    }

    private fun bindView(){
        soundVisualizerView.onRequestCurrentAmplitude={
            recorder?.maxAmplitude?:0
        }
        resetButton.setOnClickListener {
            stopPlaying()
            soundVisualizerView.clearVisualization()
            CountUpTextView.clearCountTime()
            state =State.BEFORE_RECORDING
        }
        recordButton.setOnClickListener {
            when(state){
                State.BEFORE_RECORDING ->{
                    startRecording()
                }
                State.ON_RECORDING->{
                    stopRecording()
                }
                State.AFTER_RECORDING->{
                    startPlaying()
                }
                State.ON_PLAYING->{
                    stopPlaying()
                }
            }
        }
    }

    private fun initVariables(){
        state =State.BEFORE_RECORDING
    }

    private fun startRecording(){
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            prepare()
        }
        recorder?.start()
        soundVisualizerView.startVisualizing(false)
        CountUpTextView.startCountUp()
        state = State.ON_RECORDING
    }

    private fun stopRecording(){
        soundVisualizerView.stopVisualizing()
        recorder?.run{
            stop()
            release()
        }
        CountUpTextView.stopCountUp()
        recorder = null
        state = State.AFTER_RECORDING
    }

    private fun startPlaying(){
        player = MediaPlayer()
            .apply {
                setDataSource(recordingFilePath)
                prepare()
            }
        player?.setOnCompletionListener {
            stopPlaying()
            state= State.AFTER_RECORDING
        }
        player?.start()
        state = State.ON_PLAYING
        CountUpTextView.startCountUp()
        soundVisualizerView.startVisualizing(true)
    }

    private fun stopPlaying(){
        player?.release()
        player = null
        state = State.AFTER_RECORDING
        CountUpTextView.stopCountUp()
        soundVisualizerView.stopVisualizing()
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}