package pro.sketchware.activities.resourceseditor.components.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.activities.resourceseditor.ResourcesEditorActivity;
import pro.sketchware.activities.resourceseditor.components.adapters.DimensAdapter;
import pro.sketchware.activities.resourceseditor.components.utils.DimensEditorManager;
import pro.sketchware.databinding.ResourcesEditorFragmentBinding;
import pro.sketchware.databinding.ViewDimenEditorAddBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.XmlUtil;

public class DimensEditor extends Fragment {
    private ResourcesEditorFragmentBinding binding;

    private ResourcesEditorActivity activity;

    public DimensAdapter adapter;

    public final ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
    private HashMap<Integer, String> notesMap = new HashMap<>();

    public boolean hasUnsavedChanges;
    public String filePath;

    public final DimensEditorManager dimensEditorManager = new DimensEditorManager();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (ResourcesEditorActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ResourcesEditorFragmentBinding.inflate(inflater, container, false);
        dimensEditorManager.sc_id = activity.sc_id;
        return binding.getRoot();
    }

    public void updateDimensList(String filePath, int updateMode, boolean hasUnsavedChangesStatus) {
        this.filePath = filePath;
        hasUnsavedChanges = hasUnsavedChangesStatus;

        boolean isSkippingMode = updateMode == 1;
        boolean isMergeAndReplace = updateMode == 2;
        dimensEditorManager.isDefaultVariant = activity.variant.isEmpty();

        ArrayList<HashMap<String, Object>> defaultDimens = new ArrayList<>();
        if (FileUtil.isExistFile(filePath)) {
            dimensEditorManager.convertXmlDimensToListMap(FileUtil.readFileIfExist(filePath), defaultDimens);
        }
        notesMap = new HashMap<>(dimensEditorManager.notesMap);

        if (isSkippingMode) {
            HashSet<String> existingKeys = new HashSet<>();
            for (HashMap<String, Object> existingMap : listmap) {
                existingKeys.add((String) existingMap.get("key"));
            }

            for (HashMap<String, Object> dimenMap : defaultDimens) {
                String key = (String) dimenMap.get("key");
                if (existingKeys.add(key)) {
                    listmap.add(dimenMap);
                }
            }
        } else {
            if (isMergeAndReplace) {
                HashSet<String> newKeys = new HashSet<>();
                for (HashMap<String, Object> dimenMap : defaultDimens) {
                    newKeys.add((String) dimenMap.get("key"));
                }

                listmap.removeIf(existingMap -> newKeys.contains((String) existingMap.get("key")));
            } else {
                listmap.clear();
            }
            listmap.addAll(defaultDimens);
        }

        activity.runOnUiThread(() -> {
            adapter = new DimensAdapter(activity, listmap, notesMap);
            binding.recyclerView.setAdapter(adapter);
            activity.checkForInvalidResources();
            updateNoContentLayout();
            if (hasUnsavedChanges) {
                this.filePath = activity.dimensFilePath;
            }
        });
    }

    public void updateNoContentLayout() {
        if (listmap.isEmpty()) {
            binding.noContentLayout.setVisibility(View.VISIBLE);
            binding.noContentTitle.setText(String.format(Helper.getResString(R.string.resource_manager_no_list_title), "Dimensions"));
            binding.noContentBody.setText(String.format(Helper.getResString(R.string.resource_manager_no_list_body), "dimension"));
        } else {
            binding.noContentLayout.setVisibility(View.GONE);
        }
    }

    public void showAddDimenDialog() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity());
        ViewDimenEditorAddBinding dialogBinding = ViewDimenEditorAddBinding.inflate(getLayoutInflater());
        dialog.setTitle("Create new dimension");

        dialog.setPositiveButton("Create", (d, which) -> {
            String key = Objects.requireNonNull(dialogBinding.dimenKeyInput.getText()).toString().trim();
            String value = Objects.requireNonNull(dialogBinding.dimenValueInput.getText()).toString().trim();

            if (key.isEmpty() || value.isEmpty()) {
                SketchwareUtil.toastError("Please fill in all fields");
                return;
            }

            if (dimensEditorManager.isXmlDimensExist(listmap, key)) {
                SketchwareUtil.toastError("\"" + key + "\" already exists");
                return;
            }
            addDimen(key, value, Objects.requireNonNull(dialogBinding.dimenHeaderInput.getText()).toString().trim());
            updateNoContentLayout();
        });
        dialog.setNegativeButton(getString(R.string.cancel), null);
        dialog.setView(dialogBinding.getRoot());
        dialog.show();
    }

    public void addDimen(final String key, final String value, String note) {
        hasUnsavedChanges = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("value", value);
        if (listmap.isEmpty()) {
            listmap.add(map);
            adapter.notifyItemInserted(0);
            if (!note.isEmpty()) {
                notesMap.put(0, note);
            }
            return;
        }
        listmap.add(map);
        int notifyPosition = listmap.size() - 1;
        if (!note.isEmpty()) {
            notesMap.put(notifyPosition, note);
        }
        adapter.notifyItemInserted(notifyPosition);
    }

    public void saveDimensFile() {
        if (hasUnsavedChanges) {
            XmlUtil.saveXml(filePath, dimensEditorManager.convertListMapToXmlDimens(listmap, notesMap));
            hasUnsavedChanges = false;
        }
    }
}
