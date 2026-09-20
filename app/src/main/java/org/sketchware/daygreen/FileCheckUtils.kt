package org.sketchware.daygreen

import android.content.Context
import java.io.File
import mod.jbk.build.BuiltInLibraries

object FileCheckUtils {
    fun isAaptDownloaded(context: Context): Boolean {
        // Based on ProjectBuilder, it uses filesDir/bin/aapt2
        val aapt2Binary = File(context.filesDir, "bin/aapt2")
        return aapt2Binary.exists()
    }

    fun isSdkDownloaded(context: Context): Boolean {
        // Check for the old android.jar or any new android-XX.jar
        if (File(BuiltInLibraries.EXTRACTED_COMPILE_ASSETS_PATH, "android.jar").exists()) return true
        
        val libsDir = BuiltInLibraries.EXTRACTED_COMPILE_ASSETS_PATH
        return libsDir.exists() && libsDir.listFiles { _, name -> 
            name.startsWith("android-") && name.endsWith(".jar") 
        }?.isNotEmpty() == true
    }

    fun isNdkDownloaded(context: Context): Boolean {
        // Based on NativeCompiler, it uses filesDir/native/ndk/build/cmake/android.toolchain.cmake
        val toolchainFile = File(context.filesDir, "native/ndk/build/cmake/android.toolchain.cmake")
        return toolchainFile.exists()
    }

    fun isCmakeDownloaded(context: Context): Boolean {
        // Based on NativeCompiler, it uses filesDir/native/cmake/bin/cmake
        val cmakeBinary = File(context.filesDir, "native/cmake/bin/cmake")
        return cmakeBinary.exists()
    }

    fun getToolStatus(isInstalled: Boolean): String {
        return if (isInstalled) "Installed" else "Not Installed"
    }

    fun getNdkSize(): String = "~360 MB"
    fun getCmakeSize(): String = "~48 MB"
    fun getAaptSize(): String = "~5 MB"
    fun getSdkVersionSize(): String = "~30 MB"

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
