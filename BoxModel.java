import java.util.ArrayList;
import java.util.LinkedHashMap;

//Model
public class BoxModel {

    //TODO rifare mettendo una classe DM che estende la classe BoxModel
    private String Name;
    //String key = sensor location, integer value = sensor output
    private LinkedHashMap<String,Integer> Version = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> STS = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> LHC = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> AC = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Status = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag1 = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag2_STS = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag2_IOCARD = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag2_IOINP = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag2_AC = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag2_OUTPUT = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> Diag2_MAUVALVE = new LinkedHashMap<>();


    public BoxModel(String carCode, LinkedHashMap<String,Integer> STS, LinkedHashMap<String,Integer> LHC, LinkedHashMap<String,Integer> AC ) {
        Name = carCode;
        this.STS = STS;
        this.LHC = LHC;
        this.AC = AC;
        Status = null;
    }

    public BoxModel() {}

    //Getters&Setters
    public LinkedHashMap<String, Integer> getSTS() {
        return STS;
    }

    public LinkedHashMap<String, Integer> getLHC() {
        return LHC;
    }

    public LinkedHashMap<String, Integer> getAC() {
        return AC;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public LinkedHashMap<String, Integer> getStatus() {
        return Status;
    }

    public void setStatus(LinkedHashMap<String, Integer> status) {
        Status = status;
    }

    public LinkedHashMap<String, Integer> getVersion() {
        return Version;
    }

    public void setVersion(LinkedHashMap<String, Integer> Version) {
        this.Version = Version;
    }

    public LinkedHashMap<String, Integer> getDiag1() {
        return Diag1;
    }

    public void setDiag1(LinkedHashMap<String, Integer> diag1) {
        Diag1 = diag1;
    }

    public void setDiag2(ArrayList<LinkedHashMap<String, Integer>> diag2) {
        Diag2_STS = diag2.get(0);
        Diag2_IOCARD = diag2.get(1);
        Diag2_IOINP = diag2.get(2);
        Diag2_AC = diag2.get(3);
        Diag2_OUTPUT = diag2.get(4);
        Diag2_MAUVALVE = diag2.get(5);
    }

    public LinkedHashMap<String, Integer> getDiag2_STS() {
        return Diag2_STS;
    }

    public LinkedHashMap<String, Integer> getDiag2_IOCARD() {
        return Diag2_IOCARD;
    }

    public LinkedHashMap<String, Integer> getDiag2_IOINP() {
        return Diag2_IOINP;
    }

    public LinkedHashMap<String, Integer> getDiag2_AC() {
        return Diag2_AC;
    }

    public LinkedHashMap<String, Integer> getDiag2_OUTPUT() {
        return Diag2_OUTPUT;
    }

    public LinkedHashMap<String, Integer> getDiag2_MAUVALVE() {
        return Diag2_MAUVALVE;
    }


    
      
}
