package frc.lib.Webserver2.DashboardConfig;

public abstract class WidgetConfig {

    String name = "";
    public double xPos = 0.0;
    public double yPos = 0.0;
    public double sizeScaleFactor = 1.0;
    public int idx = 0;

    String nt4TopicCurVal = "";

    public String getHTML(){
        return "";
    }

    public String getJSDeclaration(){
        return "";
    }

    public String getJSUpdate() {
        return "";
    }

    public String getJSSetData() {
        return "";
    }

    public String getJSSetNoData() {
        return "";
    }

    public String getJSCallback() {
        return "";
    }
    

    String genHtmlDeclaration(double height, double width) {
        return "<div class=\"widgetBase\" style=\"top:" + Double.toString(yPos) + "%;left:" + Double.toString(xPos)
                + "%;height:" + Double.toString(height) + "vw;width:" + Double.toString(width) + "vw\" id=\"widget"
                + Integer.toString(idx) + "\"></div>";
    }

}
