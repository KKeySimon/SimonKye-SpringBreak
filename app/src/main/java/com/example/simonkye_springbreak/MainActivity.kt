package com.example.simonkye_springbreak

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simonkye_springbreak.databinding.ActivityMainBinding
import java.util.Locale
import java.util.Objects
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var textToSpeech: TextToSpeech

    private val REQUEST_CODE_SPEECH_INPUT = 1

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Source: https://www.geeksforgeeks.org/how-to-convert-text-to-speech-in-android/
        textToSpeech = TextToSpeech(applicationContext) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.UK)
            }
        }

        // Source: https://www.geeksforgeeks.org/speech-to-text-application-in-android-with-kotlin/
        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val langCode = when (findViewById<RadioButton>(checkedId).text.toString()) {
                "English" -> "en"
                "Korean" -> "ko"
                "Chinese" -> "zh"
                else -> "en"
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                langCode
            )

            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something in the selected language")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast
                    .makeText(
                        this@MainActivity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            // Fetching x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 10) {
                val checkedButton = binding.languageRadioGroup.checkedRadioButtonId
                if (checkedButton != -1) {
                    val coordinate : Pair<Double, Double> = when (findViewById<RadioButton>(checkedButton).text.toString()) {
                        "English" -> {
                            textToSpeech.setLanguage(Locale.UK)
                            textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, null)
                            if (Random.nextBoolean()) Pair(51.51, 0.13) else Pair(53.41, 2.99)
                        }
                        "Korean" -> {
                            textToSpeech.setLanguage(Locale.KOREAN)
                            textToSpeech.speak("안녕하세요", TextToSpeech.QUEUE_FLUSH, null, null)
                            if (Random.nextBoolean()) Pair(37.55, 126.99) else Pair(35.21,129.07)
                        }
                        "Chinese" -> {
                            textToSpeech.setLanguage(Locale.CHINESE)
                            // Android's built in TTS doesn't support Chinese so I'll simply write out the pinyin for it
                            textToSpeech.speak("ni hao", TextToSpeech.QUEUE_FLUSH, null, null)
                            if (Random.nextBoolean()) Pair(39.9, 116.41) else Pair(31.23, 121.47)
                        }
                        else -> {
                            Pair(0.0, 0.0)
                        }
                    }
                    // Source https://stackoverflow.com/questions/22704451/open-google-maps-through-intent-for-specific-location-in-android
                    val geoUri = "geo:${coordinate.first},${coordinate.second}?z=10"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                    startActivity(intent)
                }

            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(sensorListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                binding.editText.setText(
                    Objects.requireNonNull(res)[0]
                )
            }
        }
    }
}