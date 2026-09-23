package pro.sketchware.activities.resourceseditor.components.adapters;

import static com.besome.sketch.design.DesignActivity.sc_id;
import static com.besome.sketch.editor.LogicEditorActivity.getAllJavaFileNames;
import static com.besome.sketch.editor.LogicEditorActivity.getAllXmlFileNames;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.beans.BlockBean;
import com.besome.sketch.beans.ViewBean;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import a.a.a.eC;
import a.a.a.jC;
import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.activities.resourceseditor.ResourcesEditorActivity;
import pro.sketchware.databinding.PalletCustomviewBinding;
import pro.sketchware.databinding.ViewDimenEditorAddBinding;
import pro.sketchware.utility.SketchwareUtil;

public class DimensAdapter extends RecyclerView.Adapter<DimensAdapter.ViewHolder> {

    private final ArrayList<HashMap<String, Object>> originalData;
    private final ResourcesEditorActivity activity;
    private final HashMap<Integer, String> notesMap;
    private ArrayList<HashMap<String, Object>> filteredData;

    public DimensAdapter(ResourcesEditorActivity activity, ArrayList<HashMap<String, Object>> data, HashMap<Integer, String> notesMap) {
        originalData = new ArrayList<>(data);
        filteredData = data;
        this.activity = activity;
        this.notesMap = notesMap;
    }

    @NonNull
    @Override
    public DimensAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PalletCustomviewBinding itemBinding = PalletCustomviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DimensAdapter.ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DimensAdapter.ViewHolder holder, int position) {
        HashMap<String, Object> item = filteredData.get(position);
        String key = (String) item.get("key");
        String value = (String) item.get("value");
        holder.binding.title.setText(key);
        holder.binding.sub.setText(value);

        if (notesMap.containsKey(position)) {
            holder.binding.tvTitle.setText(notesMap.get(position));
            holder.binding.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvTitle.setVisibility(View.GONE);
        }

        holder.binding.backgroundCard.setOnClickListener(v -> {
            int adapterPosition = holder.getAbsoluteAdapterPosition();
            HashMap<String, Object> currentItem = filteredData.get(adapterPosition);

            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
            ViewDimenEditorAddBinding dialogBinding = ViewDimenEditorAddBinding.inflate(activity.getLayoutInflater());

            dialogBinding.dimenKeyInput.setText((String) currentItem.get("key"));
            dialogBinding.dimenValueInput.setText((String) currentItem.get("value"));
            dialogBinding.dimenHeaderInput.setText(notesMap.getOrDefault(adapterPosition, ""));

            dialog.setTitle("Edit dimension");
            dialog.setPositiveButton("Save", (d, which) -> {
                String keyInput = Objects.requireNonNull(dialogBinding.dimenKeyInput.getText()).toString().trim();
                String valueInput = Objects.requireNonNull(dialogBinding.dimenValueInput.getText()).toString().trim();
                if (keyInput.isEmpty() || valueInput.isEmpty()) {
                    SketchwareUtil.toast("Please fill in all fields", Toast.LENGTH_SHORT);
                    return;
                }
                currentItem.put("key", keyInput);
                currentItem.put("value", valueInput);
                String note = Objects.requireNonNull(dialogBinding.dimenHeaderInput.getText()).toString().trim();
                if (note.isEmpty()) {
                    notesMap.remove(adapterPosition);
                } else {
                    notesMap.put(adapterPosition, note);
                }
                notifyItemChanged(adapterPosition);
                activity.dimensEditor.hasUnsavedChanges = true;
            });

            dialog.setNeutralButton(Helper.getResString(R.string.common_word_delete), (d, which) -> {
                if (isXmlDimenUsed(key)) {
                    SketchwareUtil.toastError("This dimension is currently used in the project");
                } else {
                    filteredData.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    activity.dimensEditor.updateNoContentLayout();
                    activity.dimensEditor.hasUnsavedChanges = true;
                }
            });
            dialog.setNegativeButton(Helper.getResString(R.string.cancel), null);
            dialog.setView(dialogBinding.getRoot());
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            filteredData = new ArrayList<>(originalData);
        } else {
            filteredData = new ArrayList<>();
            for (HashMap<String, Object> item : originalData) {
                String key = (String) item.get("key");
                String value = (String) item.get("value");
                if ((key != null && key.toLowerCase().contains(query)) || (value != null && value.toLowerCase().contains(query))) {
                    filteredData.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean isXmlDimenUsed(String key) {
        if (sc_id == null) {
            return false;
        }

        String projectScId = sc_id;
        eC projectDataManager = jC.a(projectScId);

        return isDimenUsedInJavaFiles(projectScId, projectDataManager, key) || isDimenUsedInXmlFiles(projectScId, projectDataManager, key);
    }

    private boolean isDimenUsedInJavaFiles(String projectScId, eC projectDataManager, String key) {
        for (String javaFileName : getAllJavaFileNames(projectScId)) {
            for (Map.Entry<String, ArrayList<BlockBean>> entry : projectDataManager.b(javaFileName).entrySet()) {
                for (BlockBean block : entry.getValue()) {
                    if (("getResDim".equals(block.opCode) && key.equals(block.spec)) ||
                            ("getResDimen".equals(block.opCode) && block.parameters != null && block.parameters.contains("R.dimen." + key))) {
                        return true;
                    }
                    if (block.parameters != null && block.parameters.contains("R.dimen." + key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isDimenUsedInXmlFiles(String projectScId, eC projectDataManager, String key) {
        String targetRef = "@dimen/" + key;
        for (String xmlFileName : getAllXmlFileNames(projectScId)) {
            for (ViewBean view : projectDataManager.d(xmlFileName)) {
                if (view.text != null && (targetRef.equals(view.text.text) || targetRef.equals(view.text.hint))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PalletCustomviewBinding binding;

        public ViewHolder(PalletCustomviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
