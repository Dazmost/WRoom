package com.example.wroom;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class PatientActivity extends AppCompatActivity {


    public static final String LOG_TAG = PatientActivity.class.getName();
    private static final String REQUEST_URL ="http://10.0.0.194:8000/api/patients";

    ArrayList<Patient> patientList;
    EditText name;
    TimePicker appointmentTimeWidget;
    TextView patientCodeTextView;
    Button submit;
    long epochTime;
    long currentTimeInMilli=1;
    long appointmentTimeTotal=0;
    Boolean timeCheck=false;
    Boolean randomNumberCheck=true;
    int randomPatientCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        //receives the intent and patient list data
        Intent intent = getIntent();
        patientList=new ArrayList<>();
        patientList= intent.getParcelableArrayListExtra("patientList");

        //Generate a random distinct Patient Code
        randomPatientCode=generateRandomPatientCode(patientList);


        //sets the patient code view in the xml
        patientCodeTextView=(TextView)findViewById(R.id.randomPatientCode);
        String patientCodeString="Patient Code: #"+Integer.toString(randomPatientCode);
        patientCodeTextView.setText(patientCodeString);

        //define the submit button
        submit=(Button) findViewById(R.id.submit);
        submitClick();
    }




    public int generateRandomPatientCode (ArrayList<Patient> patientList){
        int randomPatientCode = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);

        //check for duplicate patient code in the patient list
        if (!patientList.isEmpty()) {
            while (randomNumberCheck) {
                for (int i = 0; i < patientList.size(); i++) {
                    Patient currentPatient = patientList.get(i);
                    if (randomPatientCode == currentPatient.getmPatientCode()) {
                        randomPatientCode = ThreadLocalRandom.current().nextInt(1000, 9999 + 1);
                        randomNumberCheck = true;
                        break;
                    }
                    randomNumberCheck = false;
                }
            }
        }
        return randomPatientCode;
    }

    /**
     * Updates the patient list through an HTTP Post by adding a new patient
     * when the submit button is clicked
     */
    public void submitClick(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name=(EditText)findViewById(R.id.name_submit);
                String nameString = name.getText().toString();

                appointmentTimeWidget = (TimePicker) findViewById(R.id.timePicker);
                int timeHour = appointmentTimeWidget.getHour();
                int timeMinute = appointmentTimeWidget.getMinute();

                appointmentTimeTotal=convertTimePickerToAppointmentTIme(timeHour,timeMinute);

                Date today = Calendar.getInstance().getTime();
                currentTimeInMilli = today.getTime();
                if(appointmentTimeTotal<currentTimeInMilli){
                    Toast.makeText(PatientActivity.this, "Invalid Time", Toast.LENGTH_LONG).show();
                }else{
                    timeCheck=true;
                }

                if (timeCheck) {
                    //Patient currentPatient = new Patient(randomPatientCode, nameString, patientList.size()+1, currentTimeInMilli, appointmentTimeTotal);

                    //HTTP POST v/////////////////////////////////////////////////////////////////////
                    myUpdateOperation(Integer.toString(randomPatientCode), nameString, Integer.toString(patientList.size()+1), Long.toString(currentTimeInMilli), Long.toString(appointmentTimeTotal));
                    //HTTP POST ^/////////////////////////////////////////////////////////////////////

                }
            }
        });

    }

    /**
     * Converts the provided (Hours:Minutes) time to epoch time
     *
     * @param timeHour the time in hours
     * @param timeMinute the rest of the time in minutes
     * @return
     */
    public long convertTimePickerToAppointmentTIme (int timeHour, int timeMinute) {
        long timeHourToMilli = TimeUnit.MINUTES.toMillis(timeMinute);
        long timeMinuteToMilli = TimeUnit.HOURS.toMillis(timeHour);
        long TimeTotal = timeHourToMilli + timeMinuteToMilli;

        Date today = Calendar.getInstance().getTime();


        // Constructs a SimpleDateFormat using the given pattern
        SimpleDateFormat crunchifyFormat = new SimpleDateFormat("LLL dd, yyyy");
        // format() formats a Date into a date/time string.
        String currentTime = crunchifyFormat.format(today);
        try {
            // parse() parses text from the beginning of the given string to produce a date.
            Date date = crunchifyFormat.parse(currentTime);
            // getTime() returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
            epochTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TimeTotal += epochTime;
        return TimeTotal;
    }


    //NETWORK CONNECTION v/////////////////////////////////////////////////

    /**
     * Executes the HTTP Requests: Post and Get
     *
     * @param patientCode - The random 4-digit identification code of the patient
     * @param name -  Patient name
     * @param lineNumber - The patient's position in the queue
     * @param time - The patient's check-in time
     * @param appointmentTime - The time of the patient's appointment
     */
    private void myUpdateOperation(String patientCode, String name, String lineNumber, String time,
                                   String appointmentTime ){
        NetworkConnect task = new NetworkConnect();
        task.execute(REQUEST_URL, patientCode, name, lineNumber, time, appointmentTime);
    }



    /**
     * Update the Patient List with the given information.
     */
    private void updateList(ArrayList <Patient> patients) {
        patientList=(ArrayList <Patient>)patients.clone();
        //Adding a Patient to the Patient array with sorting v////////////////////////////
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
            if (randomPatientCode==p.getmPatientCode()){
                newPosition=i;
            }
        }

        Log.d("Patient Activity Debig","ArraySize: " + Integer.toString(patientList.size()) + " Position:" + (Integer.toString(newPosition)));

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
    //String data type for input params because the string URL is input to doInBackground method
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
            //Util methods are declared static which means you can reference them using the class name wihtout creating an instance of the Utils Object

            String postResponse = QueryUtils.postPatientData(params[0],params[1],params[2],params[3],params[4],params[5]);

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
    //NETWORK CONNECTION ^///////////////////////////////////////////////




}
