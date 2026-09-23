package mod.hey.studios.build;

import java.io.Serializable;

import mod.hey.studios.project.ProjectSettings;
import pro.sketchware.utility.FileUtil;

public class BuildSettings extends ProjectSettings implements Serializable {

    public static final String SETTING_ANDROID_JAR_PATH = "android_jar";
    public static final String SETTING_CLASSPATH = "classpath";
    public static final String SETTING_DEXER = "dexer";
    public static final String SETTING_JAVA_VERSION = "java_ver";
    public static final String SETTING_NO_HTTP_LEGACY = "no_http_legacy";
    public static final String SETTING_NO_WARNINGS = "no_warn";
    public static final String SETTING_ENABLE_LOGCAT = "enable_logcat";
    public static final String SETTING_CMAKE_ABIS = "cmake_abis";
    public static final String SETTING_CMAKE_ABI_ARM64_V8A = "cmake_abi_arm64_v8a";
    public static final String SETTING_CMAKE_ABI_ARMEABI_V7A = "cmake_abi_armeabi_v7a";
    public static final String SETTING_CMAKE_ABI_X86 = "cmake_abi_x86";
    public static final String SETTING_CMAKE_ABI_X86_64 = "cmake_abi_x86_64";

    public static final String ABI_ARM64_V8A = "arm64-v8a";
    public static final String ABI_ARMEABI_V7A = "armeabi-v7a";
    public static final String ABI_X86 = "x86";
    public static final String ABI_X86_64 = "x86_64";

    public static final String SETTING_DEXER_D8 = "D8";
    public static final String SETTING_DEXER_DX = "Dx";
    public static final String SETTING_JAVA_VERSION_1_7 = "1.7";
    public static final String SETTING_JAVA_VERSION_1_8 = "1.8";
    public static final String SETTING_JAVA_VERSION_11 = "11";
    public static final String SETTING_JAVA_VERSION_15 = "15";
    public static final String SETTING_JAVA_VERSION_16 = "16";
    public static final String SETTING_JAVA_VERSION_17 = "17";
    public static final String SETTING_JAVA_VERSION_20 = "20";

    public BuildSettings(String sc_id) {
        super(sc_id);
    }

    @Override
    public String getPath() {
        return FileUtil.getExternalStorageDir() + "/.sketchware/data/" + sc_id + "/build_config";
    }
}
