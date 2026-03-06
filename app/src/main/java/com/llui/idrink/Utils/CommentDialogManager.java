package com.llui.idrink.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.llui.idrink.Interfaces.CommentDialogListener;
import com.llui.idrink.R;

import java.util.Locale;

public class CommentDialogManager {
    Context mContext;
    View dialogView;
    EditText inputEditText;
    Button speechToTextButton;
    CommentDialogListener listener;


    public CommentDialogManager(Context context, CommentDialogListener listener, String commentTxt) {
        this.mContext = context;
        this.listener = listener;
        dialogView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_comment, null);
        inputEditText = dialogView.findViewById(R.id.inputEditText);
        inputEditText.setText(commentTxt);
        speechToTextButton = dialogView.findViewById(R.id.speechToTextButton);
    }
    private void startSpeechToText(Context context) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (context instanceof Activity) {
            try {
                ((Activity) context).startActivityForResult(intent, 1);
            } catch (Exception e) {
                Toast.makeText(context, "Your device does not support speech input", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void showCommentDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add Comment");

        speechToTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText(mContext);
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String comment = inputEditText.getText().toString();
                listener.onCommentSubmitted(comment, index);
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }
    public String getString() {
        return inputEditText.getText().toString();
    }
    public void setText(String text) {
        inputEditText.setText(text);
    }
}
