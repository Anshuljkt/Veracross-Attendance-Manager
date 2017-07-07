/**
 * Created by 18anshula on 7/7/17 at 6:56 PM.
 */
public class MeetingTime {
    private String day;
    private String block;
    private String block_abbreviation;

    public MeetingTime(String day, String block, String block_abbreviation) {
        this.day = day;
        this.block = block;
        this.block_abbreviation = block_abbreviation;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getBlock_abbreviation() {
        return block_abbreviation;
    }

    public void setBlock_abbreviation(String block_abbreviation) {
        this.block_abbreviation = block_abbreviation;
    }
}
