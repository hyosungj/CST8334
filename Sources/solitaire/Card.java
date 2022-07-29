package solitaire;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Card extends JPanel{
		// Members
		public int value;
		public Suit suit;
		private BufferedImage image;
		private BufferedImage backImage;
		boolean isReversed;
		Point positionOffset;
		
		public enum Suit {
			Spades(1, false),
			Hearts(2, true),
			Diamonds(3, true),
			Clubs(4, false);
			
			// Note below is suit value, (as seen above, not card value)
			public int suitValue;
			public boolean isRed;
			
			private Suit(int suitValue, boolean isRed) {
				this.suitValue = suitValue;
				this.isRed = isRed;
			}
		};
		
		// Conversion, to read card images from file.
		public static String valueString(int value) {
								
			if(value == 11) return "J";
			if(value == 12) return "Q";
			if(value == 13) return "K";
			if(value == 1) return "A";
			
			// Value between 2 and 10
			return Integer.toString(value);
		}

		// To read card images from file
		// TODO: perhaps by renaming our card images, we may be able to write more efficient card image finding code.
		public String toString() {
			return valueString(value) + " of " + suit.name();
		}
			
		public Card(int value, Suit suit) {
			this.value = value;
			this.suit = suit;		
			isReversed = false;
			
			try {
				// Load the image for the current file
				URL url = getClass().getResource("../images/cards/" + this.toString() + ".png");
				image = ImageIO.read(url);
				
				// Load the backimage
				url = getClass().getResource("../images/cards/back.png");
				backImage = ImageIO.read(url);
				
				setBounds(0, 0, image.getWidth(), image.getHeight());
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			// HJ: Unclear what below is doing, may be setting Java swing properties of the card, including its size)
			positionOffset = new Point(0,0);
			setSize(new Dimension(100, 145));
			setOpaque(false);
		}
		
		// Card Face Down
		public void hide() {
			isReversed = true;
		}
		
		// Card Face Up
		public void show() {
			isReversed = false;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			BufferedImage img = image;
			if(isReversed) img = backImage;

			g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
		}
	
}
