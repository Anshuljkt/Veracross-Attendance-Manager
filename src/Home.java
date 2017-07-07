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

/**
 * Created by 18anshula on 7/7/17 at 12:55 PM.
 */
public class Home {
    private static int studentPagesCount = 1000;
    private static int classesPagesCount = 1000;
    private static String studentsURL = "https://api.veracross.com/nist/v2/students.xml?grade_level=12,13&page=";
    private static String classesURL = "https://api.veracross.com/nist/v2/classes.xml?school_level=4&page=";
    private static boolean onlyNonAcademic = false; //Choose whether you want only NonAcademic courses or not.


    static ArrayList<Student> students = new ArrayList<Student>();

    public static void main(String[] args) {
        String toDownload = "none";

        initialize(toDownload); //This allows you to decide which sets of data to (re)Download.

        for (Student i : students) {
            System.out.println(i);
        }

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
            if (toDownload.equalsIgnoreCase("both")||toDownload.equalsIgnoreCase("students")) {
                //First connects to the Veracross API and view page 1 of students, and collect the total number of objects from the XML response header.
                URL web = new URL(studentsURL + 1);
                URLConnection connection = web.openConnection();
                int studentObjectCount = Integer.parseInt(connection.getHeaderField("x-total-count")); //This returns a value like 218, which is then converted to an int.
                studentPagesCount = (studentObjectCount / 100) + 1; //Now this will set the number of pages needed to be traversed, based on the fact that there are max 100 objects per page.
                downloadStudents(); //This will download the correct number of files, so that all the Y12-13 students are covered.
                System.out.println("Student files have been downloaded.");
            }
            if (toDownload.equalsIgnoreCase("both")||toDownload.equalsIgnoreCase("classes")) {
                if (onlyNonAcademic) {
                    classesURL = "https://api.veracross.com/nist/v2/classes.xml?course_type=10&school_level=4&page=";
                } else {
                    classesURL = classesURL;
                }
            //Do the same thing again for classes:
            URL web2 = new URL(classesURL + 1);
            URLConnection connection2 = web2.openConnection();
            int classesObjectCount = Integer.parseInt(connection2.getHeaderField("x-total-count"));
            System.out.println(classesObjectCount);
            classesPagesCount = (classesObjectCount / 100) + 1;
            downloadClasses();
                System.out.println("Classes files have been downloaded.");
            }
            if (toDownload.equalsIgnoreCase("none")) {
                System.out.println("ALERT: Student and Class lists have not been downloaded. May result in inaccuracies.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        processStudentsList(); //Each file will now be opened and processed into the Students arrayList.
        processClasses();
    }

    private static void downloadStudents() {
        try {
            for (int i = 1; i <= studentPagesCount; i++) {
                URL website = new URL(studentsURL + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("xmls/students" + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        } catch (Exception e) {
            System.out.println("URL Downloading Error");
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

        }
    }

    private static void downloadClasses() {
        try {
            for (int i = 1; i <= classesPagesCount; i++) {
                URL website = new URL(classesURL + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("xmls/classes" + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        } catch (Exception e) {
            System.out.println("URL Downloading Error");
        }
    }

    private static void processClasses() {

    }
}
