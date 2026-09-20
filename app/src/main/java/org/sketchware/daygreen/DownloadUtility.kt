package org.sketchware.daygreen

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import pro.sketchware.R
import pro.sketchware.utility.FileUtil
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.zip.ZipInputStream

object DownloadUtility {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    fun downloadFile(
        activity: Activity,
        url: String,
        destinationFile: File,
        onComplete: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_material_progress, null)
        val progressIndicator = dialogView.findViewById<LinearProgressIndicator>(R.id.progress_indicator)
        val tvPercentage = dialogView.findViewById<TextView>(R.id.tv_percentage)
        val tvBytes = dialogView.findViewById<TextView>(R.id.tv_bytes)

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle("Downloading...")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val urlObj = URL(url)
                connection = urlObj.openConnection() as HttpURLConnection
                connection.connect()

                val fileLength = connection.contentLength
                val input = BufferedInputStream(urlObj.openStream())
                
                destinationFile.parentFile?.mkdirs()
                val output = FileOutputStream(destinationFile)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    
                    handler.post {
                        val progress = (total * 100 / fileLength).toInt()
                        progressIndicator.progress = progress
                        tvPercentage.text = "$progress%"
                        tvBytes.text = "$total/$fileLength"
                    }
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                handler.post {
                    dialog.dismiss()
                    if (destinationFile.name.endsWith(".zip")) {
                        extractZip(activity, destinationFile) {
                            Toast.makeText(activity, "Download and extraction completed", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                    } else {
                        Toast.makeText(activity, "Download completed", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    dialog.dismiss()
                    Toast.makeText(activity, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun extractZip(activity: Activity, zipFile: File, onComplete: () -> Unit) {
        val progressDialog = MaterialAlertDialogBuilder(activity)
            .setTitle("Extracting...")
            .setMessage("Please wait while extracting files.")
            .setCancelable(false)
            .show()

        executor.execute {
            try {
                val zipInputStream = ZipInputStream(zipFile.inputStream())
                FileUtil.extractZipTo(zipInputStream, zipFile.parent)
                zipInputStream.close()
                // Optionally delete the zip file after extraction
                // zipFile.delete()
                
                handler.post {
                    progressDialog.dismiss()
                    onComplete()
                }
            } catch (e: Exception) {
                handler.post {
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Extraction failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun getDeviceAbi(): String {
        return Build.SUPPORTED_ABIS[0]
    }
}
