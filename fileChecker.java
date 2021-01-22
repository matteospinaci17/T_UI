import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;




public class fileChecker extends Thread {

    final Integer MAX_NUM_OF_SENSORS = 16;
    final Integer STATUS_NUMBER = 5;
    ObjController OC = new ObjController();
    String XMLtoCheck;
    DataVisual DV = new DataVisual();

    public fileChecker(ObjController OC, String XMLtoCheck, DataVisual DV) {
        this.OC = OC;
        this.XMLtoCheck = XMLtoCheck;
        this.DV = DV;
    }
    //TODO handle exception
    //TODO aggiornare anche i boxmodel quando si rileva un cambiamento, ora cambiano solo le tabelle
    @Override
    public void run() {

        Path fileLocation = Paths.get(XMLtoCheck);
        byte[] oldDigest = null;

        // infinite cycle that checks if the data xml has changed every 0.2 seconds
        while (true) {

            try {

                // choiche of hashing algorithm
                MessageDigest md = MessageDigest.getInstance("MD5");

                try {
                    semWait();
                    String content = new String(Files.readAllBytes(fileLocation), StandardCharsets.UTF_8);
                    semRelease();
                    md.update(content.getBytes());
                    byte[] currentDigest = md.digest();

                    if (!(Arrays.toString(oldDigest).equals(Arrays.toString(currentDigest)))) {
                        oldDigest = currentDigest;
                        //update from signals file (CTestModeReq fail)
                        if(XMLtoCheck.equals("../xml/SIGNALS.xml")) {
                            LinkedHashMap<String,Integer> FDESignals = new LinkedHashMap<String, Integer>();
                            FDESignals = OC.parseFile("../xml/SIGNALS.xml");
                            //CTestModeReq Index = 0
                            DV.getInputs().get(0).setSelectedItem(Integer.toString(FDESignals.get("CTestModeReq")));
                        }
                        //update from status file
                        else if(XMLtoCheck.equals("../xml/DM1_STATUS.xml") || XMLtoCheck.equals("../xml/DM8_STATUS.xml")) {
                            // if the digest are different the file has changed,
                            // so i will parse it again and send new data to the view
                            String[][] newStatusTable = new String[STATUS_NUMBER][2];
                            LinkedHashMap<String,Integer> StatusHM = new LinkedHashMap<String,Integer>();
                            StatusHM = OC.parseFile(XMLtoCheck);
                            DV.fromHMToStatusTable(StatusHM, newStatusTable);
                            OC.updateStatusTable(newStatusTable, XMLtoCheck);                        
                        }
                        //update from version file
                        else if(XMLtoCheck.equals("../xml/DM1_VERSION.xml") || XMLtoCheck.equals("../xml/DM8_VERSION.xml")) {
                            String[][] newVersionTable = new String[1][2];
                            LinkedHashMap<String,Integer> VersionHM = new LinkedHashMap<String,Integer>();
                            VersionHM = OC.parseFile(XMLtoCheck);
                            DV.fromHMtoVersionTable(VersionHM, newVersionTable);
                            OC.updateVersionTable(newVersionTable, XMLtoCheck);
                        }
                        //update from GENERAL file
                        else if(XMLtoCheck.equals("../xml/GENERAL.xml")) {
                            ArrayList<DefaultTableModel> newGeneralTables = OC.parseGeneralFile(XMLtoCheck);
                            OC.updateGeneralTable(newGeneralTables);
                        }
                        //update from a DIAG1 file
                        else if(XMLtoCheck.equals("../xml/DM1_DIAG1.xml") || XMLtoCheck.equals("../xml/DM8_DIAG1.xml")) {
                            String[][] newDiag1Table = new String[15][2];
                            LinkedHashMap<String,Integer> Diag1HM = new LinkedHashMap<String,Integer>();
                            Diag1HM = OC.parseFile(XMLtoCheck);
                            DV.fromHMtoDiag1Table(Diag1HM, newDiag1Table);
                            OC.updateDiag1Table(newDiag1Table, XMLtoCheck);                            
                        }
                        //update from a DIAG2 file
                        else if(XMLtoCheck.contains("DIAG2")) {
                            String[][] newtableDiag2STS = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newtableDiag2IOCARD = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newtableDiag2IOINP = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newtableDiag2AC = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newtableDiag2OUTPUT = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newtableDiag2MAUVALVE = new String[1][2];
                            BoxModel BM = new BoxModel();
                            BM.setDiag2(OC.parseDiag2File(XMLtoCheck));
                            DV.fromHMDiag2toTables(BM, newtableDiag2STS, newtableDiag2IOCARD, newtableDiag2IOINP, newtableDiag2AC, newtableDiag2OUTPUT, newtableDiag2MAUVALVE);
                            OC.updateDiag2Tables(newtableDiag2STS, newtableDiag2IOCARD, newtableDiag2IOINP, newtableDiag2AC, newtableDiag2OUTPUT, newtableDiag2MAUVALVE, XMLtoCheck);
                        }
                        //update from box file
                        else {
                            // if the digest are different the file has changed,
                            // so i will parse it again and send new data to the view
                            String[][] newTableSTS = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newTableLHC = new String[MAX_NUM_OF_SENSORS][2];
                            String[][] newtableAC = new String[MAX_NUM_OF_SENSORS][2];
                            BoxModel BM = new BoxModel();
                            BM = OC.parseBoxFile(XMLtoCheck);
                            DV.fromHMToTable(BM, newTableSTS, newTableLHC, newtableAC);
                            OC.updateBoxTables(newTableSTS, newTableLHC, newtableAC, XMLtoCheck);
                        }
                    } 
                } 
                catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
                    System.out.println("Error reading xml file");
                    e.printStackTrace();
                }

            } catch (NoSuchAlgorithmException e) {
                System.out.println("Error choosing hashing algorithm");
                e.printStackTrace();
            }

            //wait to check again
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void semWait() throws IOException {
        File lock = new File("lock.txt");
        while(lock.isFile()) {
        }
        lock.createNewFile();
    }

    private void semRelease() {
        File lock = new File("lock.txt");
        lock.delete();
    }
}