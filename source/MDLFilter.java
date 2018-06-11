// This class filters file types for *.bng in the FileChooser

import javax.swing.*;
import java.io.*;

/**
 * BNGFiler is used by JFileChooser to filter files
 * by the file extension "bng."
 */
public class MDLFilter extends javax.swing.filechooser.FileFilter
{

    /**
     * Returns true if the file has the extension .bng or is
     * a directory. Used to filter files displayed by
     * JFileChooser.
     * @return Returns true if the file being considered
     * has the extension .bng or is a directory. Returns
     * false otherwise.
     * @param f The file being considered by accept.
     */    
   public boolean accept(File f)
  {
    return f.getName().toLowerCase().endsWith(".mol")
          || f.isDirectory();
  }

  /**
   * Describes the .bng file extension. Used by JFileChooser.
   * @return A string describing the .bng file extension.
   */  
  public String getDescription()
  {
    return "Molecular Design Limited molecule file (MDL) (*.mol)";
  }
}
