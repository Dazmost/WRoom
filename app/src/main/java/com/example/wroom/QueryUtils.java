package com.example.wroom;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Patient} objects that has been built up from
     * parsing a JSON response.
     */
//    Convert SAMPLE_JSON_RESPONSE String into a JSONObject
//    Extract “features” JSONArray
//    Loop through each feature in the array
//    Get earthquake JSONObject at position i
//    Get “properties” JSONObject
//    Extract “mag” for magnitude
//    Extract “place” for location
//    Extract “time” for time
//    Create Earthquake java object from magnitude, location, and time
//    Add earthquake to list of earthquakes
    public static ArrayList<Patient> extractPatients(String jsonResponse) {

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Patient> patients = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // TODO: Parse the response given by the SAMPLE_JSON_RESPONSE string and
            // build up a list of Patient objects with the corresponding data.
            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonRootArray = new JSONArray(jsonResponse);

            //Iterate the jsonArray and print the info of JSONObjects
            for(int i=0;i<jsonRootArray.length();i++){
                JSONObject jsonObjectIndex = jsonRootArray.getJSONObject(i);

                int patientCode = jsonObjectIndex.getInt("patientCode");
                String name = jsonObjectIndex.getString("name");
                int lineNumber = jsonObjectIndex.getInt("lineNumber");
                long time = jsonObjectIndex.getLong("time");
                long appointmentTime = jsonObjectIndex.getLong("appointmentTime");


                patients.add(new Patient(patientCode, name, lineNumber, time, appointmentTime));

            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the patient JSON results", e);
        }

        // Return the list of earthquakes
        return patients;
    }

    //USED DIDYOUFEELIT APP AS REFERENCE

    /**
     * Query the dataset and return an {@link Patient} object to represent an array of patients
     */
    public static ArrayList<Patient> fetchPatientData(String requestURL) {//////////////////MAIN FUNCTION OF CLASS//////////////////////////
        URL url = createUrl(requestURL);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        ArrayList<Patient> patients = extractPatients(jsonResponse);

        // Return the {@link Event}
        return patients;
    }

    /**
     * POST
     * Query the dataset and return an {@link Patient} object to represent an array of patients
     */
    public static String postPatientData(String requestURL,String patientCode, String name, String lineNumber, String time,
                                         String appointmentTime ) {//////////////////MAIN FUNCTION OF CLASS//////////////////////////
        URL url = createUrl(requestURL);

        String jsonResponse = null;
        try {
            jsonResponse = postHttpRequest(url,patientCode,name,lineNumber,time,appointmentTime);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        //Patient patient = extractPatients(jsonResponse);

        // Return the {@link Event}
        return jsonResponse;
    }

    /**
     * PUT
     * Query the dataset and return an {@link Patient} object to represent an array of patients
     */
    public static String putPatientData(String requestURL,String patientCode, String appointmentTime ) {//////////////////MAIN FUNCTION OF CLASS//////////////////////////
        URL url = createUrl(requestURL+"/"+patientCode);

        Log.d(LOG_TAG, "URL request:"+ requestURL+"/"+patientCode);

        String jsonResponse = null;
        try {
            jsonResponse = putHttpRequest(url,patientCode, appointmentTime);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        //Patient patient = extractPatients(jsonResponse);

        // Return the {@link Event}
        return jsonResponse;
    }

    /**
     * DELETE
     * Query the dataset and return an {@link Patient} object to represent an array of patients
     */
    public static String deletePatientData(String requestURL,String patientCode) {//////////////////MAIN FUNCTION OF CLASS//////////////////////////
        URL url = createUrl(requestURL+"/"+patientCode);

        Log.d(LOG_TAG, "URL request:"+ requestURL+"/"+patientCode);

        String jsonResponse = null;
        try {
            jsonResponse = deleteHttpRequest(url,patientCode);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        //Patient patient = extractPatients(jsonResponse);

        // Return the {@link Event}
        return jsonResponse;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * POST
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String postHttpRequest(URL url, String patientCode, String name, String lineNumber, String time,
                                          String appointmentTime) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setReadTimeout(10000 /* milliseconds */);
            //urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            //urlConnection.connect();

//            String jsonInputString = "{\"patientCode\":\"2176\",\"name\":\"testingBOBpatient1\",\"lineNumber\":\"1\",\"time\":\"1581116201937\",\"appointmentTime\":\"1581222660000\"}";
//
//            try(OutputStream os = urlConnection.getOutputStream()) {
//                byte[] input = jsonInputString.getBytes("UTF-8");
//                os.write(input);
//            }

            JSONObject cred   = new JSONObject();
            try {
                cred.put("patientCode", patientCode);
                cred.put("name",  name);
                cred.put("lineNumber", lineNumber);
                cred.put("time", time);
                cred.put("appointmentTime",appointmentTime);
            }catch (JSONException e){
                Log.e(LOG_TAG, "Cannot create JSON object");
            }

            OutputStream os = urlConnection.getOutputStream();
            os.write(cred.toString().getBytes("UTF-8"));
            os.close();

//            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
//            wr.write(cred.toString());
//            wr.flush();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * PUT
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String putHttpRequest(URL url, String patientCode, String appointmentTime) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setReadTimeout(10000 /* milliseconds */);
            //urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");

            JSONObject cred   = new JSONObject();
            try {
                //cred.put("patientCode", patientCode);
                cred.put("appointmentTime",appointmentTime);
            }catch (JSONException e){
                Log.e(LOG_TAG, "Cannot create JSON object");
            }

            OutputStream os = urlConnection.getOutputStream();
            os.write(cred.toString().getBytes("UTF-8"));
            os.close();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * DELETE
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String deleteHttpRequest(URL url, String patientCode) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setReadTimeout(10000 /* milliseconds */);
            //urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("DELETE");
            //urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            //urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.connect();

//            JSONObject cred   = new JSONObject();
//            try {
//                //cred.put("patientCode", patientCode);
//                cred.put("appointmentTime",appointmentTime);
//            }catch (JSONException e){
//                Log.e(LOG_TAG, "Cannot create JSON object");
//            }

//            OutputStream os = urlConnection.getOutputStream();
//            os.write(cred.toString().getBytes("UTF-8"));
//            os.close();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
