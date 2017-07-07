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

    static ArrayList<Student> students = new ArrayList<Student>();

    public static void main(String[] args) {
        initialize();
        for (Student i:students) {
            System.out.println(i);
        }
    }


    public static void initialize() {

        //This is where the username and password go for accessing Veracross API.
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("api.nist", "X4bZsxMVBr".toCharArray());
            }
        });

        try {

            //Now we first connect to the Veracross API and view page 1 of students, and collect the total number of objects from the XML response header.
            URL web = new URL("https://api.veracross.com/nist/v2/students.xml?grade_level=12,13&page=1");
            URLConnection connection = web.openConnection();
            int objectCount = Integer.parseInt(connection.getHeaderField("x-total-count")); //This returns a value like 218, which is then converted to an int.
            System.out.println(objectCount);
            int pagesCount = (objectCount/100)+1; //Now this will set the number of pages needed to be traversed, based on the fact that there are max 100 objects per page.

            downloadStudents(pagesCount); //Now this will download the correct number of files, so that all the Y12-13 students are covered.
            processStudentsList(pagesCount);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void processStudentsList(int pagesCount) {
        try {
            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (int x = 1; x <= pagesCount; x++) {
                Document document = builder.parse(new File("xmls/students" + x + ".xml"));
                Element root = document.getDocumentElement();
//            System.out.println(root.getNodeName());


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
            System.out.println("XML Processing Error");
        }
    }

    private static void downloadStudents(int pagesCount) {
        try {
            for (int i = 1; i <= pagesCount; i++) {
                URL website = new URL("https://api.veracross.com/nist/v2/students.xml?grade_level=12,13&page=" + i);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream("xmls/students" + i + ".xml");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        } catch (Exception e) {
            System.out.println("URL Downloading Error");
        }
    }
}
