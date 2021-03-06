package solitaire;

import java.io.File;
import java.io.StringReader; // HJ: StringReader imported.
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource; // HJ: InputSource imported.

import solitaire.Card.Suit;
import solitaire.Pile.PileType;

/**
 * Core class of the application.
 * Contains all objects and states of the game 
 */
public class Engine {
	
	// HJ: piles ArrayList below appear to be for the Tableau columns.
	ArrayList<Pile> tableauPiles;
	ArrayList<Pile> foundationPiles;
	Pile stockPile, talonPile;
	ArrayList<Pile> allPiles;
	public final int numberTableauPiles = 7;
	public Deck deck;
	
	/**
	 * Class constructor
	 */
	public Engine() {
		resetCards();
	}
	
	/**
	 * Reset all game piles and the deck
	 */
	public void resetCards() {
		deck = new Deck();
		deck.shuffle();
		
		// HJ: Stockpile
		stockPile = new Pile(120);
		stockPile.setOffset(0);
		
		
		// HJ: Talon pile
		talonPile = new Pile(180);
		talonPile.setOffset(0);
		
		// HJ: Foundation pile.
		foundationPiles = new ArrayList<Pile>();
		tableauPiles = new ArrayList<Pile>();
		
		allPiles = new ArrayList<Pile>();
		allPiles.add(stockPile);
		allPiles.add(talonPile);
	}
	
	// HJ: For Shuffle Demo
	public void resetCardsShuffleDemo() {
		deck = new Deck();
		deck.shuffle();
		
		// HJ: Stockpile
		stockPile = new Pile(120);
		stockPile.setOffset(15);
		
		
		// HJ: Talon pile
		talonPile = new Pile(180);
		talonPile.setOffset(15);
		
		// HJ: Foundation pile.
		foundationPiles = new ArrayList<Pile>();
		tableauPiles = new ArrayList<Pile>();
		
		allPiles = new ArrayList<Pile>();
		allPiles.add(stockPile);
		allPiles.add(talonPile);
	}
	
	/**
	 * Setup the initial game state
	 */
	public void setupGame() {
		// Generate piles
		stockPile.type = PileType.STOCK;
		talonPile.type = PileType.TALON;

		for(int i = 1; i <= numberTableauPiles; ++i) {
			Pile p = new Pile(120);
			
			// Add i cards to the current pile
			for(int j = 1; j <= i; ++j) { 
				Card card = deck.drawCard();  
				p.addCard(card);
				
				if(j!=i)
					card.hide();
				else 
					card.show();
			}
			
			tableauPiles.add(p);
			allPiles.add(p);
		}
		
		// HJ: Below sets up foundation piles. Note, suit aren't explicitly set.
		for(Suit suit : Suit.values()) {
			Pile p = new Pile(100);
			p.setOffset(0);
			p.type = PileType.FOUNDATION;
			foundationPiles.add(p);	
			allPiles.add(p);
		}
		
		// HJ: After distributing cards across the tableau and foundation piles, the remainder gets sent to the stock pile.
		while(deck.size() > 0) {
			Card card = deck.drawCard();
			card.hide();
			stockPile.addCard(card);
		}
	}
	
	/**
	 * Draw a card from the Stock pile and place it into the Talon pile
	 */
	public void drawCard() {
		if(!stockPile.cards.isEmpty()) {
			Card drew = stockPile.drawCard();
			drew.isReversed = false;
			talonPile.addCard(drew);			
		}
	}
	
	/**
	 * When a Tableau pile is clicked, if the top card is reversed show it
	 * @param {Pile} p
	 */
	public void clickPile(Pile p) {
		if(!p.cards.isEmpty()) {
			Card c = p.cards.get(p.cards.size() - 1);
			if(c.isReversed) {
				c.isReversed = false;
			}
		}
	}
	
	/**
	 * Reverse the Talon pile and place it again for drawing from Stockpile
	 */
	public void turnTalonPile() {
		if(!stockPile.cards.isEmpty()) return;
		
		while(!talonPile.cards.isEmpty()) {
			Card c = talonPile.drawCard();
			c.isReversed = true;
			
			stockPile.addCard(c);
		}
	}
	
	/**
	 * Check whether a card can be placed on any of the Foundation piles
	 * @param {Pile} priginPile
	 * @param {Card} originCard
	 * @return {Boolean}
	 */
	public boolean checkFoundationTarget(Pile originPile, Card originCard) {
		String ocSuit = originCard.suit.name();
		int ocValue = originCard.value;
		Card targetCard;
	
		if (ocValue==1) {
			for (Pile targetPile : foundationPiles) {
				// If suit doesn't exist, then add card
				if (targetPile.isEmpty()) {

					//JOptionPane.showMessageDialog(null, "Ace found!");
					originPile.removeCard(originCard);
					targetPile.addCard(originCard);
					return true;	
				}
			}
			
		} else {
			// Otherwise, search for a suit and value match and move card.
			for (Pile targetPile : foundationPiles) {

				if ((targetCard = targetPile.searchCard(ocValue-1, ocSuit)) != null) {
					
					// JOptionPane.showMessageDialog(null, "Match found!");
					originPile.removeCard(originCard);
					targetPile.addCard(originCard);
					return true;
				}
			}
			
		}
		// If all else fails, return false
		return false;
	}
	
	/**
	 * Check whether a card can be placed on any of the Tableau piles
	 * @return {Boolean}
	 */
	public boolean checkTableauTarget(Pile originPile, Card originCard) {
		
		for (Pile targetPile : tableauPiles) {

			if (targetPile.acceptsCard(originCard)) {
						
//				JOptionPane.showMessageDialog(null, "Tableau Match found!");
				originPile.removeCard(originCard);
				targetPile.addCard(originCard);
				return true;
	
			}
			
		}
		// if all else fails, return false
		return false;
		
	}
	
	/**
	 * Check whether a tableau pile can be placed on any of the Tableau piles
	 * @return {Boolean}
	 */
	public boolean checkPeerTableauTarget(Pile originPile, Card originCard) {
		
		for (Pile targetPile : tableauPiles) {

			if (targetPile.acceptsCard(originCard)) {
						
//				JOptionPane.showMessageDialog(null, "Tableau Match found!");
				for (Card c: originPile.split(originCard).cards) {
					originPile.removeCard(c);
					targetPile.addCard(c);
				}
				return true;
	
			}
			
		}
		// if all else fails, return false
		return false;
		
	}
	
	
	/**
	 * Tests whether all the cards have been placed in the correct pile
	 * @return {Boolean}
	 */
	//HJ: if pile size is 13 (that means all piles have kings)
	public boolean checkWin() {
		for(Pile p : foundationPiles) {
			if(p.cards.size() != 13)
				return false;
		}
		return true;
	}

	

	/**
	 * Load the game state from internal configuration
	 */
	public void load() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			String cardSetup = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
					+ "<game>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Diamonds\" value=\"A\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"4\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Diamonds\" value=\"2\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"6\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"5\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"K\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"2\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"3\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"6\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Hearts\" value=\"Q\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"4\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"7\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"9\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"J\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Clubs\" value=\"A\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"4\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"K\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"5\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"10\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"3\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Hearts\" value=\"5\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"9\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"7\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"5\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"2\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"2\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"Q\"/>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"6\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"8\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"8\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"9\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"K\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"8\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"4\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"K\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"A\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"Q\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"3\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"10\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"7\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"10\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"3\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"J\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"10\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"6\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"8\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"J\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Diamonds\" value=\"Q\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"J\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Hearts\" value=\"A\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Spades\" value=\"9\"/>\n"
					+ "    <card isReversed=\"true\" suit=\"Clubs\" value=\"7\"/>\n"
					+ "  </pile>\n"
					+ "  <pile>\n"
					+ "    <card isReversed=\"false\" suit=\"Spades\" value=\"100\"/>\n"
					+ "  </pile>\n"
					+ "</game>\n"
					+ "";
			Document dom = db.parse(new InputSource(new StringReader(cardSetup)));

			Element docEle = dom.getDocumentElement();
			NodeList nl = docEle.getChildNodes();
			int currentPileCount = 0;
			if (nl != null) {
				// Iterate through all piles
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).getNodeType() != Node.ELEMENT_NODE)
						continue;
					Element el = (Element) nl.item(i);
					if (el.getNodeName().contains("pile")) {

						NodeList cardList = el.getChildNodes();
						Pile tempPile = new Pile(100);

						if (cardList != null) {
							// Iterate through all cards
							for (int j = 0; j < cardList.getLength(); j++) {
								if (cardList.item(j).getNodeType() != Node.ELEMENT_NODE)
									continue;
								
								Element cardNode = (Element) cardList.item(j);

								String suitName = cardNode.getAttribute("suit");
								boolean isReversed = cardNode.getAttribute("isReversed").equals("true");
								int value = Card.valueInt(cardNode.getAttribute("value"));

								// Skip the base card
								if (value == 100)
									continue;

								// Search for the card in all piles
								Card card = null;
								Pile foundPile = null;

								for (Pile p : allPiles) {
									if ((card = p.searchCard(value, suitName)) != null) {
										foundPile = p;
										break;
									}
								}

								tempPile.addCard(card);
								foundPile.removeCard(card);

								// Face-up or face-down card
								if (isReversed) {
									card.hide();
								} else {
									card.show();
								}
							}

							// Add the cards to the correct pile
							if (currentPileCount < numberTableauPiles) {
								tableauPiles.get(currentPileCount).merge(tempPile);
							} else if (currentPileCount < numberTableauPiles + 4) {
								foundationPiles.get(currentPileCount - numberTableauPiles)
										.merge(tempPile);

								if (!tempPile.isEmpty()) {
									// Set the pile filter for foundation piles
									Card c = tempPile.peekTopCard();
									foundationPiles.get(currentPileCount
											- numberTableauPiles).suitFilter = c.suit;
								}
							} else if (currentPileCount == numberTableauPiles + 4) {
								stockPile.merge(tempPile);
							} else {
								talonPile.merge(tempPile);
							}
						}
						currentPileCount++;
					}
				}
			}
						
			// Draw and add the cards again so the offsets are re-calculated
			for(Pile p: allPiles) {
				ArrayList<Card> cards = new ArrayList<Card>();
				
				while(!p.isEmpty()) cards.add(p.drawCard());
				
				for(Card card: cards)
					p.addCard(card);
			}
			
		} catch(Exception e ) {
			e.printStackTrace();
		}
	}	
}
