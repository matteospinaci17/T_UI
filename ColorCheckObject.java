import javax.swing.table.DefaultTableModel;

public class ColorCheckObject{

    String tabName;
    Integer[] colorCodes;

    public ColorCheckObject(String tabName,String[][] Matrix) {
        this.tabName = tabName;
        Integer nRows = Matrix.length;
        colorCodes = new Integer[nRows];
        for (int i = 0; i < colorCodes.length; i++) {
            colorCodes[i] = 0;
        }
    }

    public ColorCheckObject(String tabName2, DefaultTableModel defaultTableModel) {
	}

	public void addColorCode(Integer value, Integer position) {
        colorCodes[position] = value;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public Integer[] getColorCodes() {
        return colorCodes;
    }

    public void setColorCodes(Integer[] colorCodes) {
        this.colorCodes = colorCodes;
    }

    

}