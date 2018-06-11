/*
 * Launcher.java
 *
 * Created on December 28, 2005, 12:32 PM by Matthew Fricke
 *
 * This launcher checks the Java version before allowing RuleBuilder to run.
 * It is important that this Launcher be compiled in the earliest Java version
 * possible so that it can run under java versions not supported by RuleBuilder
 *
 */

import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.awt.*; // For older windowing
import java.lang.reflect.*; // For external class calls

/**
 *
 * @author  matthew
 */
public class Launcher 
{
 public static void main(String[] args) 
 {
     
     String java_version = null;  
     
        try {
            
            // Identify the program on the console
       
	    java_version = System.getProperty("java.version");
	    String os_name = System.getProperty("os.name");
            String os_version = System.getProperty("os.version");
            
	    System.out.println("You are using Java Virtual Machine (JVM) version " + java_version);
            System.out.println("on operating system " + os_name + " version " + os_version );

            
	    // Check that the JVM being used is recent enough (required for native drag and drop)
	    // Exit if the JVM version is too low
	    if (java_version.compareTo("1.5.0") < 0) 
            {
                    Frame frame = new Frame("More Recent Version of Java Required");
                    JOptionPane.showMessageDialog(frame, "RuleBuilder requires Java 1.5.0 or higher.\n" +
                    "An attempt was made to run RuleBuilder using Java " + java_version + ".\nPlease upgrade Java and try again.");
                    
                    
		    System.err.println("Error: RuleBuilder requires JVM version 1.5 or higher. Exiting.");
		    System.exit(1);
		}
            //else if (os_name.compareTo("Mac OS X") == 0 && os_version.compareTo("10.4") < 0 ) 
            //{
            //        Frame frame = new Frame("More Recent Version of the Apple OS required");
            //        JOptionPane.showMessageDialog(frame, "RuleBuilder requires OS X version 10.4 or higher.\n" +
            //        "An attempt was made to run RuleBuilder using " + os_name + " version " + os_version + ".\nPlease upgrade and try again.");
                    
                    
            //	    System.err.println("Error: RuleBuilder requires JVM version 1.4.2 or higher. Exiting.");
            //	    System.exit(1);
            //	}
	    else 
		{
		    System.out.println("This version of the JVM should work fine.");
		}	
            
            /*
            if ( !(os_name.compareTo("Mac OS X") == 0 || os_name.compareTo("Linux") == 0) )
            {
                Frame frame = new Frame("More Recent Version of the Apple OS required");
                JOptionPane.showMessageDialog(frame, "RuleBuilder currently requires Mac OS X or Linux to run correctly.\nSupport for MS Windows will be added soon.");
                System.exit(1);
            }
            */
            
            System.out.print("Launcher Args:");
            for ( int i = 0; i < args.length; i++ )
            {
                System.out.print(" " + args[i]);
            }
            System.out.println();
            
            String className = "RuleBuilder";
            String[] mainArgs = {"no_path"};
            if (args.length > 0) mainArgs[0] = args[0];
            Class cls = Class.forName(className);
            Method m = cls.getMethod("main", new Class[]{String[].class});
            m.invoke(null, new Object[]{mainArgs});
            
        } catch (UnsupportedClassVersionError e) 
        {
                    Frame frame = new Frame("More Recent Version of Java Required");
                    JOptionPane.showMessageDialog(frame, "RuleBuilder requires Java 1.5.0 or higher.\n" +
                    "An attempt was made to run RuleBuilder using Java " + java_version + ".\nPlease upgrade Java and try again.");
                    
                    
		    System.err.println("Error: RuleBuilder requires JVM version 1.5 or higher. Exiting.");
		    System.exit(1);
        } 
        catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        }
 }
}
