package m.srinivas.vmc_water;

/**
 * Created by srinivas on 19/03/18.
 */

public class Department {
    String dep_id,dep_name;
    Department(String dep_id,String dep_name){
        this.dep_id=dep_id;this.dep_name = dep_name;
    }

    public String getDep_id() {
        return dep_id;
    }

    public String getDep_name() {
        return dep_name;
    }
}
