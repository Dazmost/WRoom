package com.example.wroom;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wroom.Patient;
import com.example.wroom.R;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ModifyPatientActivity extends AppCompatActivity {

    public static final String LOG_TAG = ModifyPatientActivity.class.getName();
    /** URL for NodeJs Sever */
    private static final String REQUEST_URL ="http://192.168.2.13:8000/api/patients";
    //private static final String REQUEST_URL ="http://10.0.0.194:8000/api/patients";

    ArrayList<Patient> patientList;
    Patient currentPatient;
    int patientPosition = 0;
    TimePicker appointmentTimeWidget;
    TextView patientCode;
    TextView patientName;
    TextView patientPositionText;
    TextView patientCheckInDate;
    TextView patientCheckInTime;
    TextView patientAppointment;
    TextView patientCountDown;
    Button submit;
    Button cancel;

    long epochTime;
    long currentTimeInMilli = 1;
    long appointmentTimeTotal = 0;
    Boolean timeCheck = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modifypatient);

        //Receive Intent from MainActivity
        Intent intent = getIntent();
        patientList = new ArrayList<>();
        patientList = intent.getParcelableArrayListExtra("patientList");
        patientPosition = intent.getIntExtra("patientPosition", 0);
        currentPatient = patientList.get(patientPosition);


        //Display UI v/////////////////////////////////////////////////////////////////////////////////
        //Patient code display
        patientCode = (TextView) findViewById(R.id.patientcodeModify);
        String patientCodeString="#"+Integer.toString(currentPatient.getmPatientCode());
        patientCode.setText(patientCodeString);

        //Name display
        patientName = (TextView) findViewById(R.id.nameModify);
        patientName.setText(currentPatient.getmName());

        //Position display
        patientPositionText=(TextView)findViewById(R.id.positionModify) ;
        patientPositionText.setText(Integer.toString(currentPatient.getmLineNumber()));

        //Check-in date display
        Date dateObject = new Date(currentPatient.getmTime());
        patientCheckInDate = (TextView) findViewById(R.id.dateModify);
        String formattedDate = formatDate(dateObject);
        patientCheckInDate.setText(formattedDate);

        //Check-in time display
        patientCheckInTime = (TextView) findViewById(R.id.timeModify);
        String formattedTime2 = formatTime(dateObject);
        patientCheckInTime.setText(formattedTime2);

        //Appointment time display
        Date appdateObject = new Date(currentPatient.getmAppointmentTime());
        patientAppointment = (TextView) findViewById(R.id.appointment_timeModify);
        String formattedTime = formatTime(appdateObject);
        patientAppointment.setText(formattedTime);
        //Display ^/////////////////////////////////////////////////////////////////////////////////


        //COUNTDOWN FEATURE UI v////////////////////////////////////////////
        patientCountDown = (TextView) findViewById(R.id.countdown_Modify);

        long currentTime = System.currentTimeMillis();
        long countDown = currentPatient.getmAppointmentTime()-currentTime;

        new CountDownTimer(countDown, 1000) {
            public void onTick(long millisUntilFinished) {

                String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));
                patientCountDown.setText(hms);
            }
            public void onFinish() {
                patientCountDown.setText("00:00:00");
            }
        }.start();
        //COUNTDOWN FEATURE UI ^///////////////////////////////////////////


        //Cancel button click
        cancel = (Button) findViewById(R.id.cancel_modify);
        cancelClick();

        //Submit button click
        submit = (Button) findViewById(R.id.submit_modify);
        submitClick();
    }


    /**
     * Updates the patient list through an HTTP Putt by adding a new patient
     * when the submit button is clicked
     */
    public void submitClick(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //time in milliseconds from the widget
                appointmentTimeWidget = (TimePicker) findViewById(R.id.timePicker_modify);
                int timeHour = appointmentTimeWidget.getCurrentHour(); //was getHour()
                int timeMinute = appointmentTimeWidget.getCurrentMinute(); //was getMinute()
                long timeHourToMilli = TimeUnit.MINUTES.toMillis(timeMinute);
                long timeMinuteToMilli = TimeUnit.HOURS.toMillis(timeHour);
                appointmentTimeTotal = timeHourToMilli + timeMinuteToMilli;

                //current time in milliseconds
                Date today = Calendar.getInstance().getTime();
                currentTimeInMilli = today.getTime();

                //to get the time starting from midnight of today's date
                //so this would not include the time that has passed already today
                // Constructs a SimpleDateFormat using the given pattern
                SimpleDateFormat crunchifyFormat = new SimpleDateFormat("LLL dd, yyyy");
//                // format() formats a Date into a date/time string.
                String currentTime = crunchifyFormat.format(today);
                try {
                    // parse() parses text from the beginning of the given string to produce a date.
                    Date date = crunchifyFormat.parse(currentTime);
                    // getTime() returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
                    epochTime = date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                appointmentTimeTotal += epochTime;//epochtime = the time at midnight starting today's date


                //check that the appointment time is in the future and not in the past
                if (appointmentTimeTotal < currentTimeInMilli) {
                    Toast.makeText(ModifyPatientActivity.this, "Invalid Time", Toast.LENGTH_LONG).show();
                } else {
                    timeCheck = true;
                }

                //if the appointment time is in the future update the patient time in the patient list
                if (timeCheck) {
                    Log.d("Halim", Integer.toString(patientPosition));

                    //Patient newPatientTime = new Patient(currentPatient.getmPatientCode(), currentPatient.getmName(), patientPosition + 1, currentPatient.getmTime(), appointmentTimeTotal);
                    //patientList.set(patientPosition,newPatientTime);

                    //HTTP PUT v//////////////////////////////////////////////////////////////////////
                    myUpdateOperation(Integer.toString(currentPatient.getmPatientCode()), Long.toString(appointmentTimeTotal));
                    //HTTP PUT ^//////////////////////////////////////////////////////////////////////

//                    for(Patient p:patientList){
//                        System.out.println(p);
//                    }
                }
            }
        });
    }


    /**
     * Does not updates the patient list when the cancel button is clicked
     */
    public void cancelClick(){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("patientList", patientList);
                returnIntent.putExtra("button",false);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }




    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }




    //NETWORK CONNECTION v//////////////////////////////////////////////////////////////////////////
    private void myUpdateOperation(String patientCode, String appointmentTime ){
        ModifyPatientActivity.NetworkConnect task = new ModifyPatientActivity.NetworkConnect();
        task.execute(REQUEST_URL, patientCode, appointmentTime);
    }

    /**
     * Update the Patient List with the given information.
     */
    private void updateList(ArrayList <Patient> patients) {
        patientList=(ArrayList <Patient>)patients.clone();
        //Adding a Patient to the Patient array with sorting v//////////////////////////////////////
//                    patientList.add(patient);
        Collections.sort(patientList);//Sort by appointment time - see comparable function in Patient class

        //Edit LineNumber to reflect new order
        for (int j = 0; j < patientList.size(); j++) {
            Patient lineNumberPatient = patientList.get(j);
            lineNumberPatient.setmLineNumber(j + 1);
            patientList.set(j, lineNumberPatient);
        }

        //New position of patient after sorting
        int newPosition=0;
        for (int i= 0; i<patientList.size(); i++){
            Patient p= patientList.get(i);
            if (currentPatient.getmPatientCode()==p.getmPatientCode()){
                newPosition=i;
            }
        }

        Log.d("Patient Debug Modify","ArraySize: " + Integer.toString(patientList.size()) + " Position:" + (Integer.toString(newPosition)));

        Intent returnIntent = new Intent();
        returnIntent.putExtra("patientList", patientList);
        returnIntent.putExtra("patientPosition",newPosition);
        //Adding a Patient to the Patient array with sorting ^////////////////////////////
        returnIntent.putExtra("button",true);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    //Generic Data Types
    //String data type for input params because the string UGSURL is input to doInBackground method
    //Void for progress parameter because we do not need to update the user on the task
    //Result parameter is Event bc want the result of the doInBackground method to be an Event Object
    private class NetworkConnect extends AsyncTask<String, Void, ArrayList <Patient>> {

        //ALT+Enter to import the Async task at top of java file

        /**
         * This method is invoked (or called) on a background thread, so we can perform
         * long-running operations like making a network request.
         *
         * It is NOT okay to update the UI from a background thread, so we just return an
         * {@link ArrayList <Earthquake>} object as the result.
         */
        @Override
        protected ArrayList <Patient> doInBackground(String... params) {

            // Don't perform the request if there are no URLs, or the first URL is null.
            if (params.length < 1 || params[0] == null) {
                return null;
            }

            // Perform the HTTP request for earthquake data and process the response.
            //Util methods are declared static which means you can reference them using the class name without creating an instance of the Utils Object

            String putResponse = QueryUtils.putPatientData(params[0],params[1],params[2]);

            ArrayList <Patient> patients = QueryUtils.fetchPatientData(params[0]);
            return patients;
        }

        /**
         * This method is invoked on the main UI thread after the background work has been
         * completed.
         *
         * It IS okay to modify the UI within this method. We take the {@link ArrayList <Earthquake>} object
         * (which was returned from the doInBackground() method) and update the views on the screen.
         */
        @Override
        protected void onPostExecute(ArrayList <Patient> patients) {
            // If there is no result, do nothing.
            if (patients==null){
                return;
            }
            updateList(patients);
        }
    }
    //NETWORK CONNECTION ^///////////////////////////////////////////////////////////////////////////
}