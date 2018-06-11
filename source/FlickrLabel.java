/*
 * FlickrText.java
 *
 * Created on December 17, 2005, 1:14 PM by Matthew
 */

/**
 *
 * @author  matthew
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // For keyboard interactions
import javax.swing.text.DefaultCaret; // For Caret
import java.util.*; // For iterator
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.io.*; // For file IO in objectWrite and Read
import javax.swing.event.*; // For document listener

// Have to make thse things extend widget... need to figure out keyboard input without
// using JComponent 
public class FlickrLabel extends Widget
{

        private class BlinkTimerListener implements ActionListener
        {
        
        BlinkTimerListener()
        {
        }
        
        public void actionPerformed( ActionEvent evt )
            {
                if ( editable )
                {
                if (cursor_visible)
                {
                    cursor_visible = false;
                    caret_color = Color.BLACK;
                }
                else
                {
                    cursor_visible = true;
                    caret_color = background_color;
                }
                
                containing_panel.repaint();
                blink_timer.restart();
                }
            }
    }

    private class DialogCloser extends WindowAdapter implements ActionListener 
    {
    
    public void actionPerformed(ActionEvent event) 
    {
        handleClose();
    }
    
    public void windowClosing(WindowEvent e)
    {
	handleClose();
    }
    
    private void handleClose()
    {
        String text = text_field.getText();
        
	if ( text.length() == 0 )
        {
            return;
        }
        
        contents = text;
        layout = new TextLayout( contents, font, frc );
        hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
        
        dialog.setVisible( false );
        dialog.dispose();
        
        getContainingPanel().repaint();
    }
  }
   
    private class TextFieldChangeListener implements DocumentListener 
    {
    
    public void changedUpdate(DocumentEvent e) 
    {
        handleChange();
    }
    
    public void insertUpdate(DocumentEvent e) 
    {
        handleChange();
    }
    
    public void removeUpdate(DocumentEvent e) 
    {
        handleChange();
    }
    
    private void handleChange()
    {
        if ( text_field.getText() != null )
        {
            if ( text_field.getText().length() != 0 )
            {
                done_button.setEnabled( true );
            }
            else
            {
                done_button.setEnabled( false );
            }
        }
        else
        {
            done_button.setEnabled( false );
        }
    }
  }
    
    public class KeyboardControl implements KeyListener 
    {
        KeyboardControl(){}
        
        public void keyPressed(KeyEvent evt) 
        {
            if (debug_statements) System.out.println("Key Press Detected in Flickr Text");
            
            
            int key = evt.getKeyCode();
            if (debug_statements) System.out.println("Key code: " + key );
            if ( key == KeyEvent.VK_BACK_SPACE ) 
            {
                TextHitInfo current_hit = hitInfo;
                removeCharacterAt( hitInfo.getInsertionIndex()-1 );
                
                hitInfo = layout.getNextLeftHit(hitInfo.getInsertionIndex());
                if ( hitInfo == null )
                {
                    hitInfo = current_hit;
                }
                
                if (contents.length() != 0 )
                {
                    layout = new TextLayout(contents,font,frc);
                }
                else
                {
                    layout = new TextLayout("...",font,frc);
                }
                
                
                containing_panel.repaint();
            }
            else if ( key == KeyEvent.VK_DELETE ) 
            {
                removeCharacterAt( hitInfo.getInsertionIndex() );
                if (contents.length() != 0 )
                {
                    layout = new TextLayout(contents,font,frc);
                }
                else
                {
                    layout = new TextLayout("...",font,frc);
                }
                
                containing_panel.repaint();
            }
            else if ( key == KeyEvent.VK_RIGHT ) 
            {
                TextHitInfo current_hit = hitInfo;
                hitInfo = layout.getNextRightHit(hitInfo.getInsertionIndex());
                if (hitInfo == null)
                hitInfo = current_hit;
                containing_panel.repaint();
                
                caret_position++;
                
                containing_panel.repaint();
            }
            else if ( key == KeyEvent.VK_LEFT ) 
            {
                TextHitInfo current_hit = hitInfo;
                hitInfo = layout.getNextLeftHit(hitInfo.getInsertionIndex());
                if (hitInfo == null)
            	hitInfo = current_hit;
          	containing_panel.repaint();
                
                caret_position--;
                
                containing_panel.repaint();
            }
            
            
        }
        
        public void keyReleased(KeyEvent evt) {
            
        }
        
        public void keyTyped(KeyEvent evt) 
        {

                if (debug_statements) System.out.println("FlickrLabel: Key char: " + evt.getKeyChar() );
                
                // Make sure the char is alphanumeric
                
                if ( evt.getKeyChar() >= 'a' && evt.getKeyChar() <= 'z' 
	             || evt.getKeyChar() >= 'A' && evt.getKeyChar() <= 'Z'
                     || evt.getKeyChar() >= '0' && evt.getKeyChar() <= '9'
                     || evt.getKeyChar() == '_' )
                {
                    if ( layout == null )
                    {
                        addCharacterAt( 0,  evt.getKeyChar() );
                        layout = new TextLayout(contents,font,frc);
                        hitInfo = layout.getNextRightHit(0);
                        containing_panel.repaint();
                    }
                    else //if ( contents.length() != 0 )
                    { 
                        addCharacterAt( hitInfo.getCharIndex(), evt.getKeyChar() );
                        layout = new TextLayout(contents,font,frc);
                        hitInfo = layout.getNextRightHit(hitInfo.getInsertionIndex());
                        if (hitInfo == null)
                        hitInfo = layout.getNextLeftHit(1);
                        containing_panel.repaint();
                    }
                }
        }
    }
    
    
    class MouseHandler extends MouseAdapter 
    {
        
        
    public void mouseClicked(MouseEvent e) 
    {
      if ( !on ) return;  
        
      caretColor = caret_color;
      hit1 = getHitLocation(e.getX(), e.getY());
      hit2 = hit1;
      containing_panel.repaint();
    }

    public void mousePressed(MouseEvent e) 
    {
      if ( !on ) return;  
        
      caretColor = caret_color;
      hit1 = getHitLocation(e.getX(), e.getY());
      hit2 = hit1;
      containing_panel.repaint();
    }

    public void mouseReleased(MouseEvent e) 
    {
      if ( !on ) return;  
        
      hit2 = getHitLocation(e.getX(), e.getY());
      containing_panel.repaint();
    }
  }

  class MouseMotionHandler extends MouseMotionAdapter 
  {
        public void mouseExited(MouseEvent e)
        {
            if (debug_statements) System.out.println("Mouse left FlickrLabel");
        }
        
        public void mouseEntered(MouseEvent e)
        {
            if (debug_statements) System.out.println("Mouse entered FlickrLabel");
            // get input focus for the keyboard
            //requestFocus();
        }
      
    public void mouseDragged(MouseEvent e) {
      caretColor = containing_panel.getBackground();
      hit2 = getHitLocation(e.getX(), e.getY());
      containing_panel.repaint();
    }
  }
    
    // Serialization explicit version
    private static final long serialVersionUID = 1;
  
    private String contents;
    
    int caret_position = 0;
    private int x;
    private int y;
    private boolean selected = false;
    private boolean on = true;
    transient private KeyboardControl keyboard_control = new KeyboardControl();
    transient private FontRenderContext frc = new FontRenderContext(null, false, false);
    transient private TextLayout layout;
    private int hit1, hit2;
    transient private Color caretColor;
    transient private TextHitInfo hitInfo;
    transient private Rectangle2D rect;
    private float rx, ry, rw, rh;
    transient private javax.swing.Timer blink_timer;
    transient private BlinkTimerListener blink_timer_listener = new BlinkTimerListener();
    transient private Color text_color = Color.BLACK;
    transient private Color caret_color = Color.BLACK;
    private boolean cursor_visible = true;
    transient private Color background_color = new Color( 0, 0, 255, 5); // Light Blue
    transient private JTextField text_field;
    transient private JButton done_button;
    transient private JDialog dialog;
    private Widget owner;
    
    private boolean editable;
    
    /** Creates a new instance of FlickrText */
    public FlickrLabel(String label, Widget owner, int x, int y, WidgetPanel panel, boolean on) 
    { 
        this.on = on;
        containing_panel = panel;
        this.setOwner(owner);
        
        setY( x );
        setX( y );
        
      // if ( label.length() == 0 )
      // {
      //      label = " ";
      // }
        
        contents = label;
        if ( 0 != contents.length() )
        {
            layout = new TextLayout(contents, font, frc);
        
            hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
        }
        
        blink_timer = new javax.swing.Timer(750, blink_timer_listener );
        
    }
    
    KeyboardControl getKeyboardListener()
    {
        return keyboard_control;
    }
    
    public boolean contains( int mouse_x, int mouse_y )
    {
        return mouse_x > getX() && mouse_x < getX() + getWidth() && mouse_y > getY() && mouse_y < getY() + getHeight();       
    }
    
    public void setX( int x )
    {
        this.x = x;
    }
    
    public void setY( int y)
    {
        this.y = y;

    }
    
    public int getWidth()
    {
        int value = 0;
        if ( layout == null && contents.length() != 0)
        {
            layout = new TextLayout(contents,font,frc);
            
        }

        value = (int)layout.getBounds().getWidth();
        
        return value;
    }
    
    public int getHeight()
    {
        int value = 0;
        if ( layout == null && contents.length() != 0)
        {
            layout = new TextLayout(contents,font,frc);
            
        }

        value = (int)layout.getBounds().getHeight();
        return value;
    }
    
    
    public int getHitLocation(int mouseX, int mouseY) {
    hitInfo = layout.hitTestChar(mouseX, mouseY, rect);
    return hitInfo.getInsertionIndex();
  }
    
    public void setEditable( boolean editable )
    {
         
        if ( editable )
        {
            // Get keyboard focus
            containing_panel.removeKeyListener( containing_panel.getKeyboardListener() );
            containing_panel.addKeyListener( keyboard_control );
            blink_timer.start();
        }
        else
        { 
            if ( contents.length() == 0 )
            {
                displaySetLabelDialog();
            }
            
            // Give keyboard focus back
            containing_panel.removeKeyListener( keyboard_control );
            containing_panel.addKeyListener( containing_panel.getKeyboardListener() );
            blink_timer.stop();
            
            owner.setLabel( contents );
        }
        
        this.editable = editable;
    }
    
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
            //if (debug_statements) System.out.println("Displaying FlickrLabel \"" + getString() + "\" at " + getX() + ", " + getY() + " of length " + getString().length() );
            if ( !on ) return;
        
            if ( editable )
            {
                int bx = getX() - 5;
                int by = getY() - 5;
                int bwidth = getWidth() + 10;
                int bheight = getHeight() + 10;
                
                g2d.drawRect( bx, by, bwidth, bheight );
                g2d.setColor( background_color );
                g2d.fillRect( bx, by, bwidth, bheight );
                
                
                Shape[] carets = layout.getCaretShapes(hitInfo.getInsertionIndex());
            
                for (int i = 0; i < carets.length; i++) 
                {
                    if (carets[i] != null)
                    {
                        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
                        Shape shape = at.createTransformedShape(carets[i]);
                        g2d.setColor( caret_color );
                        //g2.setStroke(caretStrokes[i]);
                        g2d.draw(shape);
                        g2d.setColor( text_color );
                    }
                }
            }
            
            if ( isSelected() )
            {
                g2d.setColor( Color.BLUE );
            }
            else
            {
                g2d.setColor( text_color );
            }
            
            layout.draw( g2d, x, y );
            
            g2d.setColor( g2d.getBackground() );

    }
    
    public void removeCharacterAt(int pos) 
    {
        
        String new_string = new String();
                for ( int i = 0; i < contents.length(); i++ )
                {
                    if ( i == pos )
                    {
                        continue;
                    }
                    new_string += contents.charAt( i );
                }
                
                
        contents = new_string;
        
    }
    
    public void addCharacterAt(int pos, char c )
    {
        
                if (debug_statements) System.out.println( "Adding \'" + c + "\' to position " + pos + " in \"" + contents + "\"" );
        
                String new_string = new String();
                if ( contents.length() == 0 || pos >= contents.length() )
                {
                    contents += c;
                    return;
                }
                
                for ( int i = 0; i < contents.length(); i++ )
                {
                    if ( i == pos )
                    {
                        new_string += c;
                    }
                    new_string += contents.charAt( i );
                }
                
                contents = new_string;
                
    }
   
    public int getX() 
    {
        return x+(int)layout.getBounds().getX();
    }
    
    public int getY()
    {
        
        return y+(int)layout.getBounds().getY();
    }
    
    public String getString() 
    {
        return contents;
    }
    
    public void setString( String string ) 
    {
        setString( string, false );
    }
    
    public void setString( String string, boolean peer ) 
    {
        contents = string;
         if ( 0 != contents.length() )
        {
            layout = new TextLayout(contents, font, frc);
        
            //hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
        }
        
        if ( !peer )
        {
            Iterator<Widget> peer_itr = getPeers().iterator();
            while ( peer_itr.hasNext() )
            {
                ((FlickrLabel)peer_itr.next()).setString( string, true );
            }
        }
        
    }
    
    // Lots of transient data members to recreate after a clone operation
   private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
    stream.defaultReadObject();
    font = new Font("Arial", Font.PLAIN, 12);
    frc = new FontRenderContext(null, false, false);
    
    text_color = Color.BLACK;
    caret_color = Color.BLACK;
    cursor_visible = true;
    background_color = new Color( 0, 0, 255, 5); // Light Blue
    keyboard_control = new KeyboardControl();
    
    if (contents.length() != 0)
    {
        layout = new TextLayout(contents,font,frc);
        hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
    }
        
        blink_timer_listener = new BlinkTimerListener();
        blink_timer = new javax.swing.Timer(750, blink_timer_listener );
        //blink_timer.start();
   
   }
    
   public void setContainingPanel(WidgetPanel panel) 
   {
       // This code causes the label to be added to the containing panel
       // whenever it is cloned decoupling it from the object being labeled
       //if ( containing_panel != null )
       //{
       //    containing_panel.removeFlickrLabel( this );
       //}
       
       this.containing_panel = panel;
       //containing_panel.addFlickrLabel( this );
    }
   
   public void displaySetLabelDialog() 
   {
           
            JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            dialog = new JDialog( owner, true );
            dialog.setTitle( "Enter Label" );
            dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            dialog.addWindowListener( new DialogCloser() );
            
            Container content = dialog.getContentPane();
            
            JPanel button_panel = new JPanel();
            JPanel label_panel = new JPanel();
            
            done_button = new JButton("Done");
            done_button.addActionListener( new DialogCloser() );
            done_button.setEnabled( false );
            button_panel.add( done_button );
            
            text_field = new JTextField(20);
            label_panel.add( text_field );
            text_field.getDocument().addDocumentListener( new TextFieldChangeListener() );
            
            content.add(label_panel, BorderLayout.CENTER);
            content.add(button_panel, BorderLayout.SOUTH);
            
            dialog.pack();
            
            int dialog_height = dialog.getHeight();
            int dialog_width = dialog.getWidth();
            
            dialog.setLocation( 200, 100 );  
            
            dialog.setVisible(true);
           
   }

    public void setFont(Font font) 
    {
        this.font = font;
        if (contents.length() != 0) layout = new TextLayout(contents, font, frc);
    }

    public void setOn() 
    {
        on = true;
    }

    public void setOff() 
    {
        on = false;
    }

    private int label_x_offset;

    private int label_y_offset;

    public int getLabelXOffset() {
        return label_x_offset;
    }

    public void setLabelXOffset(int label_x_offset) {
        this.label_x_offset = label_x_offset;
    }

    public int getLabelYOffset() {
        return label_y_offset;
    }

    public void setLabelYOffset(int label_y_offset) {
        this.label_y_offset = label_y_offset;
    }

    public Widget getOwner() {
        return owner;
    }

    public void setOwner(Widget owner) {
        this.owner = owner;
    }

    

    
}
