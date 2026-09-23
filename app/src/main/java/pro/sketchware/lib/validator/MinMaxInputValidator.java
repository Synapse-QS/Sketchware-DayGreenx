package pro.sketchware.lib.validator;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import a.a.a.MB;

public class MinMaxInputValidator extends MB {
    public int minValue;
    public int maxValue;
    public boolean allowDimen = true;

    public MinMaxInputValidator(Context context, TextInputLayout textInputLayout, int minValue, int maxValue) {
        this(context, textInputLayout, minValue, maxValue, true);
    }

    public MinMaxInputValidator(Context context, TextInputLayout textInputLayout, int minValue, int maxValue, boolean allowDimen) {
        super(context, textInputLayout);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.allowDimen = allowDimen;
        c = textInputLayout.getEditText();
        c.setFilters(new InputFilter[]{this});
        c.addTextChangedListener(this);
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String inputString = s.toString().trim();
        if (inputString.isEmpty()) {
            b.setError(String.format(Locale.US, "%d ~ %d", minValue, maxValue));
            d = false;
        } else if (allowDimen && inputString.startsWith("@dimen/")) {
            if (inputString.length() > "@dimen/".length()) {
                b.setError(null);
                d = true;
            } else {
                b.setError("Please enter dimension name");
                d = false;
            }
        } else {
            try {
                int inputNumber = Integer.parseInt(inputString);
                if (inputNumber >= minValue && inputNumber <= maxValue) {
                    b.setError(null);
                    d = true;
                } else {
                    b.setError(String.format(Locale.US, "%d ~ %d", minValue, maxValue));
                    d = false;
                }
            } catch (NumberFormatException e) {
                b.setError(allowDimen ? String.format(Locale.US, "%d ~ %d or @dimen/...", minValue, maxValue) : String.format(Locale.US, "%d ~ %d", minValue, maxValue));
                d = false;
            }
        }
    }
}
