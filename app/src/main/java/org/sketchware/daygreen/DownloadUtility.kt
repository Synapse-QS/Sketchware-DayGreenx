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
                val input = BufferedInputStream(connection.inputStream)
                
                destinationFile.parentFile?.mkdirs()
                val output = FileOutputStream(destinationFile)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    
                    handler.post {
                        if (activity.isFinishing) return@post
                        val progress = if (fileLength > 0) (total * 100 / fileLength).toInt() else 0
                        progressIndicator.progress = progress
                        tvPercentage.text = "$progress%"
                        tvBytes.text = if (fileLength > 0) "$total/$fileLength" else "$total bytes"
                    }
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                handler.post {
                    if (activity.isFinishing) return@post
                    dialog.dismiss()
                    val name = destinationFile.name.lowercase()
                    if (name.endsWith(".zip") || name.endsWith(".tar.gz") || name.endsWith(".tar.xz") || name.endsWith(".tgz")) {
                        extractArchive(activity, destinationFile) {
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
                    if (!activity.isFinishing) {
                        dialog.dismiss()
                        Toast.makeText(activity, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun extractArchive(activity: Activity, archiveFile: File, onComplete: () -> Unit) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_material_progress, null)
        val progressIndicator = dialogView.findViewById<LinearProgressIndicator>(R.id.progress_indicator)
        val tvPercentage = dialogView.findViewById<TextView>(R.id.tv_percentage)
        val tvBytes = dialogView.findViewById<TextView>(R.id.tv_bytes)

        progressIndicator.isIndeterminate = true
        tvPercentage.text = "Extracting..."
        tvBytes.text = archiveFile.name

        val progressDialog = MaterialAlertDialogBuilder(activity)
            .setTitle("Extracting...")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        progressDialog.show()

        executor.execute {
            try {
                val name = archiveFile.name.lowercase()
                val parentPath = archiveFile.parent ?: activity.filesDir.absolutePath
                
                if (name.endsWith(".zip")) {
                    val zipInputStream = ZipInputStream(archiveFile.inputStream())
                    FileUtil.extractZipTo(zipInputStream, parentPath)
                    zipInputStream.close()
                } else {
                    val tarFlag = if (name.endsWith(".tar.xz")) "-xJf" else "-xzf"
                    val process = ProcessBuilder("tar", tarFlag, archiveFile.absolutePath, "-C", parentPath)
                        .redirectErrorStream(true)
                        .start()
                    
                    val reader = process.inputStream.bufferedReader()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        // We could potentially parse lines if we used -v, but indeterminate is safer
                    }
                    
                    val exitCode = process.waitFor()
                    if (exitCode != 0) {
                        throw Exception("Tar process failed with exit code $exitCode")
                    }
                }

                // Post-extraction fix for NDK/CMake
                archiveFile.parentFile?.let { parent ->
                    if (name.contains("cmake")) {
                        fixFolderStructure(parent, "cmake")
                    } else if (name.contains("ndk")) {
                        fixFolderStructure(parent, "ndk")
                    }
                }
                
                handler.post {
                    if (!activity.isFinishing) {
                        progressDialog.dismiss()
                        onComplete()
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    if (!activity.isFinishing) {
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Extraction failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun fixFolderStructure(parentDir: File, targetName: String) {
        val targetDir = File(parentDir, targetName)
        val subDirs = parentDir.listFiles { f -> f.isDirectory && f.name != "cmake" && f.name != "ndk" }
        if (subDirs != null && subDirs.size == 1) {
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            subDirs[0].renameTo(targetDir)
        }
    }

    fun getDeviceAbi(): String {
        return Build.SUPPORTED_ABIS[0]
    }
}
