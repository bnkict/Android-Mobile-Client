package io.intelehealth.client.activities.todayPatientActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.homeActivity.HomeActivity;
import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.models.TodayPatientModel;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;

public class TodayPatientActivity extends AppCompatActivity {
    private static final String TAG = TodayPatientActivity.class.getSimpleName();
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    RecyclerView mTodayPatientList;
    AlertDialog.Builder dialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_patient);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_today_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);
        toolbar.setOverflowIcon(drawable);


        setSupportActionBar(toolbar);
        mTodayPatientList = findViewById(R.id.today_patient_recycler_view);
        sessionManager = new SessionManager(this);
        mDbHelper = new InteleHealthDatabaseHelper(this);
        db = mDbHelper.getWritableDatabase();
        if (sessionManager.isSyncFinished()) {
            doQuery();
        }

    }


    /**
     * This method retrieves visit details about patient for a particular
     * <p>
     * <p>
     * <p>
     * <p>
     * <p>
     * <p>
     * date.
     *
     * @return void
     */
    private void doQuery() {
        List<TodayPatientModel> todayPatientList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        String query =
                "SELECT tbl_visit.uuid, tbl_visit.patientuuid, tbl_visit.startdate, tbl_visit.enddate," +
                        "tbl_visit.uuid, tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name, " +
                        "tbl_patient.date_of_birth,tbl_patient.openmrs_id,tbl_patient.phone_number FROM tbl_visit, tbl_patient WHERE tbl_visit.patientuuid = tbl_patient.uuid " +
                        "AND tbl_visit.startdate LIKE '" + currentDate + "T%'" +
                        "ORDER BY tbl_patient.first_name ASC";
        //  "SELECT * FROM visit, patient WHERE visit.patient_id = patient._id AND visit.start_datetime LIKE '" + currentDate + "T%'";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    todayPatientList.add(new TodayPatientModel(
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone_number"))
                    ));
                } while (cursor.moveToNext());
            }
        }
        cursor.close();

        if (!todayPatientList.isEmpty()) {
            for (TodayPatientModel todayPatientModel : todayPatientList)
                Log.i(TAG, todayPatientModel.getFirst_name() + " " + todayPatientModel.getLast_name());

            TodayPatientAdapter mTodayPatientAdapter = new TodayPatientAdapter(todayPatientList, TodayPatientActivity.this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TodayPatientActivity.this);
            mTodayPatientList.setLayoutManager(linearLayoutManager);
            mTodayPatientList.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            mTodayPatientList.setAdapter(mTodayPatientAdapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.summary_endAllVisit:
                endAllVisit();

            case R.id.action_filter:
                //alert box.
                displaySingleSelectionDialog();    //function call


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void displaySingleSelectionDialog() {

       /* View checkBoxView = View.inflate(this, R.layout.checkbox_view, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Save to shared preferences
            }
        });
        checkBox.setText("Text to the right of the check box.");*/

        final String[] creator_names = {"Creator 1", "Creator 2", "Creator 3"};
        boolean[] checkedItems = {false, false, false};
        // ngo_numbers = getResources().getStringArray(R.array.ngo_numbers);
        dialogBuilder = new AlertDialog.Builder(TodayPatientActivity.this);
        dialogBuilder.setTitle("Filter by Creator");

        dialogBuilder.setMultiChoiceItems(creator_names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

            }
        });

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);
        //dialogBuilder.setView(checkBoxView);
        //dialogBuilder.setIcon(R.drawable.ic_sort_white_24dp);
        //  dialogBuilder.setItems(creator_names, new DialogInterface.OnClickListener() {
        // @Override
        //  public void onClick(DialogInterface dialog, int which) {
        // the user clicked on colors[which]
               /* final String a = "tel:"+"9769025715";
                final String b = "tel:"+"7304154312";

                if("Team 1".equals(ngo_numbers[which]))
                {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(a));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(i);
                }

                else if("Team 2".equals(ngo_numbers[which]))
                {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(b));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(i);
                }*/
        // }
        // });
        dialogBuilder.show();

    }


    private void endAllVisit() {

        int failedUploads = 0;

        String query = "SELECT tbl_visit.patientuuid, tbl_visit.enddate, tbl_visit.uuid," +
                "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name FROM tbl_visit, tbl_patient WHERE" +
                " tbl_visit.patientuuid = tbl_patient.uuid AND tbl_visit.enddate IS NULL OR tbl_visit.enddate = ''";

        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    boolean result = endVisit(
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    );
                    if (!result) failedUploads++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        if (failedUploads == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Unable to end " + failedUploads +
                    " visits.Please upload visit before attempting to end the visit.");
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    private boolean endVisit(String patientID, String patientName, String visitUUID) {

        return visitUUID != null;

    }
}




