package com.example.alvar.chatapp.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alvar.chatapp.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatOptionsDialog extends DialogFragment {

    //ui
    private View view;
    private LayoutInflater viewInflater;
    private AlertDialog.Builder builder;
    private CircleImageView photoIcon, locationIcon, pdfIcon, docxIcon;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        return showAlertDialog();
    }

    private Dialog showAlertDialog() {

        builder = new AlertDialog.Builder(getActivity());
        viewInflater = getActivity().getLayoutInflater();
        view = viewInflater.inflate(R.layout.chat_options_dialog, null);
        photoIcon = view.findViewById(R.id.shareImage);
        locationIcon = view.findViewById(R.id.shareLocation);
        pdfIcon = view.findViewById(R.id.sharePDF);
        docxIcon = view.findViewById(R.id.shareDocx);
        builder.setView(view);

        photoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "image option pressed", Toast.LENGTH_SHORT).show();
            }
        });

        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "location pressed", Toast.LENGTH_SHORT).show();
            }
        });
        pdfIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "pdf", Toast.LENGTH_SHORT).show();
            }
        });
        docxIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "docx", Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();

    }


}
