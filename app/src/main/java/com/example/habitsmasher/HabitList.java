package com.example.habitsmasher;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.habitsmasher.listeners.FailureListener;
import com.example.habitsmasher.listeners.FailureListenerWithToast;
import com.example.habitsmasher.listeners.SuccessListener;
import com.example.habitsmasher.listeners.SuccessListenerWithToast;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class that acts as a container for habits, allowing for edit, delete and add operations
 */
public class HabitList extends ArrayList<Habit>{

    // array of Habits which reflect the database wrapped within HabitList
    private ObservableSnapshotArray<Habit> _snapshots;

    // arraylist of habits, auto-updates whenever Habit added or edited
    private ArrayList<Habit> _habits = new ArrayList<>();

    // set of IDs of existing Habits
    public static HashSet<Long> habitIdSet = new HashSet<>();

    /**
     * Getter method to access Habit at pos
     * @return habit
     */
    public Habit getHabit(int pos) {
        return _habits.get(pos);
    }

    /**
     * Returns the list of habits as an ArrayList
     * @return _habits
     */
    public ArrayList<Habit> getHabitList() {
        return _habits;
    }

    /**
     * Wraps a snapshots array within the HabitList
     * @param snapshots
     */
    public void setSnapshots(ObservableSnapshotArray<Habit> snapshots) {
        _snapshots = snapshots;
    }

    /**
     * Method that adds a habit with specified fields to the habit list of a specified
     * user in the database
     * @param title title of added habit
     * @param reason reason of added habit
     * @param date date of added habit
     * @param userId id of the user of the habit list the habit is being added to
     */
    public void addHabitToDatabase(String title, String reason, Date date, DaysTracker tracker, String userId) {

        // get collection of specified user (do we need this?)
        FirebaseFirestore _db = FirebaseFirestore.getInstance();
        final CollectionReference _collectionReference = _db.collection("Users")
                                                            .document(userId)
                                                            .collection("Habits");
        // generate a random ID for HabitID
        Long habitId = generateHabitId();

        // initialize fields
        HashMap<String, Object> habitData = new HashMap<>();
        habitData.put("title", title);
        habitData.put("reason", reason);
        habitData.put("date", date);
        habitData.put("id", habitId);
        habitData.put("days", tracker.getDays());

        // add habit to database, using it's habit ID as the document name
        setHabitDataInDatabase(userId, habitId.toString(), habitData);
        addHabitLocal(new Habit(title, reason, date, tracker.getDays(), habitId, new HabitEventList()));
    }

    /**
     * Method that adds a Habit to the local habitList
     * @param habit Habit to be added
     */
    public void addHabitLocal(Habit habit) {
        _habits.add(habit);
    }

    /**
     * Method that edits a Habit in the specified pos in the local HabitList
     * @param newTitle new title of habit
     * @param newReason new reason of habit
     * @param newDate new date of habit
     * @param pos position of habit
     * @param tracker days of the week the habit takes place
     */
    public void editHabitLocal(String newTitle, String newReason, Date newDate, DaysTracker tracker, int pos) {
        Habit habit = _habits.get(pos);
        habit.setTitle(newTitle);
        habit.setReason(newReason);
        habit.setDate(newDate);
        habit.setDays(tracker.getDays());
    }

    /**
     * Method that edits the habit at position pos in the database
     * @param newTitle New title of habit
     * @param newReason New reason of habit
     * @param newDate New date of habit
     * @param pos Position of habit in the HabitList
     * @param userId id of user whose habits we are editing
     */
    public void editHabitInDatabase(String newTitle, String newReason, Date newDate, DaysTracker tracker, int pos, String userId) {

        // this acquires the unique habit ID of the habit to be edited
        Long habitId = _habits.get(pos).getId();

        // stores the new fields of the Habit into a hashmap
        HashMap<String, Object> habitData = new HashMap<>();
        habitData.put("title", newTitle);
        habitData.put("reason", newReason);
        habitData.put("date", newDate);
        habitData.put("id", habitId);
        habitData.put("days", tracker.getDays());

        // replaces the old fields of the Habit with the new fields, using Habit ID to find document
        setHabitDataInDatabase(userId, habitId.toString(), habitData);
    }

    // TODO: this is a temporary implementation of generating unique habitIDs, improve this
    /**
     * Generate a habit ID for a new habit
     * @return the generated habit ID
     */
    private long generateHabitId() {
        long habitIdCounter = 1;
        while(habitIdSet.contains(habitIdCounter)) {
            habitIdCounter++;
        }
        habitIdSet.add(habitIdCounter);
        return habitIdCounter;
    }

    /**
     * Sets the fields of habit belonging the user username with the habit ID id
     * to the ones specified by data
     * @param userId id of user
     * @param id id of habit
     * @param data fields of habit
     */
    private void setHabitDataInDatabase(String userId, String id, HashMap<String, Object> data) {
        // get collection of Habits for a specified user
        FirebaseFirestore _db = FirebaseFirestore.getInstance();
        final CollectionReference _collectionReference = _db.collection("Users")
                .document(userId)
                .collection("Habits");

        // create the new document and add it
        _collectionReference.document(id)
                .set(data)
                .addOnSuccessListener(new SuccessListener(TAG, "Data successfully added."))
                .addOnFailureListener(new FailureListener(TAG, "Data failed to be added."));
    }

    /**
     * This method is responsible for deleting a habit from both locally and the db
     * @param context the current application context
     * @param userId the current user's username
     * @param habitToDelete the habit to delete
     * @param habitPosition the position of the habit to delete
     */
    public void deleteHabit(Context context,
                            String userId,
                            Habit habitToDelete,
                            int habitPosition) {
        // delete locally
        deleteHabitLocally(habitPosition);

        // delete from firebase
        deleteHabitFromDatabase(context, userId, habitToDelete);
    }

    /**
     * This method deletes a habit from the local habit list
     * @param habitPosition the index of the habit to delete
     */
    public void deleteHabitLocally(int habitPosition) {
        _habits.remove(habitPosition);
    }

    /**
     * This method deletes a habit from the database
     * @param context the current application context
     * @param userId the current user's id
     * @param habitToDelete the habit to delete
     */
    private void deleteHabitFromDatabase(Context context, String userId, Habit habitToDelete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // get the habit to delete, then delete it
        db.collection("Users")
          .document(userId)
          .collection("Habits")
          .document(String.valueOf(habitToDelete.getId()))
          .delete()
          .addOnSuccessListener(new SuccessListenerWithToast(context,
                  "deleteHabit", "Data successfully deleted.",
                  "Habit deleted!"))
                .addOnFailureListener(new FailureListenerWithToast(context,
                        "deleteHabit", "Data failed to be deleted.",
                                "Something went wrong!"));
    }
}