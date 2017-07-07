import java.util.ArrayList;

/**
 * Created by 18anshula on 7/7/17 at 6:48 PM.
 */
public class Class {
    private String name;
    private int id;
    private String IDString;
    private String grade;
    private int gradeNum;
    private String teacherName;
    private ArrayList<MeetingTime> meetingTimes = new ArrayList<MeetingTime>();

    public Class(String name, int id, String IDString, String grade, int gradeNum, String teacherName, ArrayList<MeetingTime> meetingTimes) {
        this.name = name;
        this.id = id;
        this.IDString = IDString;
        this.grade = grade;
        this.gradeNum = gradeNum;
        this.teacherName = teacherName;
        this.meetingTimes = meetingTimes;
    }

    public Class() {
        
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

    public String getIDString() {
        return IDString;
    }

    public void setIDString(String IDString) {
        this.IDString = IDString;
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

}
