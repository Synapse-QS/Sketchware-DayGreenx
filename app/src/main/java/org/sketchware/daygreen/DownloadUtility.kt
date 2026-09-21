package org.sketchware.daygreen

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.tukaani.xz.XZInputStream
import pro.sketchware.R
import pro.sketchware.utility.FileUtil
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FilterInputStream
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
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }
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

        progressIndicator.isIndeterminate = false
        progressIndicator.progress = 0
        tvPercentage.text = "0%"
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
                val parentPath = archiveFile.parentFile ?: activity.filesDir
                val tempExtractDir = File(parentPath, "temp_extract_" + System.currentTimeMillis())
                tempExtractDir.mkdirs()

                if (name.endsWith(".zip")) {
                    val zipInputStream = ZipInputStream(archiveFile.inputStream())
                    FileUtil.extractZipTo(zipInputStream, tempExtractDir.absolutePath)
                    zipInputStream.close()
                } else if (name.endsWith(".tar.xz")) {
                    val totalSize = archiveFile.length()
                    var bytesRead = 0L

                    val countingIn = object : FilterInputStream(archiveFile.inputStream().buffered()) {
                        override fun read(b: ByteArray, off: Int, len: Int): Int {
                            val n = super.read(b, off, len)
                            if (n > 0) {
                                bytesRead += n
                                handler.post {
                                    if (!activity.isFinishing) {
                                        val progress = if (totalSize > 0) (bytesRead * 100 / totalSize).toInt().coerceAtMost(100) else 0
                                        progressIndicator.progress = progress
                                        tvPercentage.text = "$progress%"
                                        tvBytes.text = "$bytesRead/$totalSize"
                                    }
                                }
                            }
                            return n
                        }
                    }

                    XZInputStream(countingIn).use { xzIn ->
                        TarArchiveInputStream(xzIn).use { tarIn ->
                            var entry = tarIn.nextTarEntry
                            while (entry != null) {
                                val outFile = File(tempExtractDir, entry.name)
                                if (entry.isDirectory) {
                                    outFile.mkdirs()
                                } else {
                                    outFile.parentFile?.mkdirs()
                                    outFile.outputStream().use { out -> tarIn.copyTo(out) }
                                    if (entry.mode and 0b001001001 != 0) outFile.setExecutable(true)
                                }
                                entry = tarIn.nextTarEntry
                            }
                        }
                    }
                } else {
                    val tarFlag = "-xzf"
                    val process = ProcessBuilder("tar", tarFlag, archiveFile.absolutePath, "-C", tempExtractDir.absolutePath)
                        .redirectErrorStream(true)
                        .start()

                    val reader = process.inputStream.bufferedReader()
                    while (reader.readLine() != null) {
                        // Wait for output to finish
                    }

                    val exitCode = process.waitFor()
                    if (exitCode != 0) {
                        throw Exception("Tar process failed with exit code $exitCode")
                    }
                }

                // Decide target name and move
                val targetName = if (name.contains("cmake")) "cmake" else if (name.contains("ndk")) "ndk" else null
                if (targetName != null) {
                    val targetDir = File(parentPath, targetName)
                    if (targetDir.exists()) targetDir.deleteRecursively()

                    val subDirs = tempExtractDir.listFiles { f -> f.isDirectory }
                    val subFiles = tempExtractDir.listFiles { f -> f.isFile }

                    if (subDirs != null && subDirs.size == 1 && (subFiles == null || subFiles.isEmpty())) {
                        subDirs[0].renameTo(targetDir)
                    } else {
                        tempExtractDir.renameTo(targetDir)
                    }
                }

                if (tempExtractDir.exists()) tempExtractDir.deleteRecursively()
                archiveFile.delete()

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

    fun getDeviceAbi(): String {
        return Build.SUPPORTED_ABIS[0]
    }
}