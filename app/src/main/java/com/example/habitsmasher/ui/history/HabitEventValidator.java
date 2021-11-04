package com.example.habitsmasher.ui.history;

import androidx.fragment.app.FragmentActivity;

import com.example.habitsmasher.ui.dashboard.HabitValidator;

import java.util.Date;

/**
 * Habit Event Validator, extends off habit validator to also add input validation
 * for habit events
 */
public class HabitEventValidator extends HabitValidator {
    /**
     * Default constructor
     * @param context (FragmentActivity) The parent fragment activity
     */
    public HabitEventValidator(FragmentActivity context) {
        super(context);
    }

    /**
     * Checks if a habit event is valid
     * @param habitEventComment (String) The comment of the new habit event
     * @param habitEventDate (String) The date of the new habit event
     * @return True or False depending on validity
     */
    public boolean isHabitEventValid(String habitEventComment, String habitEventDate) {
        Date parsedDate = checkHabitDateValid(habitEventDate);

        if ((habitEventComment.length() <=0) || (habitEventComment.length() > 20)) {
            showToastMessage("Incorrect habit event comment entered");
            return false;
        }

        if (parsedDate == null) {
            showToastMessage("Please enter a start date");
            return false;
        }

        return true;
    }
}
