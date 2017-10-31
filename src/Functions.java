import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Functions {


    public static final int OBJECTS_PER_PAGE = 100;
    public static ArrayList<Student> students = new ArrayList<Student>();
    public static ArrayList<Class> classes = new ArrayList<Class>();
    public static String programDataDir = "";
    public static String secOfficeEmail = "secoffice@nist.ac.th";
    private static int pagesCount = 200;
    private static String baseURL = "https://api.veracross.com/nist/v2/";
    private static String studentsURL = baseURL + "students.xml?grade_level=12,13&page=";
    private static String classesURL = baseURL + "classes.xml?school_level=4&page=";
    private static String enrollmentsURL = baseURL + "enrollments.xml?class=";
    private static String studentEnrollmentsURL = baseURL + "enrollments.xml?student=";
    private static String studentsPath = "/students-";
    private static String classesPath = "/classes-";
    private static String enrollmentsPath = "/enrollments-";
    private static String studentEnrollmentsPath = "/studentEnrollments-";
    private static Exception downloadError = new Exception();

    public static void saveTime() { //This is to remember how long it has been since the database was fully updated.
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String currentTime = dateFormat.format(date); //Now this should be the current Date/Time

            PrintWriter writer = new PrintWriter(programDataDir + "/time.txt", "UTF-8");
            writer.println(currentTime);

            writer.close();

        } catch (Exception e) {

        }
    }

    public static String readTime() { //This will just give us the time the database was last updated.
        Scanner scan = null;
        try {
            scan = new Scanner(new FileInputStream(programDataDir + "/time.txt"));
            String time = scan.nextLine();
            return time;
        } catch (FileNotFoundException e) {
            return "Never";
        }
    }

    //This method just downloads and processes students and classes before the program is usable.
    //It returns a String so that errors can be checked for.
    public static String initialize(boolean updateDatabase) {

        //In case the database does not exist (or was not fully updated last time), override and download everything anyway.
        if (readTime().equalsIgnoreCase("Never")) {
            updateDatabase = true;
            System.out.println("No database found. Overriding.");
        }

        try {
            if (updateDatabase) {
                //First need to delete all the database files that already exist.
                //If you are trying to update the database, also delete the old timestamp file, so that it is only created on successful database downloads.
                //This way, if something gets corrupted on download, the program will re-download on the next run.
                File folder = new File(programDataDir);
                for (File f : folder.listFiles()) {
                    if (f.getName().startsWith("student")
                            || f.getName().startsWith("classes")
                            || f.getName().startsWith("enrollments")
                            || f.getName().startsWith("time")
                            ) {
                        f.delete();
                    }
                }
                int studentDownloadResponse = download(studentsURL, programDataDir + studentsPath, "students"); //This will download the correct number of files, so that all the relevant objects are covered.
                if (studentDownloadResponse < 0) {
                    return "downloadError";
                }
                int classesDownloadResponse = download(classesURL, programDataDir + classesPath, "classes"); //Download classes too.
                if (classesDownloadResponse < 0) {
                    return "downloadError";
                }
                saveTime();
            } else {
                System.out.println("ALERT: The local database has not been updated since: " + readTime() + ". This could result in inaccuracies.");
                processClasses(pagesCount);
                processStudents(pagesCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "downloadError";
        }
        return "success";
    }

    private static int download(String URL, String path, String downloadType) throws Exception {
        //This is where the username and password go for accessing Veracross API.
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("api.nist", "X4bZsxMVBr".toCharArray());
            }
        });

        downloadType = downloadType.substring(0, 1).toUpperCase() + downloadType.substring(1);

        //Checks object count and sets pagesCount accordingly.
        try {
            //First connects to the Veracross API and view page 1 of the URL, and collect the total number of objects from the XML response header.
            URL initialURL = new URL(URL + 1);
            URLConnection connection = initialURL.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int objectCount = Integer.parseInt(connection.getHeaderField("x-total-count")); //This returns a value like 218, which is then converted to an int.
            pagesCount = (objectCount / OBJECTS_PER_PAGE) + 1; //Now this will set the number of pages needed to be traversed, based on the fact that there are max 100 objects per page.
            // Now download using the above information.
            for (int i = 1; i <= pagesCount; i++) {
                URL specificURL = new URL(URL + i);
                //This is done to make sure that the program recognizes when the internet connection is cut while downloading.
                URLConnection specificConnection = specificURL.openConnection();
                specificConnection.setReadTimeout(10000);
                specificConnection.setConnectTimeout(10000);

                ReadableByteChannel rbc = Channels.newChannel(specificConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(path + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                if (!downloadType.equalsIgnoreCase("Student Enrollments") && !downloadType.equalsIgnoreCase("Class Enrollments")) {
                    System.out.println("Downloaded " + downloadType + " File " + i + "/" + pagesCount + ".");
                }
            }

            if (downloadType.equalsIgnoreCase("Students")) {
                processStudents(pagesCount);
            } else if (downloadType.equalsIgnoreCase("Classes")) {
                processClasses(pagesCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw downloadError;
        }
        return pagesCount;
    }

    private static void processStudents(int pagesCount) {
        try {

            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (int x = 1; x <= pagesCount; x++) {
                Document document = builder.parse(new File(programDataDir + studentsPath + x + ".xml"));

                //Create a temporary list that creates a node for each student object in the xml file received.
                NodeList tempList = document.getElementsByTagName("student");
                //for loop that feeds the relevant data into the students ArrayList as new objects.
                for (int i = 0; i < tempList.getLength(); i++) {
                    Node current = tempList.item(i);
                    if (current.getNodeName().equalsIgnoreCase("student")) {
                        Element eElement = (Element) current;
                        int id = Integer.parseInt(eElement.getElementsByTagName("person_pk").item(0).getTextContent());
                        //First Name
                        String fName = eElement.getElementsByTagName("first_name").item(0).getTextContent();
                        //Preferred Name (Some students don't have one)
                        String pName = eElement.getElementsByTagName("preferred_name").item(0).getTextContent();
                        //Handles missing pNames, and replaces the empty pName for that student with fName
                        if (pName.equals("")) {
                            pName = fName;
                        }
                        //Last Name
                        String lName = eElement.getElementsByTagName("last_name").item(0).getTextContent();
                        //Email
                        String email = eElement.getElementsByTagName("email_1").item(0).getTextContent();
                        //Homeroom Class ID
                        int homeroom = Integer.parseInt(eElement.getElementsByTagName("homeroom").item(0).getTextContent());
                        //Year Level
                        String grade = eElement.getElementsByTagName("current_grade").item(0).getTextContent();
                        //Finally creates the new object and feeds it in. Prioritizes preferred name if available over first name.
                        if (!email.equalsIgnoreCase("")) { //If the person has no E-Mail ID, then don't add them.
                            students.add(new Student(pName, lName, id, email, homeroom, grade));
                        }
                    }
                }
            }
            Collections.sort(students);
        } catch (FileNotFoundException e) {
            System.out.println("Successfully processed database in offline mode.");
        } catch (Exception e) {

        }
    }

    private static void processClasses(int pagesCount) {
        try {
            int count = 0;
            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder2 = factory2.newDocumentBuilder();
            for (int x = 1; x <= pagesCount; x++) {
                Document doc = builder2.parse(new File(programDataDir + classesPath + x + ".xml"));

                //Create a NodeList for the class objects in the xml file
                NodeList classList = doc.getElementsByTagName("class");
                //for loop to feed into ArrayList
                for (int i = 0; i < classList.getLength(); i++) {
                    Node current = classList.item(i);
                    if (current.getNodeName().equalsIgnoreCase("class")) {
                        Element element = (Element) current;
                        count++;
//                        System.out.println(count);

                        //Class Name
                        String name = element.getElementsByTagName("description").item(0).getTextContent();

                        //Integer ID
                        int id = Integer.parseInt(element.getElementsByTagName("class_pk").item(0).getTextContent());

                        //Year Level
                        String grade = element.getElementsByTagName("primary_grade_level").item(0).getTextContent();

                        //stringID
                        String stringID = element.getElementsByTagName("class_id").item(0).getTextContent();

                        //Teacher Name
                        String teacherName = element.getElementsByTagName("teacher_full_name").item(0).getTextContent();

                        //Course Type
                        String type = element.getElementsByTagName("course_type").item(0).getTextContent();

                        //Subject - in order to include P1 Check In classes, CAS, and Homeroom.
                        String subject = element.getElementsByTagName("subject").item(0).getTextContent();

                        //Meeting Times
                        ArrayList<MeetingTime> meetingTimes = new ArrayList<MeetingTime>();
                        NodeList meetTimes = doc.getElementsByTagName("meeting_time");
                        for (int z = 0; z < meetTimes.getLength(); z++) {
                            current = meetTimes.item(z);
                            element = (Element) current;
                            String day = element.getElementsByTagName("day").item(0).getTextContent();
                            String block = element.getElementsByTagName("block").item(0).getTextContent();
                            String block_abbreviation = element.getElementsByTagName("block_abbreviation").item(0).getTextContent();
                            meetingTimes.add(new MeetingTime(day, block, block_abbreviation));
                        }

                        //Conditions for a valid class.
                        boolean validType = ((type.equalsIgnoreCase("Academic")) || (type.equalsIgnoreCase("Homeroom")));
                        boolean homeroomAdvisory = (subject.equalsIgnoreCase("House Class") || subject.equalsIgnoreCase("Advisory") || subject.equalsIgnoreCase("Community and Service"));
                        boolean y12_13 = (grade.equalsIgnoreCase("Year 12") || grade.equalsIgnoreCase("Year 13"));
                        boolean validTeacher = !(teacherName.equalsIgnoreCase(""));
                        //Finally add new Class object, if it meets above conditions.
                        if ((homeroomAdvisory || validType) && y12_13 && validTeacher) {
                            classes.add(new Class(name, id, stringID, grade, teacherName, type, meetingTimes));
                        }
                    }
                }
            }
            Collections.sort(classes);
        } catch (Exception e) {
            e.getCause();
        }
    }

    //Method to pull students out based on their Year Levels - Uses String instead of Integer as parameter to leave that for ID-based search.
    public static ArrayList<Student> searchStudents(String yearLevel) {
        //This method will take a String Year level to search for.
        ArrayList<Student> results = new ArrayList<Student>();
        for (Student i : students) {
            if (i.getGrade().equalsIgnoreCase(yearLevel)) {
                i.setCurrentClass(yearLevel);
                results.add(i);
            }
        }
        Collections.sort(results);
        return results;
    }

    private static ArrayList search(ArrayList<Integer> IDs, String searchType, String className) {
        ArrayList results = new ArrayList();
        if (searchType.equalsIgnoreCase("students")) {
            results = new ArrayList<Student>();
            for (Student i : students) {
                int currentID = i.getId();
                for (int comparing : IDs) {
                    if (currentID == comparing) {
                        i.setCurrentClass(className);
                        results.add(i);
                    }
                }
            }
        } else if (searchType.equalsIgnoreCase("classes")) {
            results = new ArrayList<Class>();
            for (Class i : classes) {
                int currentID = i.getId();
                for (int comparing : IDs) {
                    if (currentID == comparing) {
                        results.add(i);
                    }
                }
            }
        }
        return results;
    }

    public static ArrayList<Class> searchClasses(String searchTerm) {
        ArrayList<Class> results = new ArrayList<Class>();
        for (Class x : classes) {
            if ((x.getName().toLowerCase().contains(searchTerm.toLowerCase())) || (x.getTeacherName().toLowerCase().contains(searchTerm.toLowerCase())) || (x.getstringID().toLowerCase().contains(searchTerm.toLowerCase()))) {
                results.add(x);
            }
        }
        return results;
    }

    public static String searchClassName(int search) {
        for (Class x : classes) {
            if (x.getId()==search) {
                return x.getName() + " - " + x.getTeacherName();
            }
        }
        return "Error";
    }

    //Method that just calls the method below, except repeats it for each ID in the arrayList given.
    public static ArrayList processEnrollments(ArrayList<Integer> IDList, boolean reverseEnrollments, int numberOfClasses) throws Exception {
        try {
            ArrayList results = new ArrayList();
            for (int i = 1; i <= numberOfClasses; i++) {
                String nameOfClass = searchClassName(IDList.get(i - 1));
                results.addAll(processEnrollments(IDList.get(i - 1), false, nameOfClass));
                System.out.println("Downloaded Enrollments File " + i + "/" + numberOfClasses + ".");
            }


            //Only remove duplicates if you have a list of Students, not if you have a list of classes.
            if (!reverseEnrollments) {
                Set noDuplicates = new LinkedHashSet(results);
                results.clear();
                results.addAll(noDuplicates);
            }
            Collections.sort(results);
            return results;
        } catch (Exception e) {
            throw downloadError;
        }
    }

    //Method to create the list of student IDs to expect for a certain class/student using the class/student ID(s).
    public static ArrayList processEnrollments(int ID, boolean reverseEnrollments, String nameOfClass) throws Exception {
        ArrayList<Integer> receivedIDs = new ArrayList<Integer>();
        ArrayList results;

        //Download enrollments next.
        String pathToUse, urlToUse, downloadType;

        //Decides which URL/Path to use based on reverseEnrollments.
        if (reverseEnrollments) {
            pathToUse = programDataDir + studentEnrollmentsPath + ID + "-";
            urlToUse = studentEnrollmentsURL + ID + "&page=";
            downloadType = "Student Enrollments";
        } else {
            pathToUse = programDataDir + enrollmentsPath + ID + "-";
            urlToUse = enrollmentsURL + ID + "&page=";
            downloadType = "Class Enrollments";
        }

        //First need to delete all the enrollment files that already exist, in order to avoid cluttering the directory.
        File folder = new File(programDataDir);
        for (File f : folder.listFiles()) {
            if (f.getName().startsWith("enrollments") || f.getName().startsWith("reverseEnrollments")) {
                f.delete();
            }
        }
        try {
        int enrollmentsPagesCount = download(urlToUse, pathToUse, downloadType);
//        System.out.println("Enrollments for " + name + ":");

        //Now to process the downloaded files.

            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (int x = 1; x <= enrollmentsPagesCount; x++) {
                Document document = builder.parse(new File(pathToUse + x + ".xml"));
                Element root = document.getDocumentElement();

                //Create a temporary list that creates a node for each enrollment object in the xml file received.
                NodeList tempList = document.getElementsByTagName("enrollment");
                //for loop that feeds the relevant data into the students ArrayList as new objects.
                for (int z = 0; z < tempList.getLength(); z++) {
                    Node current = tempList.item(z);
                    if (current.getNodeName().equalsIgnoreCase("enrollment")) {
                        Element eElement = (Element) current;
                        int newID = -1;
                        String currentClass;
                        if (reverseEnrollments) {
                            newID = Integer.parseInt(eElement.getElementsByTagName("class_fk").item(0).getTextContent());
                        } else {
                            newID = Integer.parseInt(eElement.getElementsByTagName("student_fk").item(0).getTextContent());
                        }
                        //ID received, now add to the arrayList of receivedIDs.
                        receivedIDs.add(newID);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw downloadError;
        }
        //Now we have an ArrayList of Integers that has the ID numbers of students/classes that we want to use.
        //We can use the search function that takes an integer array and returns the appropriate students/classes arrayList.
        if (reverseEnrollments) { //If reverse enrollments, then the received IDs are those of classes. Match them to their IDs.
            results = search(receivedIDs, "classes", null);
            printArrayList(results);
        } else { //If not reverse enrollments, then the received IDs are student IDs, and we need to match those to student IDs.
            results = search(receivedIDs, "students", nameOfClass); //Also want to store the current class name of each student.
        }
        Collections.sort(results);
        return results;
    }

    public static void printClasses() {
        for (Class i : classes) {
            System.out.println(i);
        }
    }

    public static void printStudents(int[] yearLevel) {
        for (Student i : students) {
            System.out.println(i.toStringWithID());
        }
    }

    public static void printStudents() {
        for (Student i : students) {
            System.out.println(i);
        }
    }

    public static void printArrayList(ArrayList x) {
        for (Object i : x) {
            System.out.println(i);
        }
    }
}
