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
    private

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
