package frc.robot;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class testUnicode {

    public static void main(String[] args) {
        try{
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./unicodeTest.txt"), "UTF-8"));

            String infoString = new String();
            for(int i = 0x0020; i < 0xFFFF; i++){
                infoString = "";
                infoString += String.format("0x%04X: %c ", i, (char)i);
    
                if(Character.isJavaIdentifierPart(i)){
                    infoString += " ID_PART ";
                } else {
                    infoString += "         ";
                }
    
                if(Character.isJavaIdentifierStart(i)){
                    infoString += " ID_START ";
                } else {
                    infoString += "          ";
                }

                infoString += "\n";
                out.write(infoString);
            }
            out.close();

        } catch (Exception e) {
            System.out.println("What, you actually expected reasonable error handling?");
            System.out.println(e);
        }
    }
}