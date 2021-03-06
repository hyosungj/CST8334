package solitaire;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import solitaire.Pile.PileType;

public class GUI extends JFrame implements ActionListener, MouseListener,
										   MouseMotionListener {

		private JMenuBar menuBar;
		
		// Map all the GUI text
		Map<String, String> displayText;
		JPanel gameArea;
		JPanel columns;
		JPanel topColumns;
		JLayeredPane lp;
		Engine game;
		
		// Auxiliary elements to use while dragging
		Pile tempPile;
		Point mouseOffset;
		
		/**
		 * GUI class constructor
		 */
		public GUI (Engine game) {			
			this.game = game;
			
			// Initialize stuff
			createTextMap();
			
			
			// Window settings
			setTitle("CST8322 Group 2 Assignment 3 Release 2");
//			setSize(900, 700);
			setSize(900, 1000);
			
			try {
				setContentPane((new JPanelWithBackground("../images/background.jpg")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			setLayout(new BorderLayout());
			
			gameArea = new JPanel();
			gameArea.setOpaque(false);
			gameArea.setLayout(new BoxLayout(gameArea, BoxLayout.PAGE_AXIS));
			
			// Center the window
			setLocationRelativeTo(null);
			
			// Window close event
		    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		    
		    // Add GUI elements
			createTopMenu();
			
			// Flow layout to display multiple columns on the same row
			FlowLayout flow = new FlowLayout(FlowLayout.CENTER);
			flow.setAlignOnBaseline(true);
			
			// Add the columns panel
			columns = new JPanel();
			columns.setOpaque(false);
			columns.setLayout(flow);
			columns.setMinimumSize(new Dimension(200, 900));
			
			// Add the top columns panel
//			FlowLayout topFlow = new FlowLayout(FlowLayout.LEFT);
			FlowLayout topFlow = new FlowLayout(FlowLayout.CENTER);
			topFlow.setAlignOnBaseline(true);
			
			topColumns = new JPanel();
			topColumns.setOpaque(false);
			topColumns.setLayout(topFlow);
			
			
			gameArea.add(topColumns);
			gameArea.add(columns);
			
			//layers.add(dragLayer, JLayeredPane.DRAG_LAYER);
			add(gameArea);
			
			// Display the window
			lp = getLayeredPane();
			setVisible(true);
			
			// Auxiliarry elements
			mouseOffset = new Point(0, 0);
			
			initialize();
		}
		
		/**
		 * Add cards from the game to the GUI
		 */
		private void initialize() {
			topColumns.removeAll();
			columns.removeAll();
			
			// Add a listener for each card
			for(Card c: game.deck.cards) {
				c.addMouseListener(this);
				c.addMouseMotionListener(this);		
			}
			
			game.setupGame();
			for(Pile p : game.tableauPiles) {
				columns.add(p);
			}
			
			topColumns.add(game.stockPile);
			topColumns.add(game.talonPile);
			
			for(Pile p : game.foundationPiles) {
				topColumns.add(p);
			}
			
			validate();
		}
		
		/**
		 * Resets the whole game
		 */
		public void reset() {
			game.resetCards();
			initialize();
			repaint();
		}
		
		public void resetShuffleDemo() {
			game.resetCardsShuffleDemo();
			initialize();
			repaint();
		}
		
		/**
		 * Creates the displayText map
		 * Change this if you want to translate the game into another language
		 */
		private void createTextMap() {
			displayText = new HashMap<String, String>();
			
//			displayText.put("File", "File");
     		displayText.put("New", "New");
			displayText.put("Menu", "Menu");
//			displayText.put("Stockpile Demo", "Stockpile Demo");
			displayText.put("Contact Developers", "Contact Developers");
//			displayText.put("Save", "Save");
//			displayText.put("Load", "Load");
			displayText.put("Exit", "Exit");			
		}
		
		/**
		 * Create the top menu bar
		 */
		// HJ: VK_ below is for ALT+ actions.
		private void createTopMenu() {
			menuBar = new JMenuBar();
			
			JMenu FileMenu = new JMenu("Menu");
			FileMenu.setMnemonic(KeyEvent.VK_F);
			menuBar.add(FileMenu);
			
			menuOption[] fileOptions = new menuOption[] {
  			new menuOption(displayText.get("New"), KeyEvent.VK_N),
//				new menuOption(displayText.get("Save"), KeyEvent.VK_S),
//				new menuOption(displayText.get("Load"), KeyEvent.VK_L),
//				new menuOption(displayText.get("Stockpile Demo"), KeyEvent.VK_S),
				new menuOption(displayText.get("Contact Developers"), KeyEvent.VK_C),
				new menuOption(displayText.get("Exit"), KeyEvent.VK_X)
			};
			
			
			for(menuOption option: fileOptions) {	
				JMenuItem opt = new JMenuItem(option.name);
				if(option.shorcut != 0) opt.setMnemonic(option.shorcut);
				
				opt.addActionListener(this);
				FileMenu.add(opt);				
			}

			
			setJMenuBar(menuBar);
		}
		
		/**
		 * Auxiliary class which stores information about a single menu option
		 * @member {String} name The name of the
		 * @member {Integer} shortcut The mnemonic for this button 
		 */
		class menuOption {
			public String name;
			public Integer shorcut = 0;
			
			public menuOption(String name, Integer shorcut) {
				this.name = name;
				this.shorcut = shorcut;
			}
		}
		
		/**
		 * Function to handle most of the events performed on the GUI
		 */
		public void actionPerformed(ActionEvent e) {
			
			// Handle all menu interactions
			if(e.getSource() instanceof JMenuItem) handleMenuInteraction(e);
			
		}
		
		/**
		 * Handles the activation of any of the menu bar buttons
		 * @param {ActionEvent} e
		 */
		private void handleMenuInteraction(ActionEvent e) {
			JMenuItem item = (JMenuItem)e.getSource();
			
			if(item.getText().equals(displayText.get("Exit"))) {
				this.dispose();
				return;
			}
			if(item.getText().equals(displayText.get("New"))) {
				reset();
				return;
			}
//			if(item.getText().equals(displayText.get("Stockpile Demo"))) {
//				resetShuffleDemo();
//				// HJ: Display all cards to show non-repetition.
//				for (Pile p: game.allPiles) {
//					
//						for (Card card: p.cards) {
//							if (card.value != 100) {
//								// HJ: Show all cards face up.
//								card.show();
//							}
//						}
//					
//				}
//				
//				return;
//			}
			if(item.getText().equals(displayText.get("Contact Developers"))) {
				//HJ: Load up email address with a desktop app.
				Desktop desktop = Desktop.getDesktop(); 
				String message = "mailto:help@ACSolitaireCST8334Group2.ca?subject=User%20Help";
				URI uri = URI.create(message);
				try {
					desktop.mail(uri);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(this, "Could not launch email application");
					e1.printStackTrace();
				}

				return;
			}
//			if(item.getText().equals(displayText.get("Save"))) {
//				game.save();
//				JOptionPane.showMessageDialog(this, "Game saved!");
//				return;
//			}
			if(item.getText().equals(displayText.get("Load"))) {
				game.load();
				validate();
				return;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(tempPile != null) {
				
				Point pos = getLocationOnScreen();
				pos.x = e.getLocationOnScreen().x - pos.x - mouseOffset.x;
				pos.y = e.getLocationOnScreen().y - pos.y - mouseOffset.y;
				
				tempPile.setLocation(pos);
			}
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			//HJ Double click logic:
			if (e.getClickCount() == 2 && !e.isConsumed() 
					&& e.getComponent() instanceof Card) {
				Card c = (Card)e.getComponent();
				Pile p = (Pile)c.getParent();
				
				// if front facing:
				if (!c.isReversed) 
					switch(p.type) {
					case TABLEAU:
//						JOptionPane.showMessageDialog(this, "Tableau card!");
						// If foundation target doesn't exist, then check other tableau pile
						if (!game.checkFoundationTarget(p,c)) {
							game.checkPeerTableauTarget(p, c);
						}
						
						break;
					case TALON:
//						JOptionPane.showMessageDialog(this, "Talon card!");
						if (!game.checkFoundationTarget(p,c)) {
							game.checkTableauTarget(p, c);
						}
						break;
					}
					
					
			     e.consume();		
			} else {
			
				//HJ: Single click logic
				if(e.getComponent() instanceof Card) {
					Card c = (Card)e.getComponent();
					Pile p = (Pile)c.getParent();
					
					switch(p.type) {
						case STOCK:
							game.drawCard();
						break;
						case TABLEAU:
							game.clickPile(p);
						break;
						case TALON:
							if (game.stockPile.isEmpty()) {
								
								if (!game.checkFoundationTarget(p,c)) {
									if(!game.checkTableauTarget(p, c)){
										game.turnTalonPile();										
									}
								}
							}
						break;
					}	
					repaint();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(e.getComponent() instanceof Card) {
				Card c = (Card)e.getComponent();
				
				// Do nothing if card is reversed
				if(c.isReversed)
					return;
				
				Pile p  = (Pile)c.getParent();
				
				if(p.cards.isEmpty() || p.type == PileType.FOUNDATION) return;
				
				tempPile = p.split(c);


				lp.add(tempPile, JLayeredPane.DRAG_LAYER);

				Point pos = getLocationOnScreen();
				mouseOffset = e.getPoint();
				pos.x = e.getLocationOnScreen().x - pos.x - mouseOffset.x;
				pos.y = e.getLocationOnScreen().y - pos.y - mouseOffset.y;
				
				tempPile.setLocation(pos);
				
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(tempPile != null) {
				
				Point mousePos = e.getLocationOnScreen();
				boolean match = false;
				
				// Check if pile can merge with the pile it is dropped on
				ArrayList<Pile> droppable = new ArrayList<Pile>(game.tableauPiles);
				droppable.addAll(game.foundationPiles);
				
				for(Pile p: droppable) {
					Point pilePos = p.getLocationOnScreen();
					Rectangle r = p.getBounds();
					r.x = pilePos.x;
					r.y = pilePos.y;
					
					if(r.contains(mousePos) && p.acceptsPile(tempPile)) {
						p.merge(tempPile);
						match = true;
						break;
					}
				}
				
				// Snap back if no merge is found
				if(!match) tempPile.parent.merge(tempPile);
					
				lp.remove(tempPile);
				tempPile = null;

				repaint();
				
				if(game.checkWin()) {
					JOptionPane.showMessageDialog(this, "You won! Congrats!");
					reset();
				}
			}
		}
		
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		
		public class JPanelWithBackground extends JPanel {
			  private Image backgroundImage;

			  // Some code to initialize the background image.
			  // Here, we use the constructor to load the image. This
			  // can vary depending on the use case of the panel.
			  public JPanelWithBackground(String fileName) throws IOException {
				URL urlToImage = this.getClass().getResource(fileName);
			  	backgroundImage = ImageIO.read(urlToImage);
			  }

			  public void paintComponent(Graphics g) {
			    super.paintComponent(g);

			    // Draw the background image.
			    g.drawImage(backgroundImage, 0, 0, this);
			  }
		}
}
