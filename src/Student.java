/**
 * Created by Anshul Agrawal on 7/7/17 at 12:50 PM.
 */
public class Student {
    

    public Student() {
    }


    public Student(String fName, String lName, int id, String email, int homeroom, String grade) {
        this.fName = fName;
        this.lName = lName;
        this.id = id;
        this.email = email;
        this.homeroom = homeroom;
        this.grade = grade;
        this.gradeNum = Integer.parseInt(grade.substring(5));
    }
    private String fName;
    private String lName;
    private int id;
    private String email;
    private int homeroom;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
        this.gradeNum = Integer.parseInt(grade.substring(5));
    }

    public int getGradeNum() {
        return gradeNum;
    }

    public void setGradeNum(int gradeNum) {
        this.gradeNum = gradeNum;
        this.grade = "Year " + gradeNum;
    }

    private String grade;
    private int gradeNum;


    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getHomeroom() {
        return homeroom;
    }

    public void setHomeroom(int homeroom) {
        this.homeroom = homeroom;
    }

    public String toString() {
        return  "First Name = " + fName +
                " | Last Name = " + lName +
                " | Year Level = " + gradeNum +
                " | ID = " + id +
                " | Email = " + email +
                " | Homeroom ID = " + homeroom;
    }
}
