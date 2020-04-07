package com.example.wroom;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.AbsListView.MultiChoiceModeListener;

import java.util.ArrayList;
import java.util.Collections;

public class PatientFragment extends Fragment {

    public static final String LOG_TAG = PatientFragment.class.getName();
    /** URL for NodeJs Sever */
    private static final String REQUEST_URL ="http://10.0.0.194:8000/api/patients";

    View rootView;
    //DatabaseHelper myDB;
    PatientAdapter adapter;
    ListView patientListView;
    SwipeRefreshLayout mySwipeRefreshLayout;


    static final int PATIENT_CONTACT_REQUEST = 1; // The request code.
    static final int MODIFY_PATIENT_REQUEST = 2; // The request code.

    private FloatingActionButton fab;
    public ArrayList<Patient> patients;

    public PatientFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_patient, container, false);

        patients = new ArrayList<Patient>();

        /**
         * NETWORK CONNECTION - In order to perform HTTP Requests
         * swipe down feature to reload patient list from server database
         */
        //NETWORK CONNECTION v///////////////////////////////////////////////////////////////////////
        mySwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);


        // Create an {@link AsyncTask} to perform the HTTP request to the given URL
        // on a background thread. When the result is received on the main UI thread,
        // then update the UI.
        NetworkConnect task = new NetworkConnect();//Same as myUpdateOperation();
        task.execute(REQUEST_URL);

        /**
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        myUpdateOperation();
                    }
                }
        );
        //NETWORK CONNECTION ^///////////////////////////////////////////////////////////////////////


        //SQL management
        //myDB = new DatabaseHelper(getActivity());
        //patients=getAll(patients);
        //^SQL management

        patientListView = (ListView) rootView.findViewById(R.id.list);
        //updateUi(patients); //Update the UI and onclicklistener



        //MULTIPLE SELECT v/////////////////////////////////////////////////////////////////////////
        patientListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        patientListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = patientListView.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                adapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.delete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = adapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                Patient selecteditem = adapter
                                        .getItem(selected.keyAt(i));
                                // Remove selected items following the ids
                                adapter.remove(selecteditem);

                                ///////////////////////////////////ADDED v////////////////////
                                //patients.remove(selecteditem);
                                myDeleteOperation(Integer.toString(selecteditem.getmPatientCode()), "delete");
//                                DeleteData(selecteditem.getmPatientCode());
                                ///////////////////////////////////ADDED ^////////////////////
                            }
                        }
                        ///////////////////////////////////ADDED v////////////////////
                        //New position of patient after sorting
//                        for (int i= 0; i<patients.size(); i++){
//                            Patient p= patients.get(i);
//                            p.setmLineNumber(i+1);
//                            patients.set(i,p);
//                        }

//                        //Edit LineNumber to reflect new order in SQL
//                        for (int j = 0; j < patients.size(); j++) {
//                            Patient lineNumberPatient = patients.get(j);
//                            UpdateData(lineNumberPatient.getmPatientCode(),lineNumberPatient.getmName(),
//                                    lineNumberPatient.getmLineNumber(), lineNumberPatient.getmTime(),
//                                    lineNumberPatient.getmAppointmentTime());
//                        }
                        ///////////////////////////////////ADDED ^////////////////////

                        // Close CAB
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.fragment_patient, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        //MULTIPLE SELECT ^/////////////////////////////////////////////////////////////////////////




        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Patient patient = patients.get(position);
                Log.d("Halim Item", patients.toString()+ "ArraySize: " + Integer.toString(patients.size()) + " Position:" + (Integer.toString(position)));
                System.out.println(patients.toString());

                Intent intentPatientModify = new Intent(getActivity(), ModifyPatientActivity.class);
                intentPatientModify.putExtra("patientList", patients);
                intentPatientModify.putExtra("patientPosition",position);
                startActivityForResult(intentPatientModify,MODIFY_PATIENT_REQUEST);
            }
        });



        //Floating Action Button Click
        fab= (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Fab Clicked", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(),PatientActivity.class);
                intent.putExtra("patientList", patients);
                startActivityForResult(intent,PATIENT_CONTACT_REQUEST);

            }
        });

        return rootView;
    }//OnCreateView ^///////////////////////////////////////////////////////////////////////////////


    /**
     * The return code once PatientActivity or ModifyPatientActivity are done with their activities and the intent is finished
     * @param requestCode can be MODIFY_PATIENT_REQUEST or PATIENT_CONTACT_REQUEST to identify the request/intent
     * @param resultCode can be Activity.RESULT_OK to signify a successful intent
     * @param data the data (i.e. Patient List) that is sent back from the intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        // Check which request we're responding to /////////////////////////////////////////////////
        /**
         * Execute the following code when the intent is done and returns to PatientFragment
         * PatientActivity.class intent
         */
        if (requestCode == PATIENT_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                patients=data.getParcelableArrayListExtra("patientList");
                final int newPatientPosition=data.getIntExtra("patientPosition", 0);
                final boolean buttonPress = data.getBooleanExtra("button",false);
                Log.d("Patient Activity Debiig","ArraySize: " + Integer.toString(patients.size()) + " Position:" + (Integer.toString(newPatientPosition)));

                //myUpdateOperation();
                //SQL method v//////////////////////////////////////////////////////////////////////
//                Patient currentPatient=patients.get(newPatientPosition);
//                AddData(currentPatient.getmPatientCode(),currentPatient.getmName(),currentPatient.getmLineNumber(),
//                        currentPatient.getmTime(),currentPatient.getmAppointmentTime());
//
//                //ViewModel Update
//                model = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
//                model.setPatients(patients);
//
//
//                //Edit LineNumber to reflect new order in SQL
//                for (int j = 0; j < patients.size(); j++) {
//                    Patient lineNumberPatient = patients.get(j);
//                    UpdateData(lineNumberPatient.getmPatientCode(),lineNumberPatient.getmName(),
//                            lineNumberPatient.getmLineNumber(), lineNumberPatient.getmTime(),
//                            lineNumberPatient.getmAppointmentTime());
//                }
                //SQL method ^//////////////////////////////////////////////////////////////////////


                Log.d("Halim", patients.toString()+ " " + Integer.toString(patients.size()));
                System.out.println(patients.toString());

                final ListView patientListView = (ListView) rootView.findViewById(R.id.list);
                // Do something with the contact here (bigger example below)
                adapter = new PatientAdapter(getActivity(),R.layout.list_item,patients);
                //patientListView = (ListView) findViewById(R.id.list);
                // Set the adapter onto the view pager
                patientListView.setAdapter(adapter);
                patientListView.setSelection(newPatientPosition);

                if (buttonPress) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateView(newPatientPosition, patientListView);//INDICATOR COLOR CHANGE method
                        }
                    }, 10);
                }


            }
        }

        // Check which request we're responding to ////////////////////////////////////////////////
        /**
         * Execute the following code when the intent is done and returns to PatientFragment
         * ModifyPatientActivity.class intent
         */
        if (requestCode == MODIFY_PATIENT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.


                patients=data.getParcelableArrayListExtra("patientList");
                final int newPatientPosition=data.getIntExtra("patientPosition", 0);
                final boolean buttonPress = data.getBooleanExtra("button",false);

                //SQL method v//////////////////////////////////////////////////////////////////////
//                Patient currentPatient=patients.get(newPatientPosition);
//                UpdateData(currentPatient.getmPatientCode(),currentPatient.getmName(),currentPatient.getmLineNumber(),
//                        currentPatient.getmTime(),currentPatient.getmAppointmentTime());
//
//                //Edit LineNumber to reflect new order in SQL
//                for (int j = 0; j < patients.size(); j++) {
//                    Patient lineNumberPatient = patients.get(j);
//                    UpdateData(lineNumberPatient.getmPatientCode(),lineNumberPatient.getmName(),
//                            lineNumberPatient.getmLineNumber(), lineNumberPatient.getmTime(),
//                            lineNumberPatient.getmAppointmentTime());
//                }
                //SQL method ^//////////////////////////////////////////////////////////////////////

                adapter = new PatientAdapter(getActivity(),R.layout.list_item,patients);
                final ListView patientListView = (ListView) rootView.findViewById(R.id.list);
                // Set the adapter onto the view pager
                patientListView.setAdapter(adapter);
                //patientListView.smoothScrollToPositionFromTop(newPatientPosition);
                patientListView.setSelection(newPatientPosition);

                if (buttonPress) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateView(newPatientPosition, patientListView);//INDICATOR COLOR CHANGE method
                        }
                    }, 10);
                }


            }
        }
    }


    //INDICATOR COLOR CHANGE: to set the change in color for the circle for an indicator of change v
    /**
     * Changes the color of the circle in the list view to indicate a change in the patient list
     * @param index the position of the changed patient in the list
     * @param patientListView the list view of the patient list
     */
    private void updateView(int index, ListView patientListView){
        adapter = new PatientAdapter(getActivity(),R.layout.list_item,patients);
        final View v = patientListView.getChildAt(index - patientListView.getFirstVisiblePosition());

        if(v == null)
            return;

        final TextView numberTextView = (TextView) v.findViewById(R.id.number);
        GradientDrawable magnitudeCircle = (GradientDrawable) numberTextView.getBackground();
        int magnitudeColor = R.color.green;
        magnitudeCircle.setColor(ContextCompat.getColor(v.getContext(),magnitudeColor));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GradientDrawable magnitudeCircle = (GradientDrawable) numberTextView.getBackground();
                int magnitudeColor = R.color.magnitude1;
                magnitudeCircle.setColor(ContextCompat.getColor(v.getContext(),magnitudeColor));
            }
        }, 2000);

    }
    //INDICATOR COLOR CHANGE: to set the change in color for the circle for an indicator of change ^



    //SQL METHODS v/////////////////////////////////////////////////////////////////////////////////
//    public void DeleteData(int mPatientCode){
//        Integer deletedRows = myDB.deleteData(Integer.toString(mPatientCode));
//        if (deletedRows>0){
//            Toast.makeText(getActivity(),"Data Deleted", Toast.LENGTH_SHORT).show();
//        }else {
//            Toast.makeText(getActivity(), "Data not Deleted", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void UpdateData(int mPatientCode, String mName, int mLineNumber, long mTime, long mAppointmentTime){
//        boolean isUpdate = myDB.updateData(Integer.toString(mPatientCode),
//                mName, Integer.toString(mLineNumber), Long.toString(mTime), Long.toString(mAppointmentTime));
//        if (isUpdate==true){
//            Toast.makeText(getActivity(),"Data Updated", Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(getActivity(),"Data not Updated", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void AddData(int mPatientCode, String mName, int mLineNumber, long mTime, long mAppointmentTime){
//
//        boolean isInserted = myDB.insertData(Integer.toString(mPatientCode),
//                mName, Integer.toString(mLineNumber), Long.toString(mTime), Long.toString(mAppointmentTime));
//
//        if (isInserted==true){
//            Toast.makeText(getActivity(),"Data Inserted", Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(getActivity(),"Data not Inserted", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public ArrayList<Patient> getAll(ArrayList<Patient> patientList){
//        Cursor res = myDB.getAllData();
//        if (res.getCount()==0){
//            //means no data is available
//            //error
//            Toast.makeText(getActivity(),"No Data", Toast.LENGTH_SHORT).show();
//            return new ArrayList<Patient>();
//        }else{
//
//            StringBuffer buffer = new StringBuffer();
//            //get all data one by one through res object
//            while(res.moveToNext()){
//                buffer.append("Code : "+res.getString(0)+"\n");
//                buffer.append("Name : " +res.getString(1)+"\n");
//                buffer.append("Line Number : "+res.getString(2)+"\n");
//                buffer.append("Time : "+res.getString(3)+"\n");
//                buffer.append("Appointment : "+res.getString(4)+"\n\n");
//
//                int code=Integer.parseInt(res.getString(0));
//                String name=res.getString(1);
//                int linenumber=Integer.parseInt(res.getString(2));
//                long time =Long.parseLong(res.getString(3));
//                long appointment = Long.parseLong(res.getString(4));
//
//                patientList.add(new Patient(code,name,linenumber,time,appointment));
//
//            }
//        }
//        return patientList;
//    }
    //SQL METHODS ^/////////////////////////////////////////////////////////////////////////////////




    //NETWORK CONNECTION v//////////////////////////////////////////////////////////////////////////
    private void myUpdateOperation(){
        NetworkConnect task = new NetworkConnect();
        task.execute(REQUEST_URL);
    }

    private void myDeleteOperation(String patientCode, String deleteTag) {
        NetworkConnect task = new NetworkConnect();
        task.execute(REQUEST_URL, patientCode, deleteTag);
    }

    //onCompletion is your server response with a success
    public void onCompletion() {

        if (mySwipeRefreshLayout.isRefreshing()) {
            mySwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Update the UI with the given Patient List information.
     */
    private void updateUi(final ArrayList <Patient> patientsUi) {
        patients= patientListSort(patientsUi);
        adapter = new PatientAdapter(getActivity(),R.layout.list_item, patients);
        // Find a reference to the {@link ListView} in the layout
        patientListView.setAdapter(adapter);
        onCompletion();
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
        protected ArrayList <Patient> doInBackground(String... urls) {

            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            // Perform the HTTP request for earthquake data and process the response.
            //Util methods are declared static which means you can reference them using the class name wihtout creating an instance of the Utils Object

            if(urls.length >= 3) {
                if (urls[2] == "delete" || urls[2] != null) {
                    QueryUtils.deletePatientData(urls[0], urls[1]);
                }
            }

            ArrayList <Patient> patient = QueryUtils.fetchPatientData(urls[0]);

            if(urls.length >= 3) {
                urls[2] = null;
            }

            return patient;
        }

        /**
         * This method is invoked on the main UI thread after the background work has been
         * completed.
         *
         * It IS okay to modify the UI within this method. We take the {@link ArrayList <Earthquake>} object
         * (which was returned from the doInBackground() method) and update the views on the screen.
         */
        @Override
        protected void onPostExecute(ArrayList <Patient> patient) {
            // If there is no result, do nothing.
            if (patient==null){
                return;
            }
            updateUi(patient);
        }
    }
    //NETWORK CONNECTION ^//////////////////////////////////////////////////////////////////////////


    /**
     * Sorts the patient list according to appointment time and renumber their line in the queue
     * @param patientList the list of patients
     * @return the sorted patient list
     */
    public ArrayList <Patient> patientListSort (ArrayList <Patient> patientList){
        Collections.sort(patientList);//Sort by appointment time - see comparable function in Patient class

        //Edit LineNumber to reflect new order
        for (int j = 0; j < patientList.size(); j++) {
            Patient lineNumberPatient = patientList.get(j);
            lineNumberPatient.setmLineNumber(j + 1);
            patientList.set(j, lineNumberPatient);
        }
        return patientList;
    }

}
