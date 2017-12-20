import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.util.*;

/**  This class represents the painting panel and implements all relevant
 * functionality. */
public class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener  {
    /** This is useful for creating custom cursors. */
    private static Toolkit tk= Toolkit.getDefaultToolkit();

    private final Color defaultForegroundColor= Color.BLACK; // Default foreground color

    private BufferedImage img; // The image.
    private int width;  // width of the image
    private int height;  // height of the image

    private PaintGUI window; // main window of the program

    private Tool activeTool; // the active tool.
    private int toolSize; // size of the tool.
    private double half; // half the toolSize (rounded, e.g. 1.5 is rounded to 2)

    private Point2D.Double mousePos;     // Position of mouse, always
    private Point2D.Double mousePosPrev; // Previous mouse position (used to interpolate)

    // State for LINE drawing. False means that no LINE is being drawn.
    // True means: the LINE tool is active and the first press has been made.
    // If it is true, firstPoint describes the point of the first press.
    private boolean pointPressed;
    private Point2D.Double firstPoint;

    // State for CIRCLE drawing. False means that no CIRCLE is being drawn.
    // True means: the CIRCLE tool is active and the first press has been made.
    // If it is true, center describes the point of the first press.
    private boolean centerPressed;  
    private Point2D.Double center;

    private Color foreColor; // Foreground color (used for drawing).
    private Color backColor; // Background color (used for erasing).

    /** Random generator for airbrush. */
    private Random random= new Random(System.currentTimeMillis());

    /** Constructor: a new drawing panel for application window of
     * size(w, h), background color bckColor, and tool size toolSize. */
    public DrawingPanel(PaintGUI window, int w, int h, Color bckColor, int toolSize) {
        this.window= window;
        width= w;
        height= h;
        setToolSize(toolSize);

        // Create image with background color bckColor
        img= new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d= (Graphics2D) img.getGraphics();
        g2d.setColor(bckColor);
        System.out.println("DrawingPanel. mousePos: " + mousePos);
        g2d.fillRect(0, 0, w, h);  //mousePos.distance(center);

        foreColor= defaultForegroundColor;
        backColor= bckColor;

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /** Set the foreground color to c.
     * Throw an IllegalArgumentException if c is null */
    public void setForeGroundColor(Color c) {
        if (c == null) throw new IllegalArgumentException();

        foreColor= c;

        if (activeTool == Tool.LINE  &&  pointPressed) {
            repaint();
        }
        if (activeTool == Tool.CIRCLE  &&  centerPressed) {
            repaint();
        }
    }

    /** Set the background color to c.
     * Throw an IllegalArgumentException if c is null */
    public void setBackGroundColor(Color c) {
        if (c == null) throw new IllegalArgumentException();
        backColor= c;
    }

    /** return the Foreground color.  */
    public Color foreGroundColor() {
        return foreColor;
    }

    /** Return the Background color. */
    public Color backGroundColor() {
        return backColor;
    }

    /** Return the image. */
    public BufferedImage getImg() {
        return img;
    }

    /** Return the tool size. */
    public int getToolSize() {
        return toolSize;
    }

    /** Set the tool size to v.
     * Throw an IllegalArgumentException if v < 0. */
    public void setToolSize(int v) {
        if (v < 0)
            throw new IllegalArgumentException("setToolSize: v < 0");
        toolSize= v;
        half= (toolSize+1) / 2.0;
    }

    /** Create new blank image of width w and height h with
     * background color c. */
    public void newBlankImage(int w, int h, Color c) {
        width= w;
        height= h;

        // reset line/circle state
        pointPressed= false;
        centerPressed= false;

        img= new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d= (Graphics2D) img.getGraphics();
        g2d.setColor(c);
        g2d.fillRect(0, 0, w, h);

        repaint();
        revalidate();
    }

    /** Change the image to img. */
    public void newImage(BufferedImage img) {
        System.out.println("newImage");

        // reset line/circle state
        pointPressed= false;
        centerPressed= false;

        width= img.getWidth();
        height= img.getHeight();
        this.img= img;

        repaint();
        revalidate();
    }


    /** Return the dimension of this image. */
    @Override public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    /** Paint this component using g. */
    @Override public void paintComponent(Graphics g) {
        System.out.println("Paint drawing pane.");

        super.paintComponent(g);
        Graphics2D g2d= (Graphics2D) g;

        // Draw a border around the image.
        int z= 0;
        for (int i= 0; i<5; i++) {
            Color c= new Color(z,z,z);
            g2d.setColor(c);
            g2d.drawLine(0, height+i, width+i, height+i);
            g2d.drawLine(width+i, 0, width+i, height+i);
            z += 63;
        }

        g2d.drawImage(img, 0, 0, null);

        // TODO: #15. Implement me!
        // If the active tool is the LINE and the first point has been pressed,,
        // draw the line on g2d using the foreColor and toolSize.
        if (activeTool == Tool.LINE && pointPressed == true) {
        		g2d.setColor(foreColor);
        		g2d.setStroke(new BasicStroke(toolSize));
        		g2d.draw(new Line2D.Double(firstPoint.x, firstPoint.y, mousePos.x, mousePos.y));
        }


        // TODO: #17. Implement me!
        // If the active tool is the Circle and the center has been pressed,
        // draw the circle on g2d using the foreColor and toolSize.
        if (activeTool == Tool.CIRCLE && centerPressed == true) {
        		g2d.setColor(foreColor);
        		g2d.setStroke(new BasicStroke(toolSize));
        		double rad = Math.sqrt(Math.pow(center.x - mousePos.x, 2) + Math.pow(center.y - mousePos.y, 2));
        		g2d.draw(new Ellipse2D.Double(center.x - rad, center.y - rad, rad*2 , rad*2));
        }

    }

    /** Return the active tool. */
    public Tool getActiveTool() {
        return activeTool;
    }

    /** Change cursor to point (x, y), with image given by im */
    public void setActiveTool(int x, int y, String im) {
        Point hotspot= new Point(x, y);
        Image cursorImage= tk.getImage(im);
        Cursor cursor= tk.createCustomCursor(cursorImage, hotspot, "Custom Cursor");
        setCursor(cursor);
    }

    /** Set the active tool (and cursor) to t. */
    public void setActiveTool(Tool t) {
        // reset line/circle state
        pointPressed= false;
        centerPressed= false;

        repaint();

        switch(t) {
            case PENCIL:
                setActiveTool(2, 30, "pencil-cursor.png");
                break;
            case ERASER:
                setActiveTool(5, 27, "eraser-cursor.png"); 
                break;
            case COLOR_PICKER:
                setActiveTool(9, 23, "picker-cursor.png");
                break;
            case AIRBRUSH:
                setActiveTool(1, 25, "airbrush-cursor.png");
                break;
            case LINE:
                setActiveTool(0, 0, "line-cursor.png");
                break;
            case CIRCLE:
                setActiveTool(16, 16, "circle-cursor.png");
                break;
            default:System.err.println("setActiveTool " + t);
        }

        activeTool= t;		
    }

    @Override public void mouseClicked(MouseEvent e) {
        // Nothing to do here.
    }

    @Override public void mouseEntered(MouseEvent e) {
        // Nothing to do here.
    }

    /** Update the position of the mouse to the position given by e. */
    private void updateMousePosition(MouseEvent e) {
        int x= e.getX();
        int y= e.getY();
        // center of pixel
        mousePos= new Point2D.Double(x+0.5, y+0.5);
    }

    /** Update the mouse position to the coordinates given by e
     *  and make the position appear in the GUI (it's given by label
     *  PaintGUI.mousePositionLabel) */
    @Override public void mouseMoved(MouseEvent e) {
        updateMousePosition(e);
        // TODO: #01 Implement me! Make the position appear in the GUI.
        window.setMousePosition(e.getX(), e.getY());

        // TODO #17. Implement me!
        // If the active tool is the Line or Circle and the first mouse 
        // press has been recognized,  repaint().
        if (pointPressed == true) {
        		repaint();
        }
        	if (centerPressed == true) {
        		repaint();
        	}
    }

    /** Process the press of the mouse, given by e. */
    @Override public void mousePressed(MouseEvent e) {
        updateMousePosition(e);
        System.out.println("mousePressed: " + mousePos + ", active tool: " + getActiveTool());

        Graphics2D g2d= (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // anti-aliasing

        if (activeTool == Tool.PENCIL) {
            System.out.println("mousePressed: pencil");

            // TODO: #08. Implement me!
            // Draw a square of size (toolSize x toolSize) filled with the current
            // foreground color. Its center should be at the position of the mouse.
            Shape square = new Rectangle2D.Double(mousePos.x - half, mousePos.y - half, toolSize, toolSize);
            g2d.draw(square);
            g2d.setColor(foreColor);
            g2d.fill(square);
            window.setImageUnsaved();
            repaint();
            
        }
        else if (activeTool == Tool.ERASER) {
            System.out.println("mousePressed: eraser");

            // TODO: #09. Implement me!
            // Draw a square of size (toolSize x toolSize) filled with the current
            // background color. Its center should be at the position of the mouse.
            Shape square = new Rectangle2D.Double(mousePos.x - half, mousePos.y - half, toolSize, toolSize);
            g2d.draw(square);
            g2d.setColor(backColor);
            g2d.fill(square);
            window.setImageUnsaved();
            repaint();

        }
        else if (activeTool == Tool.COLOR_PICKER) {
            System.out.println("mousePressed: pick color");
            //TODO Nothing to do. We give you this one.
            // Pick the color of the pixel the mouse is currently over.            
            // Left mouse button pressed: set the new foreground color
            // Right mouse button pressed: set the new background color
            pickColor(e);
        }
        else if (activeTool == Tool.LINE){
            System.out.println("mousePressed: line");

            // TODO: #14. Implement me!
            // If no mouse press has been made yet with this tool active,
            // this IS the first mouse press; record it.
            // If one has already been made, this is the second mouse press;
            // draw the line.
            if (pointPressed == false) {
            		firstPoint = mousePos;
            		pointPressed = true;
            }
            else {
            		g2d.setColor(foreColor);
            		g2d.setStroke(new BasicStroke(toolSize));
            		g2d.draw(new Line2D.Double(firstPoint.x, firstPoint.y, mousePos.x, mousePos.y));
            		window.setImageUnsaved();
            		repaint();
            		pointPressed = false;
            }

        }
        else if (activeTool == Tool.CIRCLE){
            System.out.println("mousePressed: circle");

            // TODO: #16. Implement me!
            // If no mouse press has been made yet with this tool active,
            // this IS the first mouse press; record it.
            // If one has already been made, this is the second mouse press;
            // draw the circle.
            if (centerPressed == false) {
            		center = mousePos;
            		centerPressed = true;
            }
            else {
            		g2d.setColor(foreColor);
            		g2d.setStroke(new BasicStroke(toolSize));
            		double rad = Math.sqrt(Math.pow((center.x - mousePos.x), 2) + 
            				Math.pow((center.y - mousePos.y), 2));
            		g2d.draw(new Ellipse2D.Double(center.x - rad, center.y - rad, rad*2 , rad*2));
            		window.setImageUnsaved();
            		repaint();
            		centerPressed = false;
            }
            
        }
        else if (activeTool == Tool.AIRBRUSH) {
            System.out.println("mousePressed: airbrush");

            // TODO: #10 Airbrush with the current foreground color in
            // an area of size (toolSize x toolSize) centered at the current 
            // position of the mouse. Or, do something better if you want.
            int area = (toolSize * toolSize);
            for (int i = 0; i < area; i++) {
            		double x = (random.nextDouble()*toolSize) + (mousePos.x - half);
            		double y = (random.nextDouble()*toolSize) + (mousePos.y - half);
            		Shape pix = new Rectangle2D.Double(x, y, 1, 1);
            		g2d.draw(pix);
            		g2d.setColor(foreColor);
            		g2d.fill(pix);	
            }
            window.setImageUnsaved();
            repaint();
        }
        else {
            System.err.println("Unknown tool: " + activeTool);
        }

        // set prevMousePos
        mousePosPrev= mousePos;
    }

    @Override public void mouseExited(MouseEvent e) {
        // Nothing to do here.
    }

    @Override public void mouseReleased(MouseEvent e) {
        // End of drawing, reset prevMousePos.
        mousePosPrev= null;
    }

    /** Process the dragging of the mouse given by e. */
    @Override public void mouseDragged(MouseEvent e) {
        updateMousePosition(e);
        System.out.println("mouseDragged: " + mousePos + ", active tool: " + activeTool);		

        Graphics2D g2d= (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (activeTool == Tool.PENCIL) {
            // TODO: #11 Implement me!
            // Draw a line with color foreColor and stroke toolSize from
            // mouse position mousePosPrev to position mousePos.
        		g2d.setColor(foreColor);
        		g2d.setStroke(new BasicStroke(toolSize));
        		g2d.draw(new Line2D.Double(mousePosPrev.x, mousePosPrev.y, mousePos.x, mousePos.y));
        		window.repaint();

        }
        else if (activeTool == Tool.ERASER) {
            // TODO: #12 Implement me
            // Draw a line with color backColor and stroke toolSize from
            // mouse position mousePosPrev to position mousePos.
        		g2d.setColor(backColor);
        		g2d.setStroke(new BasicStroke(toolSize));
        		g2d.draw(new Line2D.Double(mousePosPrev.x, mousePosPrev.y, mousePos.x, mousePos.y));
        		window.repaint();

        }
        else if (activeTool == Tool.COLOR_PICKER) {
            // Nothing to do here.
        }
        else if (activeTool == Tool.AIRBRUSH) {
            // TODO: #13 Implement me!
            // Airbrush with the current foreground color in an area of size
            // (toolSize x toolSize) centered at the current position of the mouse.
        	int area = (toolSize * toolSize);
            for (int i=0; i < area; i++) {
            		double x = random.nextDouble()*toolSize + (mousePos.x - half);
            		double y = random.nextDouble()*toolSize + (mousePos.y - half);
            		Shape pix = new Rectangle2D.Double(x, y, 1, 1);
            		g2d.draw(pix);
            		g2d.setColor(foreColor);
            		g2d.fill(pix);	
            }
            window.setImageUnsaved();
            window.repaint();

        }
        else {
            System.err.println("active tool: " + activeTool);
        }

        // update prevMousePos
        mousePosPrev= mousePos;
    }

    /** Pick the color of the pixel of img given by e. 
     * Left mouse button pressed: use color as new foreground color.
     * Right mouse button pressed: use color as new background color. */
    private void pickColor(MouseEvent e) {
        int rgb= img.getRGB(e.getX(),e.getY());
        Color pickedColor= new Color(rgb);
        int b= e.getButton();
        if (b == MouseEvent.BUTTON1) {
            setForeGroundColor(pickedColor);   // Left button clicked
            window.updateForeColor();
        } else if (b == MouseEvent.BUTTON3) {
            setBackGroundColor(pickedColor);  // Right button clicked
            window.updateBackColor();
        }
    }




}
