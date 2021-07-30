package frc.lib.Webserver2.DashboardConfig;

public class CameraConfig extends WidgetConfig {
    
    String streamURL = "";

    final double nominalWidth  = 40;
    final double nominalHeight = 30;

    @Override
    public String getHTML(){
        double width = nominalWidth * sizeScaleFactor;
        double height = nominalHeight * sizeScaleFactor;
        return genHtmlDeclaration(height, width);
    }

    @Override
    public String getJSDeclaration(){
        return String.format("var widget%d = new Camera('widget%d', '%s', '%s');", idx, idx, name, streamURL);
    }
    
}
