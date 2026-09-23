package com.besome.sketch.editor.property;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import a.a.a.Kw;
import a.a.a.mB;
import a.a.a.wB;
import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.activities.resourceseditor.components.utils.DimensEditorManager;
import pro.sketchware.databinding.PropertyPopupInputIndentBinding;
import pro.sketchware.lib.validator.MinMaxInputValidator;

@SuppressLint("ViewConstructor")
public class PropertyIndentItem extends RelativeLayout implements View.OnClickListener {

    /**
     * Left margin in dp
     */
    public int j;
    public int k;
    public int l;
    public int m;
    public String resLeft = null;
    public String resTop = null;
    public String resRight = null;
    public String resBottom = null;
    private Context context;
    private String sc_id;
    private String key = "";
    private View propertyItem;
    private View propertyMenuItem;
    private ImageView imgLeftIcon;
    private int icon;
    private TextView tvName;
    private TextView tvValue;
    private Kw valueChangeListener;

    public PropertyIndentItem(Context context, boolean z) {
        super(context);
        initialize(context, z);
    }

    public void setScId(String scId) {
        this.sc_id = scId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        int identifier = getResources().getIdentifier(key, "string", getContext().getPackageName());
        if (identifier > 0) {
            tvName.setText(Helper.getResString(identifier));
            switch (this.key) {
                case "property_padding":
                    icon = R.drawable.ic_mtrl_padding;
                    break;

                case "property_margin":
                    icon = R.drawable.ic_mtrl_margin;
                    break;
            }
            if (propertyMenuItem.getVisibility() == VISIBLE) {
                ((ImageView) findViewById(R.id.img_icon)).setImageResource(icon);
                ((TextView) findViewById(R.id.tv_title)).setText(Helper.getResString(identifier));
                return;
            }
            imgLeftIcon.setImageResource(icon);
        }
    }

    public String getValue() {
        return "";
    }

    @Override
    public void onClick(View v) {
        if (!mB.a()) {
            switch (key) {
                case "property_padding":
                case "property_margin":
                    showDialog();
                    break;
            }
        }
    }

    public void setOnPropertyValueChangeListener(Kw onPropertyValueChangeListener) {
        valueChangeListener = onPropertyValueChangeListener;
    }

    public void setOrientationItem(int orientationItem) {
        if (orientationItem == 0) {
            propertyItem.setVisibility(GONE);
            propertyMenuItem.setVisibility(VISIBLE);
            propertyItem.setOnClickListener(null);
            propertyMenuItem.setOnClickListener(this);
        } else {
            propertyItem.setVisibility(VISIBLE);
            propertyMenuItem.setVisibility(GONE);
            propertyItem.setOnClickListener(this);
            propertyMenuItem.setOnClickListener(null);
        }
    }

    private void initialize(Context context, boolean z) {
        this.context = context;
        wB.a(context, this, R.layout.property_input_item);
        tvName = findViewById(R.id.tv_name);
        tvValue = findViewById(R.id.tv_value);
        imgLeftIcon = findViewById(R.id.img_left_icon);
        propertyItem = findViewById(R.id.property_item);
        propertyMenuItem = findViewById(R.id.property_menu_item);
    }

    public void a(int left, int top, int right, int bottom) {
        a(left, top, right, bottom, null, null, null, null);
    }

    public void a(int left, int top, int right, int bottom, String resLeft, String resTop, String resRight, String resBottom) {
        j = left;
        k = top;
        l = right;
        m = bottom;
        this.resLeft = resLeft;
        this.resTop = resTop;
        this.resRight = resRight;
        this.resBottom = resBottom;

        String lStr = resLeft != null && !resLeft.isEmpty() ? resLeft : String.valueOf(j);
        String tStr = resTop != null && !resTop.isEmpty() ? resTop : String.valueOf(k);
        String rStr = resRight != null && !resRight.isEmpty() ? resRight : String.valueOf(l);
        String bStr = resBottom != null && !resBottom.isEmpty() ? resBottom : String.valueOf(m);

        if (lStr.equals(tStr) && tStr.equals(rStr) && rStr.equals(bStr)) {
            tvValue.setText(lStr);
        } else {
            tvValue.setText("left: " + lStr + ", top: " + tStr + ", right: " + rStr + ", bottom: " + bStr);
        }
    }

    private void showDialog() {
        String propertyType = Helper.getText(tvName);

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setTitle(propertyType);
        dialog.setIcon(icon);

        PropertyPopupInputIndentBinding binding = PropertyPopupInputIndentBinding.inflate(LayoutInflater.from(getContext()));
        View view = binding.getRoot();

        DimensEditorManager.setupDimenAutoComplete(getContext(), sc_id, binding.etAll);
        DimensEditorManager.setupDimenAutoComplete(getContext(), sc_id, binding.etLeft);
        DimensEditorManager.setupDimenAutoComplete(getContext(), sc_id, binding.etRight);
        DimensEditorManager.setupDimenAutoComplete(getContext(), sc_id, binding.etTop);
        DimensEditorManager.setupDimenAutoComplete(getContext(), sc_id, binding.etBottom);

        binding.tiAll.setHint(String.format(Helper.getResString(R.string.property_enter_value), propertyType.toLowerCase()));
        binding.chkPtyAll.setText(String.format("%s on all sides", propertyType));

        MinMaxInputValidator ti_all = new MinMaxInputValidator(context, binding.tiAll, 0, 999);
        MinMaxInputValidator ti_left = new MinMaxInputValidator(context, binding.tiLeft, 0, 999);
        MinMaxInputValidator ti_right = new MinMaxInputValidator(context, binding.tiRight, 0, 999);
        MinMaxInputValidator ti_top = new MinMaxInputValidator(context, binding.tiTop, 0, 999);
        MinMaxInputValidator ti_bottom = new MinMaxInputValidator(context, binding.tiBottom, 0, 999);

        String lStr = resLeft != null && !resLeft.isEmpty() ? resLeft : String.valueOf(j);
        String tStr = resTop != null && !resTop.isEmpty() ? resTop : String.valueOf(k);
        String rStr = resRight != null && !resRight.isEmpty() ? resRight : String.valueOf(l);
        String bStr = resBottom != null && !resBottom.isEmpty() ? resBottom : String.valueOf(m);

        binding.etLeft.setText(lStr);
        binding.etTop.setText(tStr);
        binding.etRight.setText(rStr);
        binding.etBottom.setText(bStr);

        if (lStr.equals(tStr) && tStr.equals(rStr) && rStr.equals(bStr)) {
            binding.etAll.setText(lStr);
            binding.chkPtyAll.setChecked(true);
            binding.allPaddingView.setVisibility(VISIBLE);
            binding.individualPaddingView.setVisibility(GONE);
        } else {
            binding.chkPtyAll.setChecked(false);
            binding.individualPaddingView.setVisibility(VISIBLE);
            binding.allPaddingView.setVisibility(GONE);
        }

        binding.chkPtyAll.setOnClickListener(v -> {
            if (binding.chkPtyAll.isChecked()) {
                binding.individualPaddingView.setVisibility(GONE);
                binding.allPaddingView.setVisibility(VISIBLE);
                binding.etLeft.setText(Helper.getText(binding.etAll));
                binding.etTop.setText(Helper.getText(binding.etAll));
                binding.etRight.setText(Helper.getText(binding.etAll));
                binding.etBottom.setText(Helper.getText(binding.etAll));
                binding.etLeft.clearFocus();
                binding.etTop.clearFocus();
                binding.etRight.clearFocus();
                binding.etBottom.clearFocus();
            } else {
                binding.individualPaddingView.setVisibility(VISIBLE);
                binding.allPaddingView.setVisibility(GONE);
                binding.etAll.clearFocus();
            }
        });

        binding.etAll.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = Helper.getText(binding.etAll);
                binding.etLeft.setText(text);
                binding.etTop.setText(text);
                binding.etRight.setText(text);
                binding.etBottom.setText(text);
            }
        });

        binding.tvDpAll.setVisibility(View.GONE);
        binding.tvDpBottom.setVisibility(View.GONE);
        binding.tvDpLeft.setVisibility(View.GONE);
        binding.tvDpRight.setVisibility(View.GONE);
        binding.tvDpTop.setVisibility(View.GONE);

        binding.tiAll.setSuffixText("dp");
        binding.tiBottom.setSuffixText("dp");
        binding.tiLeft.setSuffixText("dp");
        binding.tiRight.setSuffixText("dp");
        binding.tiTop.setSuffixText("dp");

        dialog.setView(view);
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_save), (v, which) -> {
            String leftText = Helper.getText(binding.chkPtyAll.isChecked() ? binding.etAll : binding.etLeft).trim();
            String topText = Helper.getText(binding.chkPtyAll.isChecked() ? binding.etAll : binding.etTop).trim();
            String rightText = Helper.getText(binding.chkPtyAll.isChecked() ? binding.etAll : binding.etRight).trim();
            String bottomText = Helper.getText(binding.chkPtyAll.isChecked() ? binding.etAll : binding.etBottom).trim();

            int leftVal = parseDimenOrInt(leftText);
            int topVal = parseDimenOrInt(topText);
            int rightVal = parseDimenOrInt(rightText);
            int bottomVal = parseDimenOrInt(bottomText);

            String rL = leftText.startsWith("@dimen/") ? leftText : null;
            String rT = topText.startsWith("@dimen/") ? topText : null;
            String rR = rightText.startsWith("@dimen/") ? rightText : null;
            String rB = bottomText.startsWith("@dimen/") ? bottomText : null;

            a(leftVal, topVal, rightVal, bottomVal, rL, rT, rR, rB);
            if (valueChangeListener != null) {
                valueChangeListener.a(key, new int[]{leftVal, topVal, rightVal, bottomVal});
            }
            v.dismiss();
        });
        dialog.setNeutralButton("@dimen/", null);
        dialog.setNegativeButton(Helper.getResString(R.string.common_word_cancel), null);
        var alertDialog = dialog.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getButton(android.content.DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
                if (binding.chkPtyAll.isChecked()) {
                    binding.etAll.setText("@dimen/");
                    binding.etAll.setSelection(binding.etAll.getText().length());
                    binding.etAll.showDropDown();
                    binding.etAll.requestFocus();
                } else {
                    binding.etLeft.setText("@dimen/");
                    binding.etLeft.setSelection(binding.etLeft.getText().length());
                    binding.etLeft.showDropDown();
                    binding.etLeft.requestFocus();
                }
            });
        });
        alertDialog.show();
    }

    private int parseDimenOrInt(String str) {
        String trimmed = str.trim();
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
