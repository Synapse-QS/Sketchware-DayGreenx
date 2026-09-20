package org.sketchware.daygreen;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import a.a.a.DB;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.GsonUtils;

public class CustomColorManager {
    private static final String COLOR_FILE_PATH = FileUtil.getExternalStorageDir() + "/.sketchware/resources/color/color.json";

    private static Map<String, String> loadData() {
        if (FileUtil.isExistFile(COLOR_FILE_PATH)) {
            try {
                String content = FileUtil.readFile(COLOR_FILE_PATH);
                return GsonUtils.getGson().fromJson(content, new TypeToken<Map<String, String>>() {}.getType());
            } catch (Exception e) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private static void saveData(Map<String, String> data) {
        File file = new File(COLOR_FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileUtil.writeFile(COLOR_FILE_PATH, GsonUtils.getGson().toJson(data));
    }

    public static String getCustomColors() {
        String val = loadData().get("custom_colors");
        return val != null ? val : "";
    }

    public static void saveCustomColors(String colors) {
        Map<String, String> data = loadData();
        data.put("custom_colors", colors);
        saveData(data);
    }

    public static String getCustomAttrs() {
        String val = loadData().get("custom_attrs");
        return val != null ? val : "";
    }

    public static void saveCustomAttrs(String attrs) {
        Map<String, String> data = loadData();
        data.put("custom_attrs", attrs);
        saveData(data);
    }

    public static void clearAll() {
        FileUtil.deleteFile(COLOR_FILE_PATH);
    }

    public static void migrateIfNecessary(DB colorPref) {
        if (!FileUtil.isExistFile(COLOR_FILE_PATH)) {
            String colors = colorPref.f("P24I1");
            String attrs = colorPref.f("P24I2");
            if (!colors.isEmpty() || !attrs.isEmpty()) {
                saveCustomColors(colors);
                saveCustomAttrs(attrs);
            }
        }
    }
}
