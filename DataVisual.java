import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

//View 
public class DataVisual {

    private final int MAX_N_OF_SENSORS = 16;
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private JFrame mainFrame = new JFrame("T_UI");
    private ColorCheck CC = new ColorCheck();
    private JTabbedPane JTP = new JTabbedPane();
    private ArrayList<BoxModel> TrainSensList = new ArrayList<BoxModel>();

    private ArrayList<DefaultTableModel> BoxTables = new ArrayList<DefaultTableModel>();
    private ArrayList<DefaultTableModel> StatusTables = new ArrayList<DefaultTableModel>();
    private ArrayList<DefaultTableModel> VersionTables = new ArrayList<DefaultTableModel>();
    private ArrayList<DefaultTableModel> GeneralTables = new ArrayList<DefaultTableModel>();
    private ArrayList<DefaultTableModel> Diag1Tables = new ArrayList<DefaultTableModel>();
    private ArrayList<DefaultTableModel> Diag2Tables = new ArrayList<DefaultTableModel>();

    private LinkedHashMap<String, Integer> SignalsTable = new LinkedHashMap<>();

    ArrayList<JLabel> Labels = new ArrayList<JLabel>();
    ArrayList<JComboBox<String>> Inputs = new ArrayList<JComboBox<String>>();

    // data object parsed from xml or json
    public DataVisual(ArrayList<BoxModel> TrainSensList) {
        this.TrainSensList = TrainSensList;
    }

    public DataVisual() {
    }

    private void buildView() {

        mainFrame.setLayout(new BorderLayout());
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowListener() {
            @Override
            //se lock.txt esiste lo cancello per evitare problemi alla prossima apertura del programma
            public void windowClosing(WindowEvent event) {
                mainFrame.dispose();
                File lock = new File("lock.txt");
                try {
                    Files.deleteIfExists(lock.toPath());
                    System.out.println("File presente e cancellato");
                } catch (IOException e) {
                    System.out.println("File assente");
                }
                System.exit(0);
            }

            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        String[] columns = { "Location", "Value" };
        String[] statusColumns = { "Sensor State", "Value" };
        String[] versionColumns = { "Version", "Number" };
        String[] diag1Columns = { "Name", "Value" };

        // tabbed pane creation
        // for each box i will create two panel, one for the sensors, one for
        // diagnostics
        for (BoxModel BM : TrainSensList) {
            // sensor panel
            String[][] tableSTS = new String[MAX_N_OF_SENSORS][2];
            String[][] tableLHC = new String[MAX_N_OF_SENSORS][2];
            String[][] tableAC = new String[MAX_N_OF_SENSORS][2];
            String[][] tableStatus = new String[5][2];
            String[][] tableVersion = new String[1][2];
            String[][] tableDiag1 = new String[15][2];

            fromHMToTable(BM, tableSTS, tableLHC, tableAC);

            if (BM.getName().equals("DM1") || BM.getName().equals("DM8")) {
                fromHMToStatusTable(BM.getStatus(), tableStatus);
                fromHMtoVersionTable(BM.getVersion(), tableVersion);
                fromHMtoDiag1Table(BM.getDiag1(), tableDiag1);
            }

            JPanel JP = new JPanel();

            if (BM.getName().equals("DM1") || BM.getName().equals("DM8")) {
                JP.setLayout(new GridLayout(2, 4));
            } else {
                JP.setLayout(new GridLayout(1, 3));
            }

            JP.add(buildTab(tableSTS, columns, "STS", BM.getName()));
            JP.add(buildTab(tableLHC, columns, "LHC", BM.getName()));
            JP.add(buildTab(tableAC, columns, "AC", BM.getName()));
            if (BM.getName().equals("DM1") || BM.getName().equals("DM8")) {
                JP.add(buildTab(tableDiag1, diag1Columns, "Diag1", BM.getName()));
                JP.add(buildTab(tableStatus, statusColumns, "State1", BM.getName()));
                JP.add(buildTab(tableVersion, versionColumns, "Ident", BM.getName()));
            }

            JTP.add(BM.getName(), JP);
            JP.setName(BM.getName());
            CC.getPanels().add(JP);

            // diagnostic panel
            String[][] tableDiag2STS = new String[MAX_N_OF_SENSORS][2];
            String[][] tableDiag2IOCARD = new String[MAX_N_OF_SENSORS][2];
            String[][] tableDiag2IOINP = new String[MAX_N_OF_SENSORS][2];
            String[][] tableDiag2AC = new String[MAX_N_OF_SENSORS][2];
            String[][] tableDiag2OUTPUT = new String[MAX_N_OF_SENSORS][2];
            String[][] tableDiag2MAUVALVE = new String[1][2];

            fromHMDiag2toTables(BM, tableDiag2STS, tableDiag2IOCARD, tableDiag2IOINP, tableDiag2AC, tableDiag2OUTPUT,
                    tableDiag2MAUVALVE);

            String[] identifiers = { "Diagnostic", "Value" };
            JPanel JPDiag2 = new JPanel();
            JPDiag2.setLayout(new GridLayout(2, 3));
            JPDiag2.add(buildTab(tableDiag2STS, identifiers, "STS_Diag2", BM.getName() + "_DIAG2"));
            JPDiag2.add(buildTab(tableDiag2IOCARD, identifiers, "IOCARD_Diag2", BM.getName() + "_DIAG2"));
            JPDiag2.add(buildTab(tableDiag2IOINP, identifiers, "IOINP_Diag2", BM.getName() + "_DIAG2"));
            JPDiag2.add(buildTab(tableDiag2AC, identifiers, "AC_Diag2", BM.getName() + "_DIAG2"));
            JPDiag2.add(buildTab(tableDiag2OUTPUT, identifiers, "OUTPUT_Diag2", BM.getName() + "_DIAG2"));

            if (BM.getName().equals("DM1") || BM.getName().equals("DM8")) {
                JPDiag2.add(buildTab(tableDiag2MAUVALVE, identifiers, "MAUVALVE_Diag2", BM.getName() + "_DIAG2"));
            }
            JPDiag2.setName(BM.getName() + "_DIAG2");
            JTP.add(BM.getName() + "_DIAG2", JPDiag2);
            CC.getPanels().add(JPDiag2);
            
        }

        JTP.add("TCMS", buildTCMSTab());
        JTP.add("General", buildGeneralTab());
        CC.setJTP(JTP);
        mainFrame.add(JTP);
        mainFrame.pack();
    }

    private JPanel buildGeneralTab() {
        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new GridLayout(1, 3));
        JTable JTDT;
        JScrollPane JSPDT;
        // Status2 table  [index 1 in GeneralTables]
        ColorCheckObject CCO = new ColorCheckObject("General", GeneralTables.get(1));
        JTDT =  initializeAndSetRenderer(GeneralTables.get(1), "Status2", CCO);
        // make table uneditable
        JTDT.setDefaultEditor(Object.class, null);
        JSPDT = new JScrollPane(JTDT);
        JSPDT.setBorder(BorderFactory.createTitledBorder("Status2"));
        generalPanel.add(JSPDT);
        // Count table  [index 0 in GeneralTables]
        JTDT = new JTable(GeneralTables.get(0));
        // make table uneditable
        JTDT.setDefaultEditor(Object.class, null);
        JSPDT = new JScrollPane(JTDT);
        JSPDT.setBorder(BorderFactory.createTitledBorder("Count"));
        generalPanel.add(JSPDT);
        // Diag2 table  [index 2 in GeneralTables]
        CCO = new ColorCheckObject("Diag2", GeneralTables.get(2));
        JTDT = initializeAndSetRenderer(GeneralTables.get(2), "Diag2", CCO);
        // make table uneditable
        JTDT.setDefaultEditor(Object.class, null);
        JSPDT = new JScrollPane(JTDT);
        JSPDT.setBorder(BorderFactory.createTitledBorder("Diag2"));
        generalPanel.add(JSPDT);
        return generalPanel;
    }

    private JPanel buildTCMSTab() {
        String[] CTestModeReqInput = { "0", "1", "2", "3", "4", "5", "6" };
        String[] ICcuLifeSignInput = { "ON", "OFF" };
        String[] ITimeZoneInput = { "0", "4", "8", "12", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52",
                "56", "60", "64", "68", "72", "76", "80", "84", "88", "92", "96" };
        String[] ISwDownLdModeInput = { "TRUE", "FALSE" };
        String[] IDaylightTimeInput = { "ON", "OFF" };

        JPanel JP = new JPanel();
        JP.setLayout(new GridLayout(SignalsTable.keySet().size(), 1));

        // Create labels and combolists for each signal in xml file
        createLabelAndComboList("CTestModeReq", CTestModeReqInput, Labels, Inputs);
        createLabelAndComboList("ICcuLifeSign", ICcuLifeSignInput, Labels, Inputs);
        createLabelAndComboList("ITimeZone", ITimeZoneInput, Labels, Inputs);
        createLabelAndComboList("ISwDownLdMode", ISwDownLdModeInput, Labels, Inputs);
        createLabelAndComboList("IDaylightTime", IDaylightTimeInput, Labels, Inputs);

        JPanel JPtemp;
        String startValue;
        // add CTestModeReq [position 0 in ArrayList] and selected its starting value
        JPtemp = new JPanel();
        JPtemp.add(Labels.get(0));
        JPtemp.add(Inputs.get(0));
        startValue = Integer.toString(SignalsTable.get(Labels.get(0).getText()));
        Inputs.get(0).setSelectedItem(startValue);
        Inputs.get(0).setName(Labels.get(0).getText());
        JP.add(JPtemp);

        // add ICcuLifeSign [position 1 in ArrayList] and selected its starting value
        JPtemp = new JPanel();
        JPtemp.add(Labels.get(1));
        JPtemp.add(Inputs.get(1));
        if (SignalsTable.get(Labels.get(1).getText()).equals(0)) {
            startValue = "OFF";
        } else {
            startValue = "ON";
        }
        Inputs.get(1).setSelectedItem(startValue);
        Inputs.get(1).setName(Labels.get(1).getText());
        JP.add(JPtemp);

        // add ITimeZone [position 2 in ArrayList] and selected its starting value
        JPtemp = new JPanel();
        JPtemp.add(Labels.get(2));
        JPtemp.add(Inputs.get(2));
        startValue = Integer.toString(SignalsTable.get(Labels.get(2).getText()));
        Inputs.get(2).setSelectedItem(startValue);
        Inputs.get(2).setName(Labels.get(2).getText());
        JP.add(JPtemp);

        // add ISwDownLdMode [position 3 in ArrayList] and selected its starting value
        JPtemp = new JPanel();
        JPtemp.add(Labels.get(3));
        JPtemp.add(Inputs.get(3));
        if (SignalsTable.get(Labels.get(3).getText()).equals(0)) {
            startValue = "FALSE";
        } else {
            startValue = "TRUE";
        }
        Inputs.get(3).setSelectedItem(startValue);
        Inputs.get(3).setName(Labels.get(3).getText());
        JP.add(JPtemp);

        // add IDaylightTime [position 4 in ArrayList] and selected its starting value
        JPtemp = new JPanel();
        JPtemp.add(Labels.get(4));
        JPtemp.add(Inputs.get(4));
        if (SignalsTable.get(Labels.get(4).getText()).equals(0)) {
            startValue = "OFF";
        } else {
            startValue = "ON";
        }
        Inputs.get(4).setSelectedItem(startValue);
        Inputs.get(4).setName(Labels.get(4).getText());
        JP.add(JPtemp);

        // add listeners
        for (JComboBox<String> JCB : Inputs) {
            JCB.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        ObjController OC = new ObjController();
                        try {
                            OC.updateSignalXML(JCB.getName(), (String) JCB.getSelectedItem());
                        } catch (ParserConfigurationException | SAXException | IOException e1) {
                            e1.printStackTrace();
                        } catch (TransformerException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
            });
        }

        // create force system time on FDE button
        JPtemp = new JPanel();
        JLabel ISystemUtcTime = new JLabel("ISystemUtcTime");
        JLabel opResultST = new JLabel("");
        JButton forceSystemTime = new JButton("Force System Time");
        forceSystemTime.setName("ISystemUtcTime");
        // signal name
        JPtemp.add(ISystemUtcTime);
        // operation button
        JPtemp.add(forceSystemTime);
        // empty label for operation result (changed on click)
        JPtemp.add(opResultST);
        JP.add(JPtemp);
        // listener creation
        forceSystemTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ObjController OC = new ObjController();
                try {
                    // converting milliseconds to seconds as required in InterfaceDefinition
                    Long time = System.currentTimeMillis() / 1000;
                    OC.updateSignalXML(forceSystemTime.getName(), Long.toString(time));
                    opResultST.setText("Time set to: " + Long.toString(time));
                } catch (ParserConfigurationException | SAXException | IOException e1) {
                    e1.printStackTrace();
                } catch (TransformerException e1) {
                    e1.printStackTrace();
                }
            }
        });
        return JP;
    }

    private void createLabelAndComboList(String buttonName, String[] possibleInput, ArrayList<JLabel> Labels,
            ArrayList<JComboBox<String>> Inputs) {
        JComboBox<String> inputValue = new JComboBox<String>(possibleInput);
        JLabel LB = new JLabel(buttonName);
        Labels.add(LB);
        Inputs.add(inputValue);
    }

    public void fromHMToStatusTable(LinkedHashMap<String, Integer> StatusHM, String[][] tableStatus) {
        Integer index = 0;
        for (String statusSignalName : StatusHM.keySet()) {
            tableStatus[index][0] = statusSignalName;
            tableStatus[index][1] = Integer.toString(StatusHM.get(statusSignalName));
            index++;
        }
    }

    public void fromHMToTable(BoxModel BM, String[][] tableSTS, String[][] tableLHC, String[][] tableAC) {
        int index = 0;
        for (String locationName : BM.getSTS().keySet()) {
            tableSTS[index][0] = locationName;
            tableSTS[index][1] = Integer.toString(BM.getSTS().get(locationName));
            index++;
        }
        index = 0;
        for (String locationName : BM.getLHC().keySet()) {
            tableLHC[index][0] = locationName;
            tableLHC[index][1] = Integer.toString(BM.getLHC().get(locationName));
            index++;
        }
        index = 0;
        for (String locationName : BM.getAC().keySet()) {
            tableAC[index][0] = locationName;
            tableAC[index][1] = Integer.toString(BM.getAC().get(locationName));
            index++;
        }
    }

    public void fromHMDiag2toTables(BoxModel bM, String[][] tableDiag2STS, String[][] tableDiag2IOCARD,
            String[][] tableDiag2IOINP, String[][] tableDiag2AC, String[][] tableDiag2OUTPUT,
            String[][] tableDiag2MAUVALVE) {

        Integer index = 0;
        for (String key : bM.getDiag2_STS().keySet()) {
            tableDiag2STS[index][0] = key;
            tableDiag2STS[index][1] = Integer.toString(bM.getDiag2_STS().get(key));
            index++;
        }
        index = 0;
        for (String key : bM.getDiag2_IOCARD().keySet()) {
            tableDiag2IOCARD[index][0] = key;
            tableDiag2IOCARD[index][1] = Integer.toString(bM.getDiag2_IOCARD().get(key));
            index++;
        }
        index = 0;
        for (String key : bM.getDiag2_IOINP().keySet()) {
            tableDiag2IOINP[index][0] = key;
            tableDiag2IOINP[index][1] = Integer.toString(bM.getDiag2_IOINP().get(key));
            index++;
        }
        index = 0;
        for (String key : bM.getDiag2_AC().keySet()) {
            tableDiag2AC[index][0] = key;
            tableDiag2AC[index][1] = Integer.toString(bM.getDiag2_AC().get(key));
            index++;
        }
        index = 0;
        for (String key : bM.getDiag2_OUTPUT().keySet()) {
            tableDiag2OUTPUT[index][0] = key;
            tableDiag2OUTPUT[index][1] = Integer.toString(bM.getDiag2_OUTPUT().get(key));
            index++;
        }
        index = 0;
        // solo DM1 e DM8 hanno MAUVALVE, quindi in certi casi può essere vuota
        if (!bM.getDiag2_MAUVALVE().isEmpty()) {
            for (String key : bM.getDiag2_MAUVALVE().keySet()) {
                tableDiag2MAUVALVE[index][0] = key;
                tableDiag2MAUVALVE[index][1] = Integer.toString(bM.getDiag2_MAUVALVE().get(key));
                index++;
            }
            index = 0;
        }

    }

    public void fromHMtoVersionTable(LinkedHashMap<String, Integer> VersionHM, String[][] tableVersion) {
        tableVersion[0][0] = "Version";
        tableVersion[0][1] = VersionHM.get("ISysSwVersionMain") + "." + VersionHM.get("ISysSwVersionDiag") + "."
                + VersionHM.get("ISysSwReleaseMain") + "." + VersionHM.get("ISysSwReleaseDiag");
    }

    public void fromHMtoDiag1Table(LinkedHashMap<String, Integer> diag1, String[][] tableDiag1) {
        Integer index = 0;
        for (String key : diag1.keySet()) {
            tableDiag1[index][0] = key;
            tableDiag1[index][1] = Integer.toString(diag1.get(key));
            index++;
        }
    }

    private JScrollPane buildTab(String[][] dataTable, String[] columns, String tableTitle, String JTabName) {
        ColorCheckObject CCO = new ColorCheckObject(JTabName, dataTable);
        CC.add(CCO);
        DefaultTableModel DTModel = new DefaultTableModel(dataTable, columns);
        JTable JTDT = initializeAndSetRenderer(DTModel, tableTitle, CCO);
        // ogni tipo di tabella ha il suo error color code

        // make table uneditable
        JTDT.setDefaultEditor(Object.class, null);
        // identifico la categoria di valori a cui appartiene questa tabella e lo
        // aggiungo
        // serve alla funzione updateTables
        if (columns[0].equals("Location")) {
            BoxTables.add(DTModel);
        } else if (columns[0].equals("Sensor State")) {
            StatusTables.add(DTModel);
        } else if (columns[0].equals("Version")) {
            VersionTables.add(DTModel);
        } else if (columns[0].equals("Name")) {
            Diag1Tables.add(DTModel);
        } else if (columns[0].equals("Diagnostic")) {
            Diag2Tables.add(DTModel);
        }
        JScrollPane JSPDT = new JScrollPane(JTDT);
        JSPDT.setBorder(BorderFactory.createTitledBorder(tableTitle));
        return JSPDT;
    }

    private JTable initializeAndSetRenderer(DefaultTableModel dTModel, String tableTitle, ColorCheckObject CCO) {
        JTable JTDT;
        if (tableTitle.equals("STS")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    ArrayList<Integer> Error = new ArrayList<Integer>();
                    ArrayList<Integer> Fault = new ArrayList<Integer>();
                    ArrayList<Integer> notConnected = new ArrayList<Integer>();
                    Error.add(1);
                    Error.add(2);
                    Error.add(3);
                    Fault.add(4);
                    notConnected.add(5);
                    comp = setColorCode(Error, Fault, notConnected, comp, getModel(), value, getModel().getColumnCount(), row, col, CCO);
                    return comp;       
                }
            };
        } 
        else if (tableTitle.equals("LHC") || tableTitle.equals("AC")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    ArrayList<Integer> Error = new ArrayList<Integer>();
                    ArrayList<Integer> Fault = new ArrayList<Integer>();
                    ArrayList<Integer> notConnected = new ArrayList<Integer>();
                    Error.add(1);
                    Fault.add(2);
                    notConnected.add(3);
                    comp = setColorCode(Error, Fault, notConnected, comp, getModel(), value, getModel().getColumnCount(), row, col, CCO);
                    return comp;       
                }
            };
        } 
        else if (tableTitle.equals("STS_Diag2")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    ArrayList<Integer> Error = new ArrayList<Integer>();
                    ArrayList<Integer> Fault = new ArrayList<Integer>();
                    ArrayList<Integer> notConnected = new ArrayList<Integer>();
                    Fault.add(1);
                    Fault.add(2);
                    Fault.add(3);
                    notConnected.add(4);
                    comp = setColorCode(Error, Fault, notConnected, comp, getModel(), value, getModel().getColumnCount(), row, col, CCO);
                    return comp;       
                }
            };
        } 
        else if (tableTitle.equals("IOCARD_Diag2")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    ArrayList<Integer> Error = new ArrayList<Integer>();
                    ArrayList<Integer> Fault = new ArrayList<Integer>();
                    ArrayList<Integer> notConnected = new ArrayList<Integer>();
                    Fault.add(1);
                    Fault.add(2);
                    notConnected.add(3);
                    comp = setColorCode(Error, Fault, notConnected, comp, getModel(), value, getModel().getColumnCount(), row, col, CCO);
                    return comp;       
                }
            };
        } 
        else if (tableTitle.equals("AC_Diag2")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    ArrayList<Integer> Error = new ArrayList<Integer>();
                    ArrayList<Integer> Fault = new ArrayList<Integer>();
                    ArrayList<Integer> notConnected = new ArrayList<Integer>();
                    Fault.add(1);
                    Fault.add(2);
                    Fault.add(3);
                    Error.add(4);
                    notConnected.add(5);
                    comp = setColorCode(Error, Fault, notConnected, comp, getModel(), value, getModel().getColumnCount(), row, col, CCO);
                    return comp;       
                }
            };
        }
        else if(tableTitle.equals("Status2")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    if (value != null) {
                        String convertedToString = String.valueOf(value);
                        if(!isNumeric(convertedToString)) {
                            Object nextValue = getModel().getValueAt(row, col + 1);
                            String adjacentValue = String.valueOf(nextValue);
                            Integer adjacentIntValue = Integer.parseInt(adjacentValue);
                            if(convertedToString.equals("IFireOnboardTx") || convertedToString.equals("IFireOnboardRx")) {
                                if(adjacentIntValue == 0) {
                                    comp.setBackground(Color.RED);
                                }
                                else if(adjacentIntValue == 1){
                                    comp.setBackground(Color.WHITE);
                                }
                                else {
                                    comp.setBackground(Color.LIGHT_GRAY);
                                }
                                return comp;
                            }
                            else if(convertedToString.equals("IFireGeneralAlarm") || convertedToString.contains("FireAlarm")) {
                                if(adjacentIntValue == 0) {
                                    comp.setBackground(Color.WHITE);
                                }
                                else if(adjacentIntValue > 0 && adjacentIntValue < 6) {
                                    comp.setBackground(Color.RED);
                                }
                                else {
                                    comp.setBackground(Color.LIGHT_GRAY);
                                }
                                return comp;
                            }
                            else if(convertedToString.contains("MauIn")) {
                                if(adjacentIntValue == 0) {
                                    comp.setBackground(Color.WHITE);
                                }
                                else if(adjacentIntValue == 1) {
                                    comp.setBackground(Color.RED);
                                }
                                else if(adjacentIntValue == 2) {
                                    comp.setBackground(Color.YELLOW);
                                }
                                else if(adjacentIntValue == 3) {
                                    comp.setBackground(Color.CYAN);
                                }
                                else {
                                    comp.setBackground(Color.LIGHT_GRAY);
                                }
                                return comp;
                            }
                            else if(convertedToString.contains("IElectrovalve")) {
                                if(adjacentIntValue == 0 || adjacentIntValue == 1) {
                                    comp.setBackground(Color.WHITE);
                                }
                                else if(adjacentIntValue == 2) {
                                    comp.setBackground(Color.YELLOW);
                                }
                                else {
                                    comp.setBackground(Color.LIGHT_GRAY);
                                }
                                return comp;
                            }
                            else if(convertedToString.contains("TC_BI_FDE_X_")) {
                                if(adjacentIntValue == 0 || adjacentIntValue == 1) {
                                    comp.setBackground(Color.WHITE);
                                }
                                else if(adjacentIntValue == 2) {
                                    comp.setBackground(Color.YELLOW);
                                }
                                else if(adjacentIntValue == 3) {
                                    comp.setBackground(Color.CYAN);
                                }
                                else {
                                    comp.setBackground(Color.LIGHT_GRAY);
                                }
                                return comp;
                            }
                        }
                    }
                    //se la cella è vuota
                    else {
                        comp.setBackground(Color.white);
                    }
                    return comp;
                }
            };

        }

        else if(tableTitle.equals("Diag2")) {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    if (value != null) {
                        String convertedToString = String.valueOf(value);
                        if(!isNumeric(convertedToString)) {
                            Object nextValue = getModel().getValueAt(row, col + 1);
                            String adjacentValue = String.valueOf(nextValue);
                            Integer adjacentIntValue = Integer.parseInt(adjacentValue);
                            if(adjacentIntValue == 0) {
                                comp.setBackground(Color.white); 
                            }
                            else if(adjacentIntValue > 0 && adjacentIntValue < 9) {
                                comp.setBackground(Color.YELLOW); 
                            }
                            else {
                                comp.setBackground(Color.LIGHT_GRAY); 
                            }
                        }
                        // se non è nullo, ma non è numerico è una label
                    }
                    //se la cella è vuota
                    else {
                        comp.setBackground(Color.white);
                    }
                    return comp;
                }
            };

        }
        
        else {
            JTDT = new JTable(dTModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component comp = super.prepareRenderer(renderer, row, col);
                    Object value = getModel().getValueAt(row, col);
                    ArrayList<Integer> Error = new ArrayList<Integer>();
                    ArrayList<Integer> Fault = new ArrayList<Integer>();
                    ArrayList<Integer> notConnected = new ArrayList<Integer>();
                    Fault.add(1);
                    Fault.add(2);
                    Fault.add(3);
                    Fault.add(4);
                    comp = setColorCode(Error, Fault, notConnected, comp, getModel(), value, getModel().getColumnCount(), row, col, CCO);
                    return comp;
                }
            };
        }
        return JTDT;
    }

    private Component setColorCode(ArrayList<Integer> Error, ArrayList<Integer> Fault, ArrayList<Integer> notConnected,
            Component comp, TableModel tableModel, Object value, int nCols, int currentR, int currentC,
            ColorCheckObject CCO) {
        int intValue = -1;
        if (value != null) {
            String convertedToString = String.valueOf(value);
            if (isNumeric(convertedToString)) {
                intValue = Integer.parseInt(convertedToString);
                //ERRORE
                if (!Error.isEmpty() && Error.contains(intValue)) {
                    comp.setBackground(Color.RED);
                    CCO.addColorCode(3, currentR);
                } 
                //FAULT
                else if(!Fault.isEmpty() && Fault.contains(intValue)) {
                    comp.setBackground(Color.YELLOW);
                    CCO.addColorCode(2, currentR);
                }
                //NOT CONNECTED
                else if(!notConnected.isEmpty() && notConnected.contains(intValue)) {
                    comp.setBackground(Color.CYAN);
                    CCO.addColorCode(1, currentR);
                }
                //OK
                else if(intValue == 0){
                    comp.setBackground(Color.WHITE);
                    CCO.addColorCode(0, currentR);
                }
                //VALUE NOT RECOGNIZED
                else {
                    comp.setBackground(Color.LIGHT_GRAY);
                }
                CC.checkColors(CCO.getTabName(), JTP);
                return comp;
            }
            // se non è nullo, ma non è numerico è una label
            else {
                // catch version error
                if (currentC + 1 < nCols) {
                    Object nextValue = tableModel.getValueAt(currentR, currentC + 1);
                    String adjacentValue = String.valueOf(nextValue);
                    if(isNumeric(adjacentValue)) {
                        Integer adjacentIntValue = Integer.parseInt(adjacentValue);
                        if(!Error.isEmpty() && Error.contains(adjacentIntValue)) {
                            comp.setBackground(Color.RED);
                        }
                        else if(!Fault.isEmpty() && Fault.contains(adjacentIntValue)) {
                            comp.setBackground(Color.YELLOW);
                        }
                        else if(!notConnected.isEmpty() && notConnected.contains(adjacentIntValue)) {
                            comp.setBackground(Color.CYAN);
                        }
                        else if(adjacentIntValue == 0){
                            comp.setBackground(Color.WHITE);
                        }
                        else {
                            comp.setBackground(Color.LIGHT_GRAY);
                        }
                        return comp;
                    }
                }
                else {
                    comp.setBackground(Color.WHITE);
                    return comp;
                }
                comp.setBackground(Color.WHITE);
                return comp;
            }
        }
        //se la cella è vuota
        else {
            comp.setBackground(Color.white);
        }
        return comp;
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false; 
        }
        return pattern.matcher(strNum).matches();
    }
    
    public void dataDisplay() {
        buildView();
        // make the frame full screen
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
    }
    
    public JTabbedPane getJTP() {
        return JTP;
    }

    public ArrayList<DefaultTableModel> getTables() {
        return BoxTables;
    }

    public ArrayList<JComboBox<String>> getInputs() {
        return Inputs;
    } 

    public ArrayList<JLabel> getLabels() {
        return Labels;
    }

    public ArrayList<DefaultTableModel> getStatusTables() {
        return StatusTables;
    }

    public ArrayList<DefaultTableModel> getVersionTables() {
        return VersionTables;
    }

	public void setGeneralTables(ArrayList<DefaultTableModel> parseGeneralFile) {
        this.GeneralTables = parseGeneralFile;
    }

    public ArrayList<DefaultTableModel> getGeneralTables() {
        return GeneralTables;
    }

    public LinkedHashMap<String, Integer> getSignalsTable() {
        return SignalsTable;
    }

    public void setSignalsTable(LinkedHashMap<String, Integer> signalsTable) {
        SignalsTable = signalsTable;
    }

    public ArrayList<DefaultTableModel> getDiag1Tables() {
        return Diag1Tables;
    }

    public void setDiag1Tables(ArrayList<DefaultTableModel> diag1Tables) {
        Diag1Tables = diag1Tables;
    }

    public ArrayList<DefaultTableModel> getDiag2Tables() {
        return Diag2Tables;
    }
     
}
