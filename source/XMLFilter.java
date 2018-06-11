// This class filters file types for *.bng in the FileChooser

import javax.swing.*;
import java.io.*;

public class XMLFilter extends javax.swing.filechooser.FileFilter
{

    /**
     *
     * @param f
     * @return
     */    
  public boolean accept(File f)
  {
    return f.getName().toLowerCase().endsWith(".xml")
          || f.isDirectory();
  }

  /**
   *
   * @return
   */  
  public String getDescription()
  {
    return "Extensible Markup Language (*.xml)";
  }
}
