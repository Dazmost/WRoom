package com.example.wroom;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Patient implements Parcelable,Comparable<Patient>{

    /**Patient Information**/
    private int mPatientCode;
    private String mName;
    private int mLineNumber;
    private long mTime;
    private long mAppointmentTime;

    /**Patient Constructor
     * @param patientCode - The random 4-digit identification code of the patient
     * @param name -  Patient name
     * @param lineNumber - The patient's position in the queue
     * @param time - The patient's check-in time
     * @param appointmentTime - The time of the patient's appointment
     * **/
    public Patient (int patientCode, String name, int lineNumber, long time, long appointmentTime){
        mPatientCode=patientCode;
        mName=name;
        mLineNumber=lineNumber;
        mTime=time;
        mAppointmentTime=appointmentTime;
    }

    /**
     *To allow your custom object to be parsed to another component they need to implement the android.os.Parcelable interface.
     * It must also provide a static final method called CREATOR which must implement the Parcelable.Creator interface.
     *
     * e.g.
     * Intent intentPatientModify = new Intent(getActivity(),ModifyPatientActivity.class);
     * intentPatientModify.putExtra("patientList", patients);
     * ...Other Activity Class...
     * patientList = intent.getParcelableArrayListExtra("patientList");
     *
     * In simple terms Parcelable is used to send a whole object of a model class to another page.
     * In your code this is in the model and it is storing int value size to Parcelable object to send and retrieve in other activity.
     * To add on in layman terms: Use Parcelable to convert an object into a sequence of bytes which can be read. transferred between Activities.
     *
     * @param in the Patient MyParcelable
     */
    protected Patient(Parcel in) {
        mPatientCode = in.readInt();
        mName = in.readString();
        mLineNumber = in.readInt();
        mTime = in.readLong();
        mAppointmentTime = in.readLong();
    }

    /**
     * Static final method called CREATOR which must implement the Parcelable.Creator interface.
     */
    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };


    /**
     * patientCode getter
     * @return patientCode
     */
    public int getmPatientCode() {
        return mPatientCode;
    }

    /**
     * name getter
     * @return name
     */
    public String getmName() {
        return mName;
    }

    /**
     * lineNumber getter
     * @return lineNumber
     */
    public int getmLineNumber() {
        return mLineNumber;
    }

    /**
     * time getter
     * @return time
     */
    public long getmTime() {
        return mTime;
    }

    /**
     * appointmentTime getter
     * @return appointmentTime
     */
    public long getmAppointmentTime() {
        return mAppointmentTime;
    }

    /**
     * appointmentTime setter
     */
    public void setmAppointmentTime(long appointmentTime){
        mAppointmentTime=appointmentTime;
    }

    /**
     * lineNumber setter
     */
    public void setmLineNumber(int lineNumber){
        mLineNumber=lineNumber;
    }

    /**
     * Other Parcelable methods
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPatientCode);
        dest.writeString(mName);
        dest.writeInt(mLineNumber);
        dest.writeLong(mTime);
        dest.writeLong(mAppointmentTime);
    }

    /**
     * Comparable<Patient>
     * Compares this object with the specified object for order.  Returns a
     *      * negative integer, zero, or a positive integer as this object is less
     *      * than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Patient o) {
        //sorting in ascending order
        return Long.compare(mAppointmentTime,o.mAppointmentTime);
    }

    @NonNull
    @Override
    public String toString() {
        return "Name: " + mName + " Appointment: " + mAppointmentTime;
    }
}

