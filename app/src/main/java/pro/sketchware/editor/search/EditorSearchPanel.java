package pro.sketchware.editor.search;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import io.github.rosemoe.sora.event.PublishSearchResultEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorSearcher;
import pro.sketchware.R;
import pro.sketchware.databinding.LayoutCodeEditorSearchBinding;
import pro.sketchware.utility.SketchwareUtil;

public class EditorSearchPanel extends FrameLayout {

    private LayoutCodeEditorSearchBinding binding;
    private CodeEditor editor;

    private boolean matchCase = false;
    private boolean matchWholeWord = false;
    private boolean useRegex = false;
    private boolean isReplaceVisible = false;

    public EditorSearchPanel(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EditorSearchPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditorSearchPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = LayoutCodeEditorSearchBinding.inflate(LayoutInflater.from(context), this, true);

        binding.btnToggleReplace.setOnClickListener(v -> toggleReplace());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                gotoNext();
                return true;
            }
            return false;
        });

        binding.etReplace.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                replaceCurrent();
                return true;
            }
            return false;
        });

        binding.btnMatchCase.setOnClickListener(v -> {
            matchCase = !matchCase;
            updateOptionButtons();
            performSearch();
        });

        binding.btnMatchWord.setOnClickListener(v -> {
            matchWholeWord = !matchWholeWord;
            updateOptionButtons();
            performSearch();
        });

        binding.btnRegex.setOnClickListener(v -> {
            useRegex = !useRegex;
            updateOptionButtons();
            performSearch();
        });

        binding.btnPrevMatch.setOnClickListener(v -> gotoPrevious());
        binding.btnNextMatch.setOnClickListener(v -> gotoNext());
        binding.btnCloseSearch.setOnClickListener(v -> hide());

        binding.btnReplace.setOnClickListener(v -> replaceCurrent());
        binding.btnReplaceAll.setOnClickListener(v -> replaceAll());

        updateOptionButtons();
    }

    public void attachToEditor(@NonNull CodeEditor editor) {
        this.editor = editor;

        editor.subscribeEvent(PublishSearchResultEvent.class, (event, unsubscribe) -> {
            post(this::updateMatchCount);
        });

        editor.subscribeEvent(SelectionChangeEvent.class, (event, unsubscribe) -> {
            if (isOpen()) {
                post(this::updateMatchCount);
            }
        });
    }

    private void updateOptionButtons() {
        int primaryColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.BLUE);
        int defaultColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY);
        int activeContainerColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY);

        setButtonActiveState(binding.btnMatchCase, matchCase, primaryColor, defaultColor, activeContainerColor);
        setButtonActiveState(binding.btnMatchWord, matchWholeWord, primaryColor, defaultColor, activeContainerColor);
        setButtonActiveState(binding.btnRegex, useRegex, primaryColor, defaultColor, activeContainerColor);
    }

    private void setButtonActiveState(MaterialButton btn, boolean active, int primaryColor, int defaultColor, int activeBg) {
        btn.setIconTint(ColorStateList.valueOf(active ? primaryColor : defaultColor));
        btn.setBackgroundTintList(ColorStateList.valueOf(active ? activeBg : Color.TRANSPARENT));
    }

    private void toggleReplace() {
        setReplaceVisible(!isReplaceVisible);
    }

    public void setReplaceVisible(boolean visible) {
        isReplaceVisible = visible;
        binding.layoutReplace.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.btnToggleReplace.animate().rotation(visible ? 180f : 0f).setDuration(200).start();
        if (visible) {
            binding.etReplace.requestFocus();
        }
    }

    public void performSearch() {
        if (editor == null) return;
        String query = binding.etSearch.getText() != null ? binding.etSearch.getText().toString() : "";
        if (query.isEmpty()) {
            try {
                editor.getSearcher().stopSearch();
            } catch (Exception ignored) {}
            binding.tvMatchCount.setText("");
            return;
        }

        try {
            int searchType = useRegex ? EditorSearcher.SearchOptions.TYPE_REGULAR_EXPRESSION :
                    (matchWholeWord ? EditorSearcher.SearchOptions.TYPE_WHOLE_WORD : EditorSearcher.SearchOptions.TYPE_NORMAL);
            EditorSearcher.SearchOptions options = new EditorSearcher.SearchOptions(searchType, !matchCase);
            editor.getSearcher().search(query, options);
        } catch (Exception e) {
            binding.tvMatchCount.setText("Invalid regex");
        }
    }

    private void updateMatchCount() {
        if (editor == null) return;
        String query = binding.etSearch.getText() != null ? binding.etSearch.getText().toString() : "";
        if (query.isEmpty() || !editor.getSearcher().hasQuery()) {
            binding.tvMatchCount.setText("");
            return;
        }

        int total = editor.getSearcher().getMatchedPositionCount();
        if (total == 0) {
            binding.tvMatchCount.setText("0/0");
        } else {
            int current = editor.getSearcher().getCurrentMatchedPositionIndex();
            if (current >= 0) {
                binding.tvMatchCount.setText((current + 1) + "/" + total);
            } else {
                binding.tvMatchCount.setText(total + " matches");
            }
        }
    }

    public void gotoNext() {
        if (editor == null || !editor.getSearcher().hasQuery()) return;
        try {
            editor.getSearcher().gotoNext();
            updateMatchCount();
        } catch (Exception ignored) {}
    }

    public void gotoPrevious() {
        if (editor == null || !editor.getSearcher().hasQuery()) return;
        try {
            editor.getSearcher().gotoPrevious();
            updateMatchCount();
        } catch (Exception ignored) {}
    }

    public void replaceCurrent() {
        if (editor == null || !editor.getSearcher().hasQuery()) return;
        String replacement = binding.etReplace.getText() != null ? binding.etReplace.getText().toString() : "";
        try {
            if (editor.getSearcher().isMatchedPositionSelected()) {
                editor.getSearcher().replaceCurrentMatch(replacement);
                editor.getSearcher().gotoNext();
            } else {
                editor.getSearcher().gotoNext();
                if (editor.getSearcher().isMatchedPositionSelected()) {
                    editor.getSearcher().replaceCurrentMatch(replacement);
                    editor.getSearcher().gotoNext();
                }
            }
            updateMatchCount();
        } catch (Exception e) {
            SketchwareUtil.toastError("Replace failed: " + e.getMessage());
        }
    }

    public void replaceAll() {
        if (editor == null || !editor.getSearcher().hasQuery()) return;
        String replacement = binding.etReplace.getText() != null ? binding.etReplace.getText().toString() : "";
        try {
            int count = editor.getSearcher().getMatchedPositionCount();
            if (count == 0) {
                SketchwareUtil.toast("No matches to replace");
                return;
            }
            editor.getSearcher().replaceAll(replacement, () -> post(() -> {
                SketchwareUtil.toast("Replaced " + count + " occurrences");
                updateMatchCount();
            }));
        } catch (Exception e) {
            SketchwareUtil.toastError("Replace all failed: " + e.getMessage());
        }
    }

    public void show(boolean withReplace) {
        setVisibility(View.VISIBLE);
        setReplaceVisible(withReplace || isReplaceVisible);

        if (editor != null && editor.getCursor().isSelected()) {
            int left = editor.getCursor().getLeft();
            int right = editor.getCursor().getRight();
            String selected = editor.getText().substring(left, right);
            if (selected != null && !selected.isEmpty() && !selected.contains("\n")) {
                binding.etSearch.setText(selected);
                binding.etSearch.selectAll();
            }
        }

        binding.etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
        }

        if (binding.etSearch.getText() != null && !binding.etSearch.getText().toString().isEmpty()) {
            performSearch();
        }
    }

    public void hide() {
        setVisibility(View.GONE);
        if (editor != null && editor.getSearcher() != null) {
            try {
                editor.getSearcher().stopSearch();
            } catch (Exception ignored) {}
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
        if (editor != null) {
            editor.requestFocus();
        }
    }

    public boolean isOpen() {
        return getVisibility() == View.VISIBLE;
    }

    public boolean handleBackPressed() {
        if (isOpen()) {
            hide();
            return true;
        }
        return false;
    }
}
