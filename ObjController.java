import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

//Controller
public class ObjController {

    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    DataVisual DV = new DataVisual();
    final Integer COUNT_MATRIX_ENTRY = 6;
    final Integer STATUS2_MATRIX_ENTRY = 50;
    final Integer DIAG2_MATRIX_ENTRY = 5;

    public ObjController() {
    }

    // TODO handle exception missing file, wrong input
    // -----------BOX FILES PARSING-----------
    public BoxModel parseBoxFile(String fileToParse)
            throws IOException, ParserConfigurationException, TransformerException, SAXException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder doc = docBuilderFactory.newDocumentBuilder();
        BoxModel Model = new BoxModel();
        // get the file name without extension to use as the box model name
        File f = new File(fileToParse);
        Integer dotIndex = f.getName().indexOf(".");
        Model.setName(f.getName().substring(0, dotIndex));
        // parse start
        semWait();
        Document document = doc.parse(new File(fileToParse));
        semRelease();
        // i need to pass the object to return because it is a recursive fuction (xml
        // needs that)
        BoxModel modelToAdd = boxParsingRecursion(document.getDocumentElement(), Model);
        return modelToAdd;
    }

    public ArrayList<LinkedHashMap<String, Integer>> parseDiag2File(String fileToParse)
            throws ParserConfigurationException, SAXException, IOException {
        ArrayList<LinkedHashMap<String, Integer>> Diag2 = new ArrayList<LinkedHashMap<String, Integer>>();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder doc = docBuilderFactory.newDocumentBuilder();
        semWait();
        Document document = doc.parse(new File(fileToParse));
        semRelease();
        LinkedHashMap<String, Integer> Diag2_STS = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> Diag2_IOCARD = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> Diag2_IOINP = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> Diag2_AC = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> Diag2_OUTPUT = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> Diag2_MAUVALVE = new LinkedHashMap<>();
        diag2ParsingRecursion(document.getDocumentElement(), Diag2_STS, Diag2_IOCARD, Diag2_IOINP, Diag2_AC,
                Diag2_OUTPUT, Diag2_MAUVALVE);
        Diag2.add(Diag2_STS);
        Diag2.add(Diag2_IOCARD);
        Diag2.add(Diag2_IOINP);
        Diag2.add(Diag2_AC);
        Diag2.add(Diag2_OUTPUT);
        Diag2.add(Diag2_MAUVALVE);
        return Diag2;
    }

    private void diag2ParsingRecursion(Node node,
            LinkedHashMap<String, Integer> diag2_STS, LinkedHashMap<String, Integer> diag2_IOCARD,
            LinkedHashMap<String, Integer> diag2_IOINP, LinkedHashMap<String, Integer> diag2_AC,
            LinkedHashMap<String, Integer> diag2_OUTPUT, LinkedHashMap<String, Integer> diag2_MAUVALVE) {
        //ignore root element
        if(!node.getNodeName().trim().equals("DIAG2")) {
            if(node.getParentNode().getNodeName().trim().equals("STS")) {
                diag2_STS.put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
            }
            else if(node.getParentNode().getNodeName().trim().equals("IOCARD")) {
                diag2_IOCARD.put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
            }
            else if(node.getParentNode().getNodeName().trim().equals("IOINP")) {
                diag2_IOINP.put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
            }
            else if(node.getParentNode().getNodeName().trim().equals("AC")) {
                diag2_AC.put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
            }
            else if(node.getParentNode().getNodeName().trim().equals("OUTPUT")) {
                diag2_OUTPUT.put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
            }
            else if(node.getParentNode().getNodeName().trim().equals("MAUVALVE")) {
                diag2_MAUVALVE.put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
            }  
        }
        
        //recursion
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // call the recursion
                diag2ParsingRecursion(currentNode, diag2_STS, diag2_IOCARD, diag2_IOINP, diag2_AC,
                diag2_OUTPUT, diag2_MAUVALVE);
            }
        }
    }

    // ------------VERSION FILE PARSING------------
    public ArrayList<DefaultTableModel> parseGeneralFile(String fileToParse) throws SAXException, IOException,
            ParserConfigurationException {
        ArrayList<DefaultTableModel> generalTables = new ArrayList<DefaultTableModel>();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder doc = docBuilderFactory.newDocumentBuilder();
        // parse start
        semWait();        
        Document document = doc.parse(new File(fileToParse));
        semRelease();
        // i need to pass the object to return because it is a recursive fuction (xml
        // needs that)
        LinkedHashMap<String, Integer> CountHM = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> Status2HM = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> Diag2HM = new LinkedHashMap<String, Integer>();
        parseGeneralRecursion(document.getDocumentElement(), CountHM, Status2HM, Diag2HM);
        //from hashmap to matrix, used to build the table
        String[][] CountMatrix = new String[COUNT_MATRIX_ENTRY][2];
        String[][] Status2Matrix = new String[STATUS2_MATRIX_ENTRY][2];
        String[][] Diag2Matrix = new String[DIAG2_MATRIX_ENTRY][2];
        Integer index = 0;
        for(String key : CountHM.keySet()) {
            CountMatrix[index][0] = key;
            CountMatrix[index][1] = Integer.toString(CountHM.get(key));
            index++;
        }
        index = 0;
        for(String key : Status2HM.keySet()) {
            Status2Matrix[index][0] = key;
            Status2Matrix[index][1] = Integer.toString(Status2HM.get(key));
            index++;
        }
        index = 0;
        for(String key : Diag2HM.keySet()) {
            Diag2Matrix[index][0] = key;
            Diag2Matrix[index][1] = Integer.toString(Diag2HM.get(key));
            index++;
        }
        String[] identifiers = {"Name", "Value"};
        DefaultTableModel CountTable = new DefaultTableModel(CountMatrix, identifiers);
        DefaultTableModel Status2Table = new DefaultTableModel(Status2Matrix, identifiers);
        DefaultTableModel Diag2Table = new DefaultTableModel(Diag2Matrix, identifiers);
        generalTables.add(CountTable);
        generalTables.add(Status2Table);
        generalTables.add(Diag2Table);
		return generalTables;
    }

    // ------------FILE PARSING------------
    public LinkedHashMap<String, Integer> parseFile(String fileToParse) throws SAXException, IOException,
    ParserConfigurationException {
        LinkedHashMap<String, Integer> Version = new LinkedHashMap<String, Integer>();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder doc = docBuilderFactory.newDocumentBuilder();
        // parse start
        semWait();        
        Document document = doc.parse(new File(fileToParse));
        semRelease();
        // i need to pass the object to return because it is a recursive fuction (xml
        // needs that)
        String root = document.getDocumentElement().getNodeName();
        Version = parseRecursion(document.getDocumentElement(), Version, root);
        return Version;
    }

    // ------------RECURSIVE PARSING FUNCTIONS------------

    private BoxModel boxParsingRecursion(Node node, BoxModel Model) {
        //add the read data to the corresponding HashMap
        if (node.getParentNode().getNodeName().trim().equals("STS")) {
            Model.getSTS().put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
        } else if (node.getParentNode().getNodeName().trim().equals("LHC")) {
            Model.getLHC().put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
        } else if (node.getParentNode().getNodeName().trim().equals("AC")) {
            Model.getAC().put(node.getNodeName().trim(), Integer.parseInt(node.getFirstChild().getNodeValue().trim()));
        }
        //recursion
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // call the recursion
                boxParsingRecursion(currentNode, Model);
            }
        }
        return Model;
    }
    
    private void parseGeneralRecursion(Node node, LinkedHashMap<String, Integer> CountHM, LinkedHashMap<String, Integer> Status2HM, LinkedHashMap<String, Integer> Diag2HM) {
        if (node.getParentNode().getNodeName().equals("COUNT")) {
            CountHM.put(node.getNodeName(), Integer.parseInt(node.getFirstChild().getNodeValue()));
        }
        else if (node.getParentNode().getNodeName().equals("STATUS2")) {
            Status2HM.put(node.getNodeName(), Integer.parseInt(node.getFirstChild().getNodeValue()));
        }
        else if (node.getParentNode().getNodeName().equals("DIAG2")) {
            Diag2HM.put(node.getNodeName(), Integer.parseInt(node.getFirstChild().getNodeValue()));
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // call the recursion
                parseGeneralRecursion(currentNode, CountHM, Status2HM, Diag2HM);
            }
        }
    }

    private LinkedHashMap<String, Integer> parseRecursion(Node node, LinkedHashMap<String, Integer> parsedKayValuePairs, String rootTagToIgnore) {
        if (!node.getNodeName().trim().equals(rootTagToIgnore)) {
            parsedKayValuePairs.put(node.getNodeName(), Integer.parseInt(node.getFirstChild().getNodeValue()));
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // call the recursion
                parseRecursion(currentNode, parsedKayValuePairs, rootTagToIgnore);
            }
        }
        return parsedKayValuePairs;
    }

    // -----------UPDATE VIEW FUNCTIONS-----------

    public void updateBoxTables(String[][] newTableSTS, String[][] newTableLHC, String[][] newTableAC,
            String changedXML) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        String[] identifiers = { "Location", "Value" };
        Integer tabIndex = DV.getJTP().indexOfTab(getTabName(changedXML));

        // model update
        // TODO can be remade using table names
        DV.getTables().get((tabIndex/2) * 3).setDataVector(newTableSTS, identifiers);
        DV.getTables().get(((tabIndex/2) * 3) + 1).setDataVector(newTableLHC, identifiers);
        DV.getTables().get(((tabIndex/2) * 3) + 2).setDataVector(newTableAC, identifiers);
    }

    public void updateDiag2Tables(String[][] newtableDiag2STS, String[][] newtableDiag2IOCARD,
			String[][] newtableDiag2IOINP, String[][] newtableDiag2AC, String[][] newtableDiag2OUTPUT,
			String[][] newtableDiag2MAUVALVE, String XMLFileName) {
                
        //TODO DA RIFARE CON TAB NAME!!
        //SE L'ORDINE DELLA TABS CAMBIA QUESTA ROBA NON FUNZIONA PIU'
        Integer[] residui = {0, 3, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8, 0, 9};        
        String[] identifiers = {"Diagnostic", "Value"};
        Integer tabIndex = DV.getJTP().indexOfTab(getTabName(XMLFileName));

        DV.getDiag2Tables().get(((tabIndex*3) - residui[tabIndex])).setDataVector(newtableDiag2STS, identifiers);
        DV.getDiag2Tables().get(((tabIndex*3) - residui[tabIndex]) + 1).setDataVector(newtableDiag2IOCARD, identifiers);
        DV.getDiag2Tables().get(((tabIndex*3) - residui[tabIndex]) + 2).setDataVector(newtableDiag2IOINP, identifiers);
        DV.getDiag2Tables().get(((tabIndex*3) - residui[tabIndex]) + 3).setDataVector(newtableDiag2AC, identifiers);
        DV.getDiag2Tables().get(((tabIndex*3) - residui[tabIndex]) + 4).setDataVector(newtableDiag2OUTPUT, identifiers);
        if(getTabName(XMLFileName).equals("DM1_DIAG2") || getTabName(XMLFileName).equals("DM8_DIAG2")) {
            DV.getDiag2Tables().get(((tabIndex*3) - residui[tabIndex]) + 5).setDataVector(newtableDiag2MAUVALVE, identifiers);
        }
        
	}

    // status update
    // TODO can be remade using table names
    public void updateStatusTable(String[][] newStatusTable, String changedXML) {
        String[] identifiers = { "State Sensor", "Value" };
        Integer tabIndex = 0;
        if (changedXML.equals("../xml/DM1_STATUS.xml")) {
            tabIndex = 0;
        } else if (changedXML.equals("../xml/DM8_STATUS.xml")) {
            tabIndex = 1;
        }
        DV.getStatusTables().get(tabIndex).setDataVector(newStatusTable, identifiers);
    }

    //version update
    public void updateVersionTable(String[][] newVersionTable, String changedXML) {
        String[] identifiers = { "", "" };
        Integer tabIndex = 0;
        if (changedXML.equals("../xml/DM1_VERSION.xml")) {
            tabIndex = 0;
        } else if (changedXML.equals("../xml/DM8_VERSION.xml")) {
            tabIndex = 1;
        }
        DV.getVersionTables().get(tabIndex).setDataVector(newVersionTable, identifiers);
    }

    //general table update
    public void updateGeneralTable(ArrayList<DefaultTableModel> newGeneralTables) {
        Object[][] tableData;
        int nRow, nCol;
        for(int i = 0; i < newGeneralTables.size(); i++) {
            //from defaultTableModel to Matrix
            nRow = newGeneralTables.get(i).getRowCount();
            nCol = newGeneralTables.get(i).getColumnCount();
            tableData = new Object[nRow][nCol];
            for (int index = 0 ; index < nRow ; index++)
                for (int j = 0 ; j < nCol ; j++)
                    tableData[index][j] = newGeneralTables.get(i).getValueAt(index,j);
            String[] identifiers = {"Name", "Value"};
            DV.getGeneralTables().get(i).setDataVector(tableData, identifiers);
        }
    }

    //diag1 tables update
	public void updateDiag1Table(String[][] newDiag1Table, String changedXML) {
        String[] identifiers = { "Name", "Value" };
        Integer tabIndex = 0;
        if (changedXML.equals("../xml/DM1_DIAG1.xml")) {
            tabIndex = 0;
        } else if (changedXML.equals("../xml/DM8_DIAG1.xml")) {
            tabIndex = 1;
        }
        DV.getDiag1Tables().get(tabIndex).setDataVector(newDiag1Table, identifiers);
	}

    private String getTabName(String XMLFileName) {
        Integer dotIndex = XMLFileName.lastIndexOf(".");
        Integer slashIndex = XMLFileName.lastIndexOf("/");
        String tabName = XMLFileName.substring(slashIndex + 1, dotIndex);
        return tabName;
    }

    public void updateSignalXML(String name, String selectedItem)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
        //from TRUE and FALSE to integer equivalent
        String value;
        if(isNumeric(selectedItem)) {
            value = selectedItem;
        }
        //this only works because there are just (On,True) and (Off,False) as literal inputs
        else if(selectedItem.equals("TRUE") || selectedItem.equals("ON")) {
            value = "1";
        }
        else {
            value = "0";
        }

        final String SignalXML = "../xml/SIGNALS.xml";
        try {
            semWait();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(SignalXML);
            
            // Get the staff element by tag name directly
            Node SIGNALS = doc.getElementsByTagName("SIGNALS").item(0);
    
            // loop the staff child node
            NodeList list = SIGNALS.getChildNodes();
    
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
               if (name.equals(node.getNodeName())) {
                    node.setTextContent(value);
               }
            }
    
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(SignalXML));
            transformer.transform(source, result);
            semRelease();
        }
        finally {}
    }


    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false; 
        }
        return pattern.matcher(strNum).matches();
    }

    private void semWait() throws IOException {
        File lock = new File("lock.txt");
        //se lock esiste ciclo
        while(lock.isFile()) {}
        //quando lock non esiste più creo il mio lock
        lock.createNewFile();        
    }

    private void semRelease() {
        File lock = new File("lock.txt");
        lock.delete();
    }

    public void setDataVisual(DataVisual DV) {
        this.DV = DV;
    }

    public DataVisual getDataVisual() {
        return DV;
    }
	
}
