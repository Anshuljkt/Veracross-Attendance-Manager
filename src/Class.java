import java.util.ArrayList;

public class Class implements Comparable<Class> {
    private String name;
    private int id;
    private String stringID;
    private String grade;
    private int gradeNum;
    private String teacherName;
    private ArrayList<MeetingTime> meetingTimes = new ArrayList<MeetingTime>();
    private String type;

    public Class(String name, int id, String stringID, String grade, String teacherName, String type, ArrayList<MeetingTime> meetingTimes) {
        this.name = name;
        this.id = id;
        this.stringID = stringID;
        this.grade = grade;
        this.gradeNum = Integer.parseInt(grade.substring(5));
        this.teacherName = teacherName;
        this.type = type;
        this.meetingTimes = meetingTimes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getstringID() {
        return stringID;
    }

    public void setstringID(String stringID) {
        this.stringID = stringID;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public ArrayList<MeetingTime> getMeetingTimes() {
        return meetingTimes;
    }

    public void setMeetingTimes(ArrayList<MeetingTime> meetingTimes) {
        this.meetingTimes = meetingTimes;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return name + " | " + grade + " | Teacher: " + teacherName + " | ID: " + id;
    }

    public int compareTo(Class compare) {
        return (this.name.compareTo(compare.getName()));
    }
}
