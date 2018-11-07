public class Student implements Comparable<Student> {


    private String fName;
    private String lName;
    private int id;
    private String email;
    private int homeroom;
    private String grade;
    private int gradeNum;
    private String lateTime;
    private String currentClass;

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

    public String getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
    }

    public String getLateTime() {
        return lateTime;
    }

    public void setLateTime(String lateTime) {
        this.lateTime = lateTime;
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
                " | " + grade;
    }

    public String toStringWithID() {
        String response = "";
        String name = fName + " " + lName;
        if (lateTime==null) {
            return response.format("%-50s | %-10s | ID: %-10d", name, grade, id);
        } else {
            return response.format("%-50s | %-10s | ID: %-10d | Arrival Time: %-20s", name, grade, id, lateTime);
        }
    }

    //compareTo method, allows sorting by last name in A-Z.
    public int compareTo(Student compare) {
        return (this.lName.compareTo(compare.lName));
    }
}
