/**
 * Created by Anshul Agrawal on 7/7/17 at 12:50 PM.
 */
public class Student implements Comparable<Student> {


    private String fName;
    private String lName;
    private int id;
    private String email;
    private int homeroom;
    private String grade;
    private int gradeNum;

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
        return fName + " " + lName +
                " | " + grade +
                " | Student ID = " + id;
    }

//    public String toString() { //Old toString Method
//        return "First Name = " + fName +
//                " | Last Name = " + lName +
//                " | Year Level = " + gradeNum +
//                " | ID = " + id +
//                " | Email = " + email +
//                " | Homeroom ID = " + homeroom;
//    }
    //compareTo method, allows sorting by last name in A-Z.
    public int compareTo(Student compare) {
        return (this.lName.compareTo(compare.lName));
    }
}
