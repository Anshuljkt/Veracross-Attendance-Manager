# NIST Attendance Manager
This program uses the Veracross V2 API to manage class attendance.
The user can search for and select the expected classes, after which the program will download and display the names of students in the selected classes.
Students can sign in with their ID numbers or School ID cards, and late attendance is marked by the program.
At class time, an email is sent to the secondary office with a list of all missing students.
The names and timestamps of late students are sent in 10-minute intervals to the secondary office.

Created by Anshul Agrawal in 2017


Requires Java 8.

# Instructions to compile/modify from source
1. Clone the entire directory (or download as a .zip file), and then use IntelliJ to import the project.
2. run the main method within *src/MainPage.java*. 

# Instructions to run the latest compiled version
1. Run the .jar file at *out/artifacts/VeracrossAttendanceManager/NISTAttendance.jar*
