package com.zoiper.zdk.android.demo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * DialogUtil
 *
 * @since 29/05/2019
 */
public class DialogUtil {

    public interface AgreementPromptOnSuccess { void onSuccess(); }
    public interface TextPromptOnSuccess { void onSuccess(String result); }
    public interface OnCanceled { void onCanceled(); }

    public static void promptText(@NonNull Context context,
                                  @Nullable String message,
                                  @Nullable String positive,
                                  @Nullable String negative,
                                  @Nullable String inputHint,
                                  @Nullable TextPromptOnSuccess onSuccess,
                                  @Nullable OnCanceled onCanceled){
        DialogInterface.OnClickListener onPositiveClickListener = null;

        // Edit text to enter number.
        EditText input = new EditText(context);
        input.setHint(inputHint);

        // Layout params
        input.setLayoutParams(new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        if(onSuccess != null){
            onPositiveClickListener = (d, w) -> {
                if(input.getText().toString().matches("")){
                    input.setError("Can't be blank.");
                }else{
                    onSuccess.onSuccess(input.getText().toString().trim());
                    input.setError("");
                }
            };
        }

        // Padding
        int padding = (int) DensityPixelUtils.convertDpToPixel(20f, context);
        input.setPadding(padding, padding, padding, padding);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if(message != null) builder.setMessage(message);
        if(onCanceled != null) builder.setOnCancelListener((di) -> onCanceled.onCanceled());
        if(negative != null) builder.setNegativeButton(negative, null);

        builder.setPositiveButton(positive != null ? positive : "Ok", onPositiveClickListener);

        AlertDialog alertDialog = builder.create();

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setView(input);
        alertDialog.show();
    }

    public static void promptAgreement(@NonNull Context context,
                                       @Nullable String message,
                                       @Nullable String positive,
                                       @Nullable String negative,
                                       @Nullable AgreementPromptOnSuccess onSuccess,
                                       @Nullable OnCanceled onCanceled){
        DialogInterface.OnClickListener positiveClickListener = null;
        if(onSuccess != null){
            positiveClickListener = (ignored, ignored1) -> onSuccess.onSuccess();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message != null ? message : "Are you sure?");
        builder.setPositiveButton(positive != null ? positive : "Ok", positiveClickListener);

        if(onCanceled != null) builder.setOnCancelListener((di) -> onCanceled.onCanceled());
        if(negative != null) builder.setNegativeButton(negative, null);

        AlertDialog alertDialog = builder.create();

        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
}
