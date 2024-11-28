package com.example.androidstudioproject

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.view.LayoutInflater
import android.widget.TextView
import com.example.androidstudioproject.ui.theme.AndroidStudioProjectTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.net.ssl.TrustManagerFactory
import java.security.cert.CertificateFactory
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONException
import org.json.JSONObject
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext


class MainActivity : ComponentActivity() {
    private var buttonVisible by mutableStateOf(false)
    private var filePickerVisible by mutableStateOf(true)
    private var textFieldVisible by mutableStateOf(false)
    private var textFieldValue by mutableStateOf("")
    private var globalToken: String? = null
    private var statusJob: Job? = null // Job to control the coroutine execution


    private val audioPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Log.d("AudioPicker", "Selected Audio URI: $uri")
            uriToFile(it)?.let { file ->
                sendAudioToBackend(file)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            AndroidStudioProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp), // Add padding for better spacing
                        verticalArrangement = Arrangement.spacedBy(16.dp), // Space between elements
                        horizontalAlignment = Alignment.CenterHorizontally // Center elements horizontally
                    ) {
                        if (filePickerVisible) {
                            AudioItemLayout {
                                audioPickerLauncher.launch("audio/*")
                            }
                        }
                        Button(
                            onClick = { handleButtonClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (buttonVisible) 100.dp else 0.dp)
                                .alpha(if (buttonVisible) 1f else 0f)
                                .padding(innerPadding)
                        ) {
                            Text("Check Status", style = MaterialTheme.typography.bodyLarge)
                        }
                        if (textFieldVisible) {
                            TextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                label = { Text("Output:") },
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    private fun handleButtonClick() {
        Log.d("ButtonClicked", "clicked")
        // Cancel any existing status checks
        statusJob?.cancel()
        // Start a new coroutine to check the status every 10 seconds
        statusJob = CoroutineScope(Dispatchers.IO).launch {
            checkStatusPeriodically()
        }
    }

    private fun checkTranscriptionStatus() {
        val client = createUnsafeOkHttpClient()

        val statusUrl = "https://192.168.1.89:5000/status/$globalToken"

        val request = Request.Builder()
            .url(statusUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("StatusError", "Error checking status", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        Log.d("StatusResponse", "Status Response: $it")

                        try {
                            val jsonResponse = JSONObject(it)
                            val code = jsonResponse.getString("status")

                            when (code) {
                                "success" -> {
                                    val transcription = jsonResponse.getString("transcription")
                                    textFieldVisible = true
                                    textFieldValue = transcription
                                    buttonVisible = false
                                    filePickerVisible = true
                                    Log.d("Transcription", transcription)
                                }
                                "in_queue" -> {
                                    val queuePosition = jsonResponse.getInt("queue_position")
                                    textFieldVisible = true
                                    textFieldValue = "In Queue: $queuePosition"
                                    buttonVisible = true
                                    filePickerVisible = false
                                    Log.d("Queue Position", "Position: $queuePosition")
                                }
                                "in_progress" -> {
                                    val processingStatus = jsonResponse.getString("processing")
                                    textFieldVisible = true
                                    textFieldValue = processingStatus
                                    buttonVisible = true
                                    filePickerVisible = false
                                    Log.d("Processing Status", processingStatus)
                                }
                                else -> {
                                    Log.d("Unknown Code", "Received unknown code: $code")
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("JSON Parse Error", "Error parsing response: ${e.message}")
                        }
                    }
                } else {
                    Log.e("StatusError", "Backend returned error: ${response.code}")
                }
            }
        })
    }

    private suspend fun checkStatusPeriodically() {
        val client = createUnsafeOkHttpClient()
        val statusUrl = "https://192.168.1.89:5000/status/$globalToken"
        val request = Request.Builder().url(statusUrl).get().build()

        while (coroutineContext.isActive) {
            try {
                val response = client.newCall(request).execute() // Synchronous request
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        Log.d("StatusResponse", "Status Response: $it")
                        val jsonResponse = JSONObject(it)
                        val code = jsonResponse.getString("status")
                        when (code) {
                            "success" -> {
                                val transcription = jsonResponse.getString("transcription")
                                updateUI(transcription)
                                cancelJob()
                                return
                            }
                            "in_queue" -> {
                                val queuePosition = jsonResponse.getInt("queue_position")
                                updateUI("In Queue: $queuePosition")
                            }
                            "in_progress" -> {
                                val processingStatus = jsonResponse.getString("processing")
                                updateUI(processingStatus)
                            }
                            else -> {
                                Log.d("Unknown Code", "Received unknown code: $code")
                            }
                        }
                    }
                } else {
                    Log.e("StatusError", "Backend returned error: ${response.code}")
                    updateUI("Error: ${response.code}")
                    cancelJob()
                    return
                }
            } catch (e: Exception) {
                Log.e("StatusError", "Exception occurred: ${e.message}", e)
                updateUI("Error: ${e.message}")
                cancelJob()
                return
            }

            delay(10_000) // Wait for 10 seconds before the next check
        }
    }


    private fun updateUI(message: String) {
        // Update UI-related states on the main thread
        runOnUiThread {
            textFieldVisible = true
            textFieldValue = message
            buttonVisible = false
            filePickerVisible = true
        }
    }

    private fun cancelJob() {
        // Cancel the coroutine job if active
        statusJob?.cancel()
    }


    private fun uriToFile(uri: Uri): File? {
        val contentResolver = applicationContext.contentResolver
        val fileName = getFileName(uri)  // Get the original file name
        val fileExtension = fileName?.substringAfterLast('.', "") ?: "wav"  // Default to "wav" if no extension

        // Create a temp file with the original extension idk why
        val tempFile = File.createTempFile("audio_", ".$fileExtension", cacheDir)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                fileName = it.getString(columnIndex)
            }
        }
        return fileName
    }


    private fun sendAudioToBackend(file: File) {
        filePickerVisible = false
        textFieldVisible = true
        textFieldValue = "Adding to queue..."
        val client = createUnsafeOkHttpClient()

        val mimeType = when {
            file.name.endsWith(".wav", ignoreCase = true) -> "audio/wav"
            file.name.endsWith(".mp3", ignoreCase = true) -> "audio/mp3"
            else -> "application/octet-stream" // no idea wth is this
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(mimeType.toMediaTypeOrNull(), file))
            .build()

        Log.d("FileInfo", "Sending file: ${file.name} of size: ${file.length()} bytes")
        val request = Request.Builder()
            .url("https://192.168.1.89:5000/transcribe")
            .post(requestBody)
            .build()

        // Execute the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BackendError", "Error uploading file", e)
                filePickerVisible = true
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        Log.d("BackendResponse", "Backend Response: $it")
                        val jsonResponse = JsonParser.parseString(it) as JsonObject
                        globalToken = jsonResponse.get("status").asString
                        buttonVisible = true
                        textFieldVisible = false
                        textFieldValue = ""
                    }
                    // Start periodic status checks
                    handleButtonClick()
                } else {
                    Log.e("BackendError", "Backend returned error: ${response.code}")
                    filePickerVisible = true
                    textFieldVisible = true
                    textFieldValue = "There was an error: ${response.code}"
                }
            }
        })
    }


    // Step 4: Create the OkHttp client with the self-signed certificate
    private fun createUnsafeOkHttpClient(): OkHttpClient {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certInputStream = resources.openRawResource(R.raw.certificate)
        val certificate = certificateFactory.generateCertificate(certInputStream)

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
        keyStore.setCertificateEntry("ca", certificate)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore)
        }

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustManagerFactory.trustManagers, SecureRandom())
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Disables hostname verification for self-signed certs
            .build()
    }
}

@Composable
fun AudioItemLayout(openAudioPicker: () -> Unit) {
    AndroidView(factory = { context ->
        // Step 2: Inflate layout and set up the audio picker click
        LayoutInflater.from(context).inflate(R.layout.audio_item, null).apply {
            findViewById<TextView>(R.id.audio_picker).setOnClickListener {
                openAudioPicker()
            }
        }
    })
}
