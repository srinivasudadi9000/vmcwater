package m.srinivas.vmc_water;

/**
 * Created by srinivas on 19/03/18.
 */

public class Ward {
    String intWardid,WardNo;
    Ward(String intWardid,String WardNo){
        this.intWardid =intWardid;this.WardNo = WardNo;
    }

    public String getIntWardid() {
        return intWardid;
    }

    public String getWardNo() {
        return WardNo;
    }
}
