package crowdsim4;

import java.awt.*;
import java.awt.image.*;

import java.io.*;
import javax.imageio.*;

import javax.swing.JPanel;
import javax.swing.JFrame;

public class ShowMap {
	
	static class ImagePanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		BufferedImage img;
		
		ImagePanel(BufferedImage image) {
			img = image;
			this.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		}
		
		public void paint(Graphics g) {
			g.drawImage(img, 0, 0, null);
		}
	}

	public static void main(String[] args) {
		System.out.println("crowdsim3.Test");
		JFrame frame = new JFrame("Image");
		
		String imgStr = "map.png";
		
		BufferedImage image = null;
		
		try{
			image = ImageIO.read(new File(imgStr));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		ImagePanel panel = new ImagePanel(image);

		frame.getContentPane().add(panel);
		frame.pack();
		// frame.getContentPane().setPreferredSize(panel.getPreferredSize());
		frame.setVisible(true);
	}
}
