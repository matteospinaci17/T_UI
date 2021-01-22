import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ColorCheck {

    private ArrayList<ColorCheckObject> tabsAndTheirColors = new ArrayList<ColorCheckObject>();
    ArrayList<JPanel> Panels = new ArrayList<JPanel>();
    JTabbedPane JTP = new JTabbedPane();
    private int isJTPSet = 0;;

    public ColorCheck() {}

    public void add(ColorCheckObject CCO) {
        tabsAndTheirColors.add(CCO);
    }

    public ArrayList<ColorCheckObject> getTabsAndTheirColors() {
        return tabsAndTheirColors;
    }

    public void setTabsAndTheirColors(ArrayList<ColorCheckObject> tabsAndTheirColors) {
        this.tabsAndTheirColors = tabsAndTheirColors;
    }

	public void checkColors(String tabName, JTabbedPane jTP) {
        int tabColor = 0;
        for(ColorCheckObject CCO : tabsAndTheirColors) {
            if(CCO.getTabName().equals(tabName)) {
                for(int i = 0; i < CCO.getColorCodes().length; i++) {
                    if(tabColor < CCO.getColorCodes()[i]) {
                        tabColor = CCO.getColorCodes()[i];
                    }
                }
            }
        }
        setTabColor(tabName, tabColor, jTP);
	}

    private void setTabColor(String tabName, int tabColor, JTabbedPane jTP) {
        if(!Panels.isEmpty()) {
            Integer index = 0;
            for(JPanel panel : Panels) {
                if(panel.getName().equals(tabName)) {
                    if(isJTPSet == 1){
                        if(tabColor == 3) {
                            JTP.setBackgroundAt(index, Color.RED);
                        }
                        else if(tabColor == 2) {
                            JTP.setBackgroundAt(index, Color.YELLOW);
                        }
                        else if(tabColor == 1) {
                            JTP.setBackgroundAt(index, Color.CYAN);           
                        }
                        else {
                            JTP.setBackgroundAt(index, Color.WHITE);
                        }
                    }
                }
                index++;
            }
        }
    }

    public ArrayList<JPanel> getPanels() {
        return Panels;
    }

    public void setJTP(JTabbedPane JTP) {
        isJTPSet = 1;
        this.JTP = JTP;
    }

       
    
}
