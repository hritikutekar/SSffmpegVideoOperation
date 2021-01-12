package com.simform.videoimageeditor.otherFFMPEGProcessActivity

import android.view.View
import android.widget.Toast
import com.arthenica.mobileffmpeg.LogMessage
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.utility.Common
import com.simform.videoimageeditor.utility.FFmpegCallBack
import com.simform.videoimageeditor.utility.FFmpegQueryExtension
import java.util.concurrent.CyclicBarrier
import kotlinx.android.synthetic.main.activity_fast_and_slow_audio.btnMotion
import kotlinx.android.synthetic.main.activity_fast_and_slow_audio.btnAudioPath
import kotlinx.android.synthetic.main.activity_fast_and_slow_audio.mProgressView
import kotlinx.android.synthetic.main.activity_fast_and_slow_audio.motionType
import kotlinx.android.synthetic.main.activity_fast_and_slow_audio.tvInputPathAudio
import kotlinx.android.synthetic.main.activity_fast_and_slow_audio.tvOutputPath

class FastAndSlowAudioActivity : BaseActivity(R.layout.activity_fast_and_slow_audio, R.string.fast_slow_motion_video) {
    private var isInputAudioSelected: Boolean = false
    override fun initialization() {
        btnAudioPath.setOnClickListener(this)
        btnMotion.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnAudioPath -> {
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = true)
            }
            R.id.btnMotion -> {
                when {
                    !isInputAudioSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        val gate = CyclicBarrier(2)
                        val imageToVideo = object : Thread() {
                            override fun run() {
                                gate.await()
                                motionProcess()
                            }
                        }
                        imageToVideo.start()
                        gate.await()
                    }
                }
            }
        }
    }

    private fun motionProcess() {
        val outputPath = Common.getFilePath(this, Common.MP3)
        var atempo = 2.0
        if (!motionType.isChecked) {
            atempo = 0.5
        }
        val query = FFmpegQueryExtension.audioMotion(tvInputPathAudio.text.toString(), outputPath, atempo)
        Common.callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                tvOutputPath.text = logMessage.text
            }

            override fun success() {
                tvOutputPath.text = "Output Path : \n$outputPath"
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.AUDIO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathAudio.text = mediaFiles[0].path
                    isInputAudioSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.audio_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processStop() {
        runOnUiThread {
            btnAudioPath.isEnabled = true
            btnMotion.isEnabled = true
            mProgressView.visibility = View.GONE
        }
    }

    private fun processStart() {
        btnAudioPath.isEnabled = false
        btnMotion.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }
}