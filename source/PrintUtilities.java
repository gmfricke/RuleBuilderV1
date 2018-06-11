/*
 * PrintUtilities.java
 *
 * Created on January 30, 2006, 11:49 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author Matthew Fricke on Jan 30th 2006
 */

import java.awt.*;
import javax.swing.*;
import java.awt.print.*;
 
public class PrintUtilities implements Printable 
{
    transient protected boolean debug_statements = true;
  private Component componentToBePrinted;

  public static void printComponent(Component c) 
  {
    new PrintUtilities(c).print();
  }
  
  public PrintUtilities(Component componentToBePrinted) 
  {
    this.componentToBePrinted = componentToBePrinted;
  }
  
  public void print() 
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable(this);
    if (printJob.printDialog())
      try 
      {
        printJob.print();
      }
      catch(PrinterException pe) 
      {
        if (debug_statements) System.out.println("Error printing: " + pe);
      }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) 
  {
    if (pageIndex > 0) 
    {
      return(NO_SUCH_PAGE);
    } 
    else 
    {
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      enableDoubleBuffering(componentToBePrinted);
      return(PAGE_EXISTS);
    }
  }

  public static void disableDoubleBuffering(Component c) 
  {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  public static void enableDoubleBuffering(Component c) 
  {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
}

