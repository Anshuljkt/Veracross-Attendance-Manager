import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by 18anshula on 7/7/17 at 12:55 PM.
 */
public class Initializer {


    static ArrayList<Student> students = new ArrayList<Student>();

    public static void main(String[] args) {
        try {
            //Creates a new DocumentBuilder to handle the xml file using Java DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("xmls/studentsp1.xml"));
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
                    //Preferred Name (Some students don't have one
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
                    //Finally creates the new object and feeds it in.
                    students.add(new Student(fName, lName, id, email, homeroom, grade));
                }
                System.out.println(students.get(i));
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
