package frc.lib.Webserver2.DashboardConfig;

public class LineGaugeConfig extends WidgetConfig {
    
    double minRange;
    double maxRange;
    double minAcceptable;
    double maxAcceptable;


    final double nominalWidth  = 20;
    final double nominalHeight = 5;

    @Override
    public String getHTML(){
        double width = nominalWidth * sizeScaleFactor;
        double height = nominalHeight * sizeScaleFactor;
        return genHtmlDeclaration(height, width);
    }

    @Override
    public String getJSDeclaration(){
        String retStr = String.format("var widget%d = new LineGauge('widget%d', '%s', %f,%f,%f,%f);\n", idx, idx, name, minRange, maxRange, minAcceptable, maxAcceptable);
        retStr += String.format("nt4Client.subscribePeriodic([\"%s\"], 0.05);", nt4TopicCurVal);
        return retStr;
    }

    @Override
    public String getJSSetData(){
        String retStr = "";
        retStr += "if(name == \"" + nt4TopicCurVal + "\"){ ";
        retStr += String.format("    widget%d.setVal(value);", idx);
        retStr += "}";
        return retStr;
    }

    @Override
    public String getJSUpdate() {
        return String.format("    widget%d.render();", idx);
    }

    
    @Override
    public String getJSSetNoData(){
        String retStr = "";
        retStr += String.format("    widget%d.reportNoData();", idx);
        return retStr;
    }
    
}
