Android Studio > Settings > System Settings > Android SDK > API Level 29 > Update


You've already gone down the list of most things that would be helpful, but you could try:

Exit Android Studio
Back up your project
Delete all the .iml files and the .idea folder
Relaunch Android Studio and reimport your project
By the way, the error messages you see in the Project Structure dialog are bogus for the most part.

UPDATE:

Android Studio 0.4.3 is available in the canary update channel, and should hopefully solve most of these issues. 
There may be some lingering problems; if you see them in 0.4.3, let us know, and try to give us a reliable set of 
steps to reproduce so we can ensure we've taken care of all code paths.


REMEBER TO SET DEVELOPER MODE ON ANDROID PHONE
TAP BUILD NUMBER 5 TIMES
SET DEBUGGING MODE ON


Patient Fragment, ModifyPatientActivity and Patient Activity
    /** URL for NodeJs Sever */
    private static final String REQUEST_URL ="http://192.---.-.--:8000/api/patients";

find ip address with ipconfig in command prompt