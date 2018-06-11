import java.io.*;
import javax.swing.*;

class ReaderThread extends Thread {
            PipedInputStream pi;
            JTextArea journal_pane;
            
            ReaderThread(PipedInputStream pi, JTextArea jp ) 
            {
                journal_pane = jp;
                this.pi = pi;
            }
    
            public void run() {
                final byte[] buf = new byte[1024];
                try {
                    while (true) {
                        final int len = pi.read(buf);
                        if (len == -1) {
                            break;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                journal_pane.append(new String(buf, 0, len));
    
                                // Make sure the last line is always visible
                                journal_pane.setCaretPosition(journal_pane.getDocument().getLength());
    
                                // Keep the text area down to a certain character size
                                int idealSize = 1000;
                                int maxExcess = 500;
                                int excess = journal_pane.getDocument().getLength() - idealSize;
                                if (excess >= maxExcess) {
                                    journal_pane.replaceRange("", 0, excess);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                }
            }
        }