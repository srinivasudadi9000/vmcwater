package m.srinivas.vmc_water;

/**
 * Created by USER on 17-07-2017.
 */

public class Drilldown {
    String applicationno,applicationname,intgrievanceid;
    Drilldown(String applicationno, String applicationname, String intgrievanceid){
         this.applicationname = applicationname;this.applicationno=applicationno;this.intgrievanceid= intgrievanceid;
    }

    public String getApplicationname() {
        return applicationname;
    }

    public String getApplicationno() {
        return applicationno;
    }

    public String getIntgrievanceid() {
        return intgrievanceid;
    }
}
