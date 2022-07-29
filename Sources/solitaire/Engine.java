package solitaire;

import java.util.ArrayList;
import solitaire.Card.Suit;
import solitaire.Pile.PileType;


public class Engine {
	
	// piles ArrayList below appear to be for the Tableau columns.
	ArrayList<Pile> tableauPiles;
	ArrayList<Pile> foundationPiles;
	Pile stockPile, talonPile;
	ArrayList<Pile> allPiles;
	public final int numberTableauPiles = 7;
	public Deck deck;
	

	public Engine() {
		resetCards();
	}
	
	//Reset all game piles and the deck
	public void resetCards() {
		deck = new Deck();
		deck.shuffle();
		
		// Stockpile
		stockPile = new Pile(120);
		stockPile.setOffset(0);
		
		
		// Talon pile
		talonPile = new Pile(180);
		talonPile.setOffset(0);
		
		// Foundation pile.
		foundationPiles = new ArrayList<Pile>();
		
		// Tableau piles
		tableauPiles = new ArrayList<Pile>();
		
		allPiles = new ArrayList<Pile>();
		allPiles.add(stockPile);
		allPiles.add(talonPile);
	}
	
	// Setup the initial game state
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
		
		// Below sets up foundation piles. Note, suits aren't explicitly set.
		for(Suit suit : Suit.values()) {
			Pile p = new Pile(100);
			p.setOffset(0);
			p.type = PileType.FOUNDATION;
			foundationPiles.add(p);	
			allPiles.add(p);
		}
		
		// After distributing cards across the tableau and foundation piles, the remainder gets sent to the stock pile.
		while(deck.size() > 0) {
			Card card = deck.drawCard();
			card.hide();
			stockPile.addCard(card);
		}
	}
	

	// Draw card from Stock and into Talon pile
	public void drawCard() {
		if(!stockPile.cards.isEmpty()) {
			Card drew = stockPile.drawCard();
			drew.isReversed = false;
			talonPile.addCard(drew);			
		}
	}
	

	//When Tableau pile clicked, if the top card is reversed show it
	//TODO: -> Moe wants this triggered automatically. Add to the end of all click actions.
	
	public void clickPile(Pile p) {
		if(!p.cards.isEmpty()&&p.type==PileType.TABLEAU) {
			Card c = p.cards.get(p.cards.size() - 1);
			if(c.isReversed) {
				c.isReversed = false;
			}
		}
	}
	
	// Reverse the Talon pile and place it again for drawing from Stockpile
	public void turnTalonPile() {
		if(!stockPile.cards.isEmpty()) return;
		
		while(!talonPile.cards.isEmpty()) {
			Card c = talonPile.drawCard();
			c.isReversed = true;
			
			stockPile.addCard(c);
		}
	}
	
	// Check if card can be placed on any of the Foundation piles
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
	
	//Check whether a card can be placed on any of the Tableau piles
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
	

	// Check whether a tableau pile can be placed on any of the Tableau piles
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
	
	

	// Test if foundation is full of all cards
	public boolean checkWin() {
		for(Pile p : foundationPiles) {
			//if pile size is 13 (that means all piles have kings)
			if(p.cards.size() != 13)
				return false;
		}
		return true;
	}


}
