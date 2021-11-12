package com.example.habitsmasher.ui.history;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.example.habitsmasher.DatabaseEntity;
import com.example.habitsmasher.DatePickerDialogFragment;
import com.example.habitsmasher.HabitEvent;
import com.example.habitsmasher.HabitEventDialog;
import com.example.habitsmasher.R;

/**
 * The AddHabitEventDialog
 * Deals with UI and information handling of the add habit event popup
 */
public class AddHabitEventDialog extends HabitEventDialog {

    private AddHabitEventDialog _addFragment = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate view and attach view elements
        View view = inflater.inflate(R.layout.add_habit_event_dialog, container, false);
        initializeUIElements(view);

        // set header
        _header.setText("Add Habit Event");
        _errorText.setText("");

        TAG = "AddHabitEventDialog";

        // Add listener to date text to open date picker
        setDateTextViewListener();

        // Add listener to cancel button which closes dialog
        setCancelButtonListener();

        // Add listener to confirm button that adds events to database and closed dialog
        setConfirmButtonListener();

        // Add listener to image view
        _eventPictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open gallery to let user pick photo
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });

        return view;
    }

    @Override
    protected void setConfirmButtonListener() {
        _confirmButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                HabitEventValidator habitEventValidator = new HabitEventValidator(_addFragment);

                String habitEventComment = _eventCommentText.getText().toString();
                String habitEventDate = _eventDateText.getText().toString();

                // Check if event data is valid
                if (habitEventValidator.isHabitEventValid(habitEventComment, habitEventDate)) {
                    // If everything is valid, add event to database, events list, and close dialog
                    HabitEvent newEvent = new HabitEvent(habitEventValidator.checkHabitDateValid(habitEventDate),
                            habitEventComment,
                            DatabaseEntity.generateId());
                    _errorText.setText("");
                    _habitEventListFragment.updateListAfterAdd(newEvent);
                    getDialog().dismiss();
                }

            }
        });
    }

    /**
     * Reference: https://stackoverflow.com/questions/10165302/dialog-to-pick-image-from-gallery-or-from-camera
     * Override onActivityResult to handle when user has selected image
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
            case 1:
                if (resultCode == RESULT_OK) {
                    // Set selected picture
                    _selectedImage = imageReturnedIntent.getData();
                    _eventPictureView.setImageURI(_selectedImage);
                }
                break;
        }
    }
}
