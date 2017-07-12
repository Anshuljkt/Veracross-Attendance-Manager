import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

 //Created by Anshul Agrawal on 7/7/17 at 12:55 PM.

public class Home {
    public static ArrayList<Student> students = new ArrayList<Student>();
    public static ArrayList<Class> classes = new ArrayList<Class>();
    private static int studentPagesCount = 100;
    private static int classesPagesCount = 100;
    private static int enrollmentsPagesCount = 100;
    private static int studentEnrollmentsPagesCount = 100;
    private static String studentsURL = "https://api.veracross.com/nist/v2/students.xml?grade_level=12,13&page=";
    private static String classesURL = "https://api.veracross.com/nist/v2/classes.xml?school_level=4&page=";
    private static String enrollmentsURL = "https://api.veracross.com/nist/v2/enrollments.xml?class=";
    private static String studentEnrollments = "https://api.veracross.com/nist/v2/enrollments.xml?student=";

    public static void main(String[] args) {

        homePage();

    }

    public static void printClasses() {
        for (Class i : classes) {
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

    public static void homePage() {
        Scanner scan = new Scanner(System.in);
//        System.out.println("Start?");
//        String choice = scan.nextLine();
//        if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
//
//        } else {
//            System.exit(0);
//        }
        String toDownload = "none";

        //In case no appropriate files exist, override and download them all anyway.
        File check = new File("xmls/students1.xml");
        if (!check.exists()) {
            toDownload = "both";
        }
        initialize(toDownload); //This allows you to decide which sets of data to (re)Download.

        ArrayList<Integer> searchID = new ArrayList<Integer>();
        searchID.add(25162);
//        searchID.add(3914);
//        searchID.add(103076);
//        searchID.add(103077);

        ArrayList results = processEnrollments(searchID, true);
        printArrayList(results);
    }


    public static void initialize(String toDownload) {

        //This is where the username and password go for accessing Veracross API.
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("api.nist", "X4bZsxMVBr".toCharArray());
            }
        });

        //Checks object count and sets studentPagesCount and classesPagesCount accordingly.
        try {
            if (toDownload.equalsIgnoreCase("both") || toDownload.equalsIgnoreCase("students")) {
                //First need to delete all the student files that already exist.
                File folder = new File("xmls/");
                for (File f : folder.listFiles()) {
                    if (f.getName().startsWith("students")) {
                        f.delete();
                    }
                }
                //First connects to the Veracross API and view page 1 of students, and collect the total number of objects from the XML response header.
                URL web = new URL(studentsURL + 1);
                URLConnection connection = web.openConnection();
                int studentObjectCount = Integer.parseInt(connection.getHeaderField("x-total-count")); //This returns a value like 218, which is then converted to an int.
                studentPagesCount = (studentObjectCount / 100) + 1; //Now this will set the number of pages needed to be traversed, based on the fact that there are max 100 objects per page.
                downloadStudents(); //This will download the correct number of files, so that all the Y12-13 students are covered.
                print("Student files have been downloaded.");
            }
            if (toDownload.equalsIgnoreCase("both") || toDownload.equalsIgnoreCase("classes")) {
                //First need to delete all the classes files that already exist.
                File folder = new File("xmls/");
                for (File f : folder.listFiles()) {
                    if (f.getName().startsWith("classes")) {
                        f.delete();
                    }
                }
                //Do the same thing again for classes:
                URL web2 = new URL(classesURL + 1);
                URLConnection connection2 = web2.openConnection();
                int classesObjectCount = Integer.parseInt(connection2.getHeaderField("x-total-count"));
                classesPagesCount = (classesObjectCount / 100) + 1;
                downloadClasses();
                print("Classes files have been downloaded.");
            }
            if (toDownload.equalsIgnoreCase("none")) {
                print("ALERT: Student and Class lists have not been downloaded. May result in inaccuracies.");
            }
        } catch (NumberFormatException e) { //This checks for Veracross Servers being down or not.
            print("NOTE: The program was not able to connect to the Veracross servers. Please check your Internet Connection. \n" +
                    "Otherwise, Veracross Servers may be down. The program may not work as intended.");
//            homePage();
        } catch (Exception e) {
            e.printStackTrace();
        }


        processStudentsList(); //Each file will now be opened and processed into the Students arrayList.
        processClasses();
        //TODO: Decide whether to sort in methods or on demand.
        Collections.sort(classes);
    }

    private static void downloadStudents() {
        try {
            for (int i = 1; i <= studentPagesCount; i++) {
                URL website = new URL(studentsURL + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("xmls/students" + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                print("Downloaded Students File " + i + "/" + studentPagesCount + ".");

            }
        } catch (Exception e) {
            print("URL Downloading Error");
        }
    }

    private static void processStudentsList() {
        try {

            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (int x = 1; x <= studentPagesCount; x++) {
                Document document = builder.parse(new File("xmls/students" + x + ".xml"));
                Element root = document.getDocumentElement();
                //System.out.println(root.getNodeName());


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
                        //Finally creates the new object and feeds it in. Uses pName, not fName.
                        students.add(new Student(pName, lName, id, email, homeroom, grade));
                    }
                    //                System.out.println(students.get(i));
                }
            }
        } catch (Exception e) {
            e.getCause();
        }
    }

    private static void downloadClasses() {
        try {
            for (int i = 1; i <= classesPagesCount; i++) {
                URL website = new URL(classesURL + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("xmls/classes" + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                print("Downloaded Classes File " + i + "/" + classesPagesCount + ".");

            }
        } catch (Exception e) {
            print("URL Downloading Error");
        }
    }

    private static void processClasses() {
        try {
            int count = 0;
            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder2 = factory2.newDocumentBuilder();
            for (int x = 1; x <= classesPagesCount; x++) {
                Document doc = builder2.parse(new File("xmls/classes" + x + ".xml"));
//                Element root2 = doc.getDocumentElement();
//                System.out.println(root2.getNodeName());

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

    //Method to pull students out based on their ID numbers.
    private static ArrayList<Student> searchStudents(ArrayList<Integer> IDs) {
        ArrayList<Student> results = new ArrayList<Student>();
        for (Student i : students) {
            int currentID = i.getId();
            for (int comparing : IDs) {
                if (currentID == comparing) {
                    results.add(i);
                }
            }
        }
        return results;
    }

    private static ArrayList<Class> searchClasses(ArrayList<Integer> IDs) {
        ArrayList<Class> results = new ArrayList<Class>();
        for (Class i : classes) {
            int currentID = i.getId();
            for (int comparing : IDs) {
                if (currentID == comparing) {
                    results.add(i);
                }
            }
        }
        return results;
    }

    //Enrollment Downloader. Downloads 1 xml file for each received int. Can only do 1 class per call.
    private static void downloadEnrollments(int ID, String path) {
//        //First need to delete all the enrollment files that already exist.
//        File folder = new File("xmls/");
//        for (File f: folder.listFiles()) {
//            if (f.getName().startsWith("enrollments")) {
//                f.delete();
//            }
//        }
        try {
            //First set URL to be used this time. It needs to be based on enrollmentsURL, and replaced for every method call.
            String classURL = enrollmentsURL + ID + "&page=";
            //First connects to the Veracross API and view page 1 of this enrollments.xml, and collects the total number of objects from the XML response header.
            URL web = new URL(classURL + 1);
            URLConnection connection = web.openConnection();
            int enrollmentsObjectCount = Integer.parseInt(connection.getHeaderField("x-total-count")); //This returns a value like 218, which is then converted to an int.
            enrollmentsPagesCount = (enrollmentsObjectCount / 100) + 1; //Now this will set the number of pages needed to be traversed, based on the fact that there are max 100 objects per page.
            //Now, we should have the pages of enrollments for the classes selected.
            for (int i = 1; i <= enrollmentsPagesCount; i++) {
                URL website = new URL(classURL + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(path + ID + "-" + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            }
        } catch (Exception e) {
            print("URL Downloading Error");
        }
    }

    //Method to create the list of student IDs to expect for a certain class/student using the class/student IDs.
    private static ArrayList processEnrollments(ArrayList<Integer> IDs, boolean studentEnrollments) {
        ArrayList<Integer> receivedIDs = new ArrayList<Integer>();

        ArrayList results;

        if (studentEnrollments) {
            results = new ArrayList<Class>();
            enrollmentsURL = "https://api.veracross.com/nist/v2/enrollments.xml?student=";
        } else {
            results = new ArrayList<Student>();
        }

//        ArrayList<Student> results = new ArrayList<Student>();

        int count = 0;
        //Need to repeat one time for each ID there is.
        for (Integer i : IDs) {
            String name = ""; //We want to print the name of the Class/Student.
            if (studentEnrollments) {
                for (Student x : students) {
                    if (x.getId() == i) {
                        name = x.getfName();
                    }
                }
            } else {
                for (Class x : classes) {
                    if (x.getId() == i) {
                        name = x.getName();
                    }
                }
            }
            //Download first.
            String path = "xmls/enrollments-";

            if (studentEnrollments) {
                path = "xmls/studentEnrollments-";
            }

            downloadEnrollments(i, path);
            count++;
            print("Downloaded Enrollments " + count + "/" + IDs.size() + ".");
//            print("Enrollments for " + name + ":");
            //Now to process
            try {
                //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                for (int x = 1; x <= enrollmentsPagesCount; x++) {
                    Document document = builder.parse(new File(path + i + "-" + x + ".xml"));
                    Element root = document.getDocumentElement();

                    //Create a temporary list that creates a node for each enrollment object in the xml file received.
                    NodeList tempList = document.getElementsByTagName("enrollment");
                    //for loop that feeds the relevant data into the students ArrayList as new objects.
                    for (int z = 0; z < tempList.getLength(); z++) {
                        Node current = tempList.item(z);
                        if (current.getNodeName().equalsIgnoreCase("enrollment")) {
                            Element eElement = (Element) current;
                            int id = -1;
                            if (studentEnrollments) {
                                id = Integer.parseInt(eElement.getElementsByTagName("class_fk").item(0).getTextContent());
                            } else {
                                id = Integer.parseInt(eElement.getElementsByTagName("student_fk").item(0).getTextContent());
                            }
                            //ID received, now add to the arrayList of receivedIDs.
                            receivedIDs.add(id);
                        }
                    }
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }

        }
        //Now we have an ArrayList of Integers that has the ID numbers of students/classes that we want to use.
        //We can use the search function that takes an integer array and returns the appropriate students/classes arrayList.
        if (studentEnrollments) {
            results = searchClasses(receivedIDs);
        } else {
            results = searchStudents(receivedIDs);
        }
        return results;
    }
}
