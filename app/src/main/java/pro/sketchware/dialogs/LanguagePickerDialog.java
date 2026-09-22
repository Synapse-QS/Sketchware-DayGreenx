package pro.sketchware.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pro.sketchware.R;
import pro.sketchware.utility.ThemeUtils;

public class LanguagePickerDialog {

    public static final String PREF_LANGUAGE = "app_language";
    private static final String PREF_NAME = "language_pref";

    private static final List<String[]> LANGUAGES = Arrays.asList(
            new String[]{"system", "Follow system"},
            new String[]{"en", "English"},
            new String[]{"id", "Bahasa Indonesia"},
            new String[]{"zh", "中文 (Chinese)"},
            new String[]{"ar", "العربية (Arabic)"},
            new String[]{"tr", "Türkçe (Turkish)"}
    );

    public static void show(Activity activity) {
        String currentLang = getSavedLanguage(activity);

        int[] selectedIndex = {0};
        for (int i = 0; i < LANGUAGES.size(); i++) {
            if (LANGUAGES.get(i)[0].equals(currentLang)) {
                selectedIndex[0] = i;
                break;
            }
        }

        RecyclerView recyclerView = new RecyclerView(activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        LanguageAdapter adapter = new LanguageAdapter(activity, LANGUAGES, selectedIndex[0]);
        recyclerView.setAdapter(adapter);

        int padding = (int) (16 * activity.getResources().getDisplayMetrics().density);
        recyclerView.setPadding(0, padding, 0, 0);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Choose Language")
                .setView(recyclerView)
                .setNegativeButton(R.string.common_word_cancel, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    String picked = LANGUAGES.get(adapter.getSelected())[0];
                    saveLanguage(activity, picked);
                    applyLanguage(picked);
                    activity.recreate();
                })
                .show();
    }

    public static void applyLanguage(String langCode) {
        if (langCode.equals("system")) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            LocaleListCompat localeList = LocaleListCompat.forLanguageTags(langCode);
            AppCompatDelegate.setApplicationLocales(localeList);
        }
    }

    public static void applyOnStartup(Context context) {
        String saved = getSavedLanguage(context);
        applyLanguage(saved);
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(PREF_LANGUAGE, "system");
    }

    private static void saveLanguage(Context context, String langCode) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(PREF_LANGUAGE, langCode).apply();
    }

    private static class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.VH> {

        private final Activity activity;
        private final List<String[]> items;
        private int selected;

        LanguageAdapter(Activity activity, List<String[]> items, int selected) {
            this.activity = activity;
            this.items = items;
            this.selected = selected;
        }

        int getSelected() {
            return selected;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Layout sederhana: TextView dengan padding
            TextView tv = new TextView(activity);
            int paddingH = (int) (24 * activity.getResources().getDisplayMetrics().density);
            int paddingV = (int) (16 * activity.getResources().getDisplayMetrics().density);
            tv.setPadding(paddingH, paddingV, paddingH, paddingV);
            tv.setTextSize(16);
            tv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String label = items.get(position)[1];
            holder.tv.setText(label);

            if (position == selected) {
                holder.tv.setTextColor(ThemeUtils.getColor(activity, R.attr.colorPrimary));
                holder.tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.tv.setTextColor(ThemeUtils.getColor(activity, R.attr.colorOnSurface));
                holder.tv.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            holder.tv.setOnClickListener(v -> {
                int prev = selected;
                selected = holder.getAdapterPosition();
                notifyItemChanged(prev);
                notifyItemChanged(selected);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(TextView tv) {
                super(tv);
                this.tv = tv;
            }
        }
    }
}