package com.example.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;

public class ResolveDialogFragment extends DialogFragment {
    public interface OkListener {
        void onOkPressed(String dialogValue);
    }
    private OkListener okListener;
    private EditText shortCodeField;
    /** Sets a listener that is invoked when the OK button on this dialog is pressed. */
    public void setOkListener(OkListener okListener) {
        this.okListener = okListener;
    }
    /**
     * Creates a simple layout for the dialog. This contains a single user-editable text field whose
     * input type is retricted to numbers only, for simplicity.
     */
    private LinearLayout getDialogLayout() {
        Context context = getContext();
        LinearLayout layout = new LinearLayout(context);
        shortCodeField = new EditText(context);
        shortCodeField.setInputType(InputType.TYPE_CLASS_NUMBER);
        shortCodeField.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        shortCodeField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        layout.addView(shortCodeField);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return layout;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setView(getDialogLayout())
                .setTitle("Resolve Anchor")
                .setPositiveButton(
                        "OK",
                        (dialog, which) -> {
                            Editable shortCodeText = shortCodeField.getText();
                            Log.d("okpressed: ", shortCodeText.toString());
                            if (okListener != null && shortCodeText != null && shortCodeText.length() > 0) {
                                // Invoke the callback with the current checked item.
                                okListener.onOkPressed(shortCodeText.toString());

                            }
                        })
                .setNegativeButton("Cancel", (dialog, which) -> {});
        return builder.create();
    }
}
