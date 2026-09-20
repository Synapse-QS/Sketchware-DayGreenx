package org.sketchware.daygreen

import android.content.Context
import android.text.format.Formatter
import java.io.File
import mod.jbk.build.BuiltInLibraries

object FileCheckUtils {
    @JvmStatic
    fun isAaptDownloaded(context: Context): Boolean {
        // Based on ProjectBuilder, it uses filesDir/bin/aapt2
        val aapt2Binary = File(context.filesDir, "bin/aapt2")
        return aapt2Binary.exists()
    }

    @JvmStatic
    fun isSdkDownloaded(context: Context): Boolean {
        // Check for the old android.jar or any new android-XX.jar
        if (File(BuiltInLibraries.EXTRACTED_COMPILE_ASSETS_PATH, "android.jar").exists()) return true
        
        val libsDir = BuiltInLibraries.EXTRACTED_COMPILE_ASSETS_PATH
        return libsDir.exists() && libsDir.listFiles { _, name -> 
            name.startsWith("android-") && name.endsWith(".jar") 
        }?.isNotEmpty() == true
    }

    @JvmStatic
    fun isNdkDownloaded(context: Context): Boolean {
        val ndkDir = File(context.filesDir, "native/ndk")
        if (!ndkDir.exists()) return false
        
        // Preferred check (what NativeCompiler needs)
        val toolchainFile = File(ndkDir, "build/cmake/android.toolchain.cmake")
        if (toolchainFile.exists()) return true
        
        // Fallback check (standard NDK marker)
        return File(ndkDir, "source.properties").exists()
    }

    @JvmStatic
    fun isCmakeDownloaded(context: Context): Boolean {
        val cmakeDir = File(context.filesDir, "native/cmake")
        if (!cmakeDir.exists()) return false
        
        // Preferred check
        val cmakeBinary = File(cmakeDir, "bin/cmake")
        return cmakeBinary.exists()
    }

    fun getToolStatus(isInstalled: Boolean): String {
        return if (isInstalled) "Installed" else "Not Installed"
    }

    private fun getFolderSize(file: File): Long {
        if (!file.exists()) return 0
        if (file.isFile) return file.length()
        var size: Long = 0
        val files = file.listFiles()
        if (files != null) {
            for (f in files) {
                size += getFolderSize(f)
            }
        }
        return size
    }

    private fun formatFileSize(context: Context, sizeInBytes: Long): String {
        return Formatter.formatShortFileSize(context, sizeInBytes)
    }

    @JvmStatic
    fun getNdkSize(context: Context): String {
        val ndkDir = File(context.filesDir, "native/ndk")
        if (ndkDir.exists()) {
            return formatFileSize(context, getFolderSize(ndkDir))
        }
        return "~360 MB"
    }

    @JvmStatic
    fun getCmakeSize(context: Context): String {
        val cmakeDir = File(context.filesDir, "native/cmake")
        if (cmakeDir.exists()) {
            return formatFileSize(context, getFolderSize(cmakeDir))
        }
        return "~48 MB"
    }

    @JvmStatic
    fun getAaptSize(context: Context): String {
        val aaptFile = File(context.filesDir, "bin/aapt2")
        if (aaptFile.exists()) {
            return formatFileSize(context, aaptFile.length())
        }
        return "~5 MB"
    }

    @JvmStatic
    fun getSdkVersionSize(context: Context, version: String): String {
        if (version.isNotEmpty()) {
            val sdkFile = File(BuiltInLibraries.EXTRACTED_COMPILE_ASSETS_PATH, "android-$version.jar")
            if (sdkFile.exists()) {
                return formatFileSize(context, sdkFile.length())
            }
        }
        return "~30 MB"
    }

    @JvmStatic
    fun isSdkVersionDownloaded(version: String): Boolean {
        if (version.isEmpty()) return true
        val sdkFile = File(BuiltInLibraries.EXTRACTED_COMPILE_ASSETS_PATH, "android-$version.jar")
        return sdkFile.exists()
    }

    @JvmStatic
    fun getInstalledSdks(context: Context): List<Int> {
        val libsDir = File(context.filesDir, "libs")
        if (!libsDir.exists()) return emptyList()
        
        return libsDir.listFiles { _, name -> name.startsWith("android-") && name.endsWith(".jar") }
            ?.mapNotNull { file ->
                file.name.removePrefix("android-").removeSuffix(".jar").toIntOrNull()
            }?.sorted() ?: emptyList()
    }
}
