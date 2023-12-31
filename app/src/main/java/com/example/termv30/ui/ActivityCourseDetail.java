package com.example.termv30.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.termv30.R;
import com.example.termv30.database.Repository;
import com.example.termv30.entities.AssessmentEntity;
import com.example.termv30.entities.CourseEntity;
import com.example.termv30.helper.CourseStatus;
import com.example.termv30.helper.DateHelper;
import com.example.termv30.helper.DateSelection;
import com.example.termv30.helper.MyReceiver;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityCourseDetail extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Repository repository;

    public static int numAlert;

    public static int termID = -1;

    int courseID;
    EditText courseTitle;
    EditText startDate;
    EditText endDate;
    EditText courseNotes;
    Spinner courseStatus;
    EditText professorName;
    EditText professorPhone;
    EditText professorEmail;

    CourseEntity currentCourse;

    public static int numberAssessments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        System.out.println(ActivityAssessmentDetail.courseIdAssessmentDetail);

        setSpinnerContents(); //Make course menu of course status

// make variables
        Button addAssessmentButton = (Button) findViewById(R.id.add_button);
        courseID = getIntent().getIntExtra("courseID", -1);
        termID = getIntent().getIntExtra("termID", -1);

        // below Sets variables to return  to  Course Entity from Assessment Detail
        if (courseID == -1) {
            courseID = ActivityAssessmentDetail.courseIdAssessmentDetail;
        }

        if (termID == -1) {
            termID = ActivityAssessmentDetail.termIdAssessmentDetail;
        }

        // below, repository enters fields, so long as its not creating a new Entity.
        repository = new Repository((getApplication()));
        List<CourseEntity> allCourses = repository.getAllCourses();

        for(CourseEntity course:allCourses){
            if (course.getCourseID() == courseID)
                currentCourse = course;
        }

        courseTitle = findViewById(R.id.course_title_editText);
        startDate = findViewById(R.id.course_start_date_editText);
        endDate = findViewById(R.id.course_end_date_editText);
        courseNotes = findViewById(R.id.course_notes_textEdit);
        courseStatus = findViewById(R.id.spinner_course_status);

        professorName = findViewById(R.id.professor_name);
        professorPhone = findViewById(R.id.professor_phone);
        professorEmail = findViewById(R.id.professor_email);

         if(currentCourse != null){
            courseTitle.setText(currentCourse.getCourseTitle());
            startDate.setText(currentCourse.getStartDate().format(DateHelper.dtf));
            endDate.setText(currentCourse.getEndDate().format(DateHelper.dtf));
            courseNotes.setText(currentCourse.getCourseNotes());
            courseStatus.setSelection(currentCourse.getStatus().ordinal());
            professorName.setText(currentCourse.getInstructorName());
            professorPhone.setText(currentCourse.getInstructorPhone());
            professorEmail.setText(currentCourse.getInstructorEmail());
         }
         else {
             getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
             addAssessmentButton.setVisibility(View.GONE);
         }

         courseNotes.setVisibility(View.VISIBLE);

        // display related assessments
        repository = new Repository(getApplication());
        RecyclerView recyclerView = findViewById(R.id.recyclerview_assessment);
        final AdapterAssessment adapter = new AdapterAssessment(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<AssessmentEntity> filteredAssessmentList = new ArrayList<>();
        for(AssessmentEntity assessment: repository.getAllAssessments()){
            if (assessment.getCourseID() == courseID)
                filteredAssessmentList.add(assessment);
        }

        numberAssessments = filteredAssessmentList.size();
        adapter.setAssessments(filteredAssessmentList);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // TODO: @NonNull was added above, delete??

                repository.delete(adapter.getAssessmentAt(viewHolder.getAdapterPosition()));
                adapter.mAssessments.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                Snackbar snackbar = Snackbar.make(findViewById(R.id.course_details), "The Assessment Has Been Deleted", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }).attachToRecyclerView(recyclerView);

        if (getIntent().getBooleanExtra("courseSaved", false))
            Toast.makeText(this,"The Course Has Been Saved",Toast.LENGTH_LONG).show();

        if (getIntent().getBooleanExtra("assessmentSaved", false))
            Toast.makeText(this,"The Assessment Has Been Saved",Toast.LENGTH_LONG).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.email_notes) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, courseTitle.getText().toString() + " " + courseNotes.getText().toString());

            sendIntent.putExtra(Intent.EXTRA_TITLE, courseTitle.getText().toString() + " Notes");
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            return true;
        }

        if (item.getItemId() == R.id.course_notification_start) {
            String dateFromScreen = startDate.getText().toString();
            String myFormat = "MM/dd/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            Date myDate = null;
            try {
                myDate = sdf.parse(dateFromScreen);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Long trigger = myDate.getTime();
            Intent intent = new Intent(ActivityCourseDetail.this, MyReceiver.class);
            intent.putExtra("courseAlert", " The Course: '" + courseTitle.getText().toString() + "' start date notification has been set.");
            PendingIntent sender=PendingIntent.getBroadcast(ActivityCourseDetail.this,++numAlert, intent,PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger,sender);
            return true;
        }

        if (item.getItemId() == R.id.course_notification_end) {
            String dateFromScreen = endDate.getText().toString();
            String myFormat = "MM/dd/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            Date myDate = null;
            try {
                myDate = sdf.parse(dateFromScreen);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Long trigger = myDate.getTime();
            Intent intent = new Intent(ActivityCourseDetail.this, MyReceiver.class);
            intent.putExtra("courseAlert", " The Course: '" + courseTitle.getText().toString() + "' end date notification has been set.");
            PendingIntent sender=PendingIntent.getBroadcast(ActivityCourseDetail.this,++numAlert, intent,PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger,sender);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSpinnerContents() {
        // content of the spinner
        Spinner courseStatusSpinner = (Spinner) findViewById(R.id.spinner_course_status);

        courseStatusSpinner.setOnItemSelectedListener(this);

        List<CourseStatus> categories = new ArrayList<CourseStatus>();
        categories.add(CourseStatus.IN_PROGRESS);
        categories.add(CourseStatus.COMPLETED);
        categories.add(CourseStatus.DROPPED);
        categories.add(CourseStatus.PLAN_TO_TAKE);

        //  Adaptr  for drop down spinner
        ArrayAdapter<CourseStatus> dataAdapter = new ArrayAdapter<CourseStatus>(this, android.R.layout.simple_spinner_item, categories);

        // layout is Drop-down menu
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // connect data adapter to spinner
        courseStatusSpinner.setAdapter(dataAdapter);
    }

    public void addAssessmentButton(View view) {

        Intent intent = new Intent(ActivityCourseDetail.this, ActivityAssessmentDetail.class);
        intent.putExtra("courseID", courseID);
        intent.putExtra("termID", termID);
        startActivity(intent);
    }

    public void saveCourseButton(View view) {
        if (courseTitle.getText().toString().trim().isEmpty() ||
        startDate.getText().toString().trim().isEmpty() ||
        endDate.getText().toString().trim().isEmpty() ||
        professorName.getText().toString().trim().isEmpty() ||
        professorPhone.getText().toString().trim().isEmpty() ||
        professorPhone.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Error: Empty Fields", Toast.LENGTH_LONG).show();
            return;
        }

        CourseEntity course;

        if (courseID == -1) {
            List<CourseEntity> allCourses = repository.getAllCourses();
            courseID = allCourses.get(allCourses.size() - 1).getCourseID();
            ++courseID;
        }

        course = new CourseEntity
            (
                courseID,
                courseTitle.getText().toString(),
                DateHelper.parseDate(startDate.getText().toString()),
                DateHelper.parseDate(endDate.getText().toString()),
                CourseStatus.fromString(courseStatus.getSelectedItem().toString()),
                courseNotes.getText().toString(),
                professorName.getText().toString(),
                professorPhone.getText().toString(),
                professorEmail.getText().toString(),
                termID
            );
        repository.insert(course);

        Intent intent = new Intent(ActivityCourseDetail.this, ActivityTermDetail.class);
        intent.putExtra("courseSaved",true);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void showDateClickerDialog(View view) {
        int viewID = view.getId();
        TextView datePickerView = findViewById(viewID);
        DialogFragment newFragment = new DateSelection(datePickerView);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}