import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

//Created by Anshul Agrawal on 7/7/17 at 12:55 PM.

public class Home {


    public static ArrayList<Student> students = new ArrayList<Student>();
    public static ArrayList<Class> classes = new ArrayList<Class>();
    public static final int OBJECTS_PER_PAGE = 100;
    private static int pagesCount = 100;
    private static String baseURL = "https://api.veracross.com/nist/v2/";
    private static String studentsURL = baseURL + "students.xml?grade_level=12,13&page=";
    private static String classesURL = baseURL + "classes.xml?school_level=4&page=";
    private static String enrollmentsURL = baseURL + "enrollments.xml?class=";
    private static String studentEnrollmentsURL = baseURL + "enrollments.xml?student=";
    private static String studentsPath = "xmls/students-";
    private static String classesPath = "xmls/classes-";
    private static String enrollmentsPath = "xmls/enrollments-";
    private static String studentEnrollmentsPath = "xmls/studentEnrollments-";
    private static String secOfficeEmail = "18anshula@nist.ac.th";


    public static void main(String[] args) {
        homePage();
        ArrayList<Student> x = processEnrollments(103166, false);
        x.remove(0);
        x.remove(1);
        x.remove(3);

        Email.sendEmail(secOfficeEmail, "Test", x);
    }

    public static void homePage() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Start?");
//        String choice = scan.nextLine();
//        if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
//
//        } else {
//            System.exit(0);
//        }

        //This allows you to decide which sets of data to (re)Download when starting the program.
        //Options: "both", "students", or "classes".
        String toDownload = "none";

        //In case not even 1 of the appropriate files exists, override and download them all anyway.
        File check = new File(studentsPath + "1.xml");
        if (!check.exists()) {
            toDownload = "both";
        }
        check = new File(classesPath + "1.xml");
        if (!check.exists()) {
            toDownload = "both";
        }

        initialize(toDownload);
    }


    public static void initialize(String toDownload) {
        try {
            if (toDownload.equalsIgnoreCase("both") || toDownload.equalsIgnoreCase("students")) {
                //First need to delete all the student files that already exist.
                File folder = new File("xmls/");
                for (File f : folder.listFiles()) {
                    if (f.getName().startsWith("students")) {
                        f.delete();
                    }
                }
                download(studentsURL, studentsPath, "students"); //This will download the correct number of files, so that all the relevant objects are covered.
            }
            if (toDownload.equalsIgnoreCase("both") || toDownload.equalsIgnoreCase("classes")) {
                //Do the same thing again for classes
                //First need to delete all the classes files that already exist.
                File folder = new File("xmls/");
                for (File f : folder.listFiles()) {
                    if (f.getName().startsWith("classes")) {
                        f.delete();
                    }
                }
                download(classesURL, classesPath, "classes");
            }
            if (toDownload.equalsIgnoreCase("students")) {
                processClasses(1000);
            } else if (toDownload.equalsIgnoreCase("classes")) {
                processStudents(1000);
            }

            if (toDownload.equalsIgnoreCase("none")) {
                print("ALERT: Student and Class lists have not been updated. This could result in inaccuracies.");
                processStudents(1000);
                processClasses(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO: Decide whether to sort in methods or on demand.
        Collections.sort(classes);
    }

    private static int download(String URL, String path, String downloadType) {
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
            URL web = new URL(URL + 1);
            URLConnection connection = web.openConnection();
            int objectCount = Integer.parseInt(connection.getHeaderField("x-total-count")); //This returns a value like 218, which is then converted to an int.
            pagesCount = (objectCount / OBJECTS_PER_PAGE) + 1; //Now this will set the number of pages needed to be traversed, based on the fact that there are max 100 objects per page.
            // Now download using the above information.
            for (int i = 1; i <= pagesCount; i++) {
                URL website = new URL(URL + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(path + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                if (!downloadType.equalsIgnoreCase("Student Enrollments")) {
                    print("Downloaded " + downloadType + " File " + i + "/" + pagesCount + ".");
                }
            }
//            print(downloadType + " file(s) have been downloaded.");
            if (downloadType.equalsIgnoreCase("Students")) {
                processStudents(pagesCount);
            } else if (downloadType.equalsIgnoreCase("Classes")) {
                processClasses(pagesCount);
            }
        } catch (NumberFormatException e) { //This checks for Veracross Servers being down or not.
            print("NOTE: The program was not able to connect to the Veracross servers. Please check your Internet Connection. \n" +
                    "Otherwise, Veracross Servers may be down. The program may not work as intended.");
//            homePage();
        } catch (Exception e) {
            print("Downloading/Connection Error");
        }
        return pagesCount;
    }

    private static void processStudents(int pagesCount) {
        try {

            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (int x = 1; x <= pagesCount; x++) {
                Document document = builder.parse(new File(studentsPath + x + ".xml"));
//                Element root = document.getDocumentElement();
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
                        //Finally creates the new object and feeds it in. Prioritizes preferred name if available over first name (you're welcome, Thai kids)
                        students.add(new Student(pName, lName, id, email, homeroom, grade));
                    }
                }
            }
            Collections.sort(students);
        } catch (Exception e) {
            e.getCause();
        }
    }

    private static void processClasses(int pagesCount) {
        try {
            int count = 0;
            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder2 = factory2.newDocumentBuilder();
            for (int x = 1; x <= pagesCount; x++) {
                Document doc = builder2.parse(new File(classesPath + x + ".xml"));

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

                        //Subject - in order to include P1 Check In classes.
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
                        boolean homeroomAdvisory = (subject.equalsIgnoreCase("House Class") || subject.equalsIgnoreCase("Advisory"));
                        boolean y12_13 = (grade.equalsIgnoreCase("Year 12") || grade.equalsIgnoreCase("Year 13"));
                        boolean validTeacher = !(teacherName.equalsIgnoreCase(""));
                        //Finally add new Class object, if it meets above conditions.
                        if ((homeroomAdvisory || validType) && y12_13 && validTeacher) {
                            classes.add(new Class(name, id, stringID, grade, teacherName, type, meetingTimes));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.getCause();
        }
    }

    //Method to pull students out based on their Year Levels - Uses String instead of Integer as parameters to leave that for ID-based search.
    private static ArrayList<Student> searchStudents(String[] yearLevels) {
        //This method will take an int array of Year Levels to search for.
        //As a result, we now need to traverse the student ArrayList and check for matches with any items in the provided int array.
        ArrayList<Student> results = new ArrayList<Student>();
        for (Student i : students) {
            String gradeLevel = i.getGrade();
            for (String searching : yearLevels) {
                if (gradeLevel.equalsIgnoreCase(searching)) {
                    results.add(i);
                }
            }
        }
        return results;
    }


    private static ArrayList search(ArrayList<Integer> IDs, String searchType) {
        ArrayList results = new ArrayList();
        if (searchType.equalsIgnoreCase("students")) {
            results = new ArrayList<Student>();
            for (Student i : students) {
                int currentID = i.getId();
                for (int comparing : IDs) {
                    if (currentID == comparing) {
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

    //Method that just calls the method below, except repeats it for each ID in the arrayList given.
    private static ArrayList processEnrollments(ArrayList<Integer> IDList, boolean studentEnrollments) {
        ArrayList results = new ArrayList();
        for (Integer i : IDList) {
            results.addAll(processEnrollments(i, studentEnrollments));
        }
        //Only remove duplicates if you have a list of Students, not if you have a list of classes.
        if (!studentEnrollments) {
            Set noDuplicates = new LinkedHashSet(results);
            results.clear();
            results.addAll(noDuplicates);
        }
        Collections.sort(results);
        return results;
    }

    //Method to create the list of student IDs to expect for a certain class/student using the class/student ID(s).
    private static ArrayList processEnrollments(int ID, boolean studentEnrollments) {
        ArrayList<Integer> receivedIDs = new ArrayList<Integer>();
        ArrayList results;

        String name = ""; //We want to print the name of the Class/Student first.
        if (studentEnrollments) { //Search the student list to find out who the student is.
            for (Student x : students) {
                if (x.getId() == ID) {
                    name = x.getfName();
                }
            }
        } else { //Search the classes list to find out which class this is.
            for (Class x : classes) {
                if (x.getId() == ID) {
                    name = x.getName();
                }
            }
        }
        //Download enrollments next.
        String pathToUse, urlToUse, downloadType;

        //Decides which URL/Path to use based on studentEnrollments.
        if (studentEnrollments) {
            pathToUse = studentEnrollmentsPath + ID + "-";
            urlToUse = studentEnrollmentsURL + ID + "&page=";
            downloadType = "Student Enrollments";
        } else {
            pathToUse = enrollmentsPath + ID + "-";
            urlToUse = enrollmentsURL + ID + "&page=";
            downloadType = "Class Enrollments";
        }

        //First need to delete all the enrollment files that already exist, in order to avoid cluttering the directory.
        File folder = new File("xmls/");
        for (File f : folder.listFiles()) {
            if (f.getName().startsWith("enrollments") || f.getName().startsWith("studentEnrollments")) {
                f.delete();
            }
        }
        int enrollmentsPagesCount = download(urlToUse, pathToUse, downloadType);
//        print("Enrollments for " + name + ":");

        //Now to process the downloaded files.
        try {
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
                        if (studentEnrollments) {
                            newID = Integer.parseInt(eElement.getElementsByTagName("class_fk").item(0).getTextContent());
                        } else {
                            newID = Integer.parseInt(eElement.getElementsByTagName("student_fk").item(0).getTextContent());
                        }
                        //ID received, now add to the arrayList of receivedIDs.
                        receivedIDs.add(newID);
                    }
                }
            }
        } catch (FileNotFoundException e) {

        } catch (Exception e) {
                e.printStackTrace();
        }
        //Now we have an ArrayList of Integers that has the ID numbers of students/classes that we want to use.
        //We can use the search function that takes an integer array and returns the appropriate students/classes arrayList.
        if (studentEnrollments) { //If student enrollments, then the received IDs are those of classes. Match them to their IDs.
            results = search(receivedIDs, "classes");
        } else { //If not student enrollments, then the received IDs are student IDs, and we need to math those to student IDs.
            results = search(receivedIDs, "students");
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
            System.out.println(i);
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
    //This method will be used later to print out alerts for the user somehow.
    public static void print(Object x) {
        System.out.println(x);
    }
}
