import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

class T_UI {
    public static void main(String args[])
            throws IOException, ParserConfigurationException, TransformerException, SAXException {
        
        //check if lock.txt exist 
        File lock = new File("lock.txt");
        try {
            Files.deleteIfExists(lock.toPath());
            System.out.println("File presente e cancellato");
        } catch (IOException e) {
            System.out.println("File assente");
        }

        
        //files to parse
        String[] boxFiles = {"../xml/DM1.xml", "../xml/TT2.xml", "../xml/M3.xml", "../xml/T4.xml",
            "../xml/T5.xml" , "../xml/M6.xml", "../xml/TT7.xml", "../xml/DM8.xml"};
        String[] diag2Files = {"../xml/DM1_DIAG2.xml", "../xml/TT2_DIAG2.xml", "../xml/M3_DIAG2.xml", "../xml/T4_DIAG2.xml",
        "../xml/T5_DIAG2.xml" , "../xml/M6_DIAG2.xml", "../xml/TT7_DIAG2.xml", "../xml/DM8_DIAG2.xml"};
        String[] DM1Data = {"../xml/DM1_STATUS.xml", "../xml/DM1_VERSION.xml", "../xml/DM1_DIAG1.xml"};
        String[] DM8Data = {"../xml/DM8_STATUS.xml", "../xml/DM8_VERSION.xml", "../xml/DM8_DIAG1.xml"};
        
        //MVC Pattern Definition
        ObjController OC = new ObjController(); //Controller
        ArrayList<BoxModel> BoxModelsList = new ArrayList<BoxModel>(); //Model Collection

        //first data parsing
        for (int i = 0; i < boxFiles.length; i++) {
            BoxModel BM = new BoxModel();
            BM = OC.parseBoxFile(boxFiles[i]);
            BM.setDiag2(OC.parseDiag2File(diag2Files[i]));
            if(i == 0) {
                BM.setStatus(OC.parseFile(DM1Data[0]));
                BM.setVersion(OC.parseFile(DM1Data[1]));
                BM.setDiag1(OC.parseFile(DM1Data[2]));
            }
            else if(i == 7) {
                BM.setStatus(OC.parseFile(DM8Data[0]));
                BM.setVersion(OC.parseFile(DM8Data[1]));
                BM.setDiag1(OC.parseFile(DM8Data[2]));
            }
            BoxModelsList.add(BM);
        }

        //parse signal file
        DataVisual DV = new DataVisual(BoxModelsList);  //View
        //set data from special tables
        DV.setGeneralTables(OC.parseGeneralFile("../xml/GENERAL.xml"));
        DV.setSignalsTable(OC.parseFile("../xml/SIGNALS.xml"));
        
        //ui creation and display
        DV.dataDisplay();
        //set the ui which the controller referees to
        OC.setDataVisual(DV);
    
        //check if any file has changed, call the controller if it happens
        continuousFileCheck(boxFiles, diag2Files, DM1Data, DM8Data, OC, DV);
    }


    private static void continuousFileCheck(String[] boxFiles, String[] Diag2Files, String[] DM1Data, String[] DM8Data, ObjController OC, DataVisual DV) {
        for (int i = 0; i < boxFiles.length; i++) {
            fileChecker FC = new fileChecker(OC, boxFiles[i], DV);
            FC.start();
        }
        for(int i = 0;i < Diag2Files.length;i++) {
            fileChecker FC = new fileChecker(OC, Diag2Files[i], DV);
            FC.start();
        }
        for(int i = 0; i < DM1Data.length; i++) {
            fileChecker FCDM1 = new fileChecker(OC, DM1Data[i], DV);
            fileChecker FCDM8 = new fileChecker(OC, DM8Data[i], DV);
            FCDM1.start();
            FCDM8.start();
        }
        fileChecker FCSignals = new fileChecker(OC, "../xml/SIGNALS.xml", DV);
        fileChecker FCSGeneral = new fileChecker(OC, "../xml/GENERAL.xml", DV);
        FCSignals.start();
        FCSGeneral.start();
    }
}