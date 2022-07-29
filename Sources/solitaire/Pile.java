package solitaire;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JLayeredPane;

import solitaire.Card.Suit;

public class Pile extends JLayeredPane {

	Card base;
	ArrayList<Card> cards;
	// Offset below describes the vertical distance before a new card is stacked on top.
	int offset = 15;
	Suit suitFilter;
	int width;
	Pile parent;
	PileType type;
	
	enum PileType {TABLEAU, STOCK, TALON, FOUNDATION};
	

	public Pile(int width) {
		cards = new ArrayList<Card>();
		this.width = width;
		
		// Note: a 100 of spades is created for determining the "base" card - the black bottom of each pile.
		base = new Card(100, Suit.Spades);
		add(base, 1, 0);
		
		type = PileType.TABLEAU;
	}
	
	// Adds a new card to the top of the pile.

	public void addCard(Card c) {
		c.setLocation(0, offset * cards.size());
		cards.add(c);

		this.add(c, 1, 0);
		updateSize();
	}
	
	 // Removes a card from the pile
	public void removeCard(Card c) {
		cards.remove(c);
		this.remove(c);
		
		updateSize();
	}
	

	// Return first card of the pile, without removing it
	public Card peekTopCard() {
		return cards.get(cards.size() - 1);
	}
	
	// Draws card from a pile.
	public Card drawCard() {
		Card c = cards.get(0);
		removeCard(c);

		return c;
	}
	
	// Set pile width (for visual "padding" between columns).
	public void setWidth(int width) {
		this.width = width;
		updateSize();		
	}
	

	// Updates pile size based on the number of cards in it
	public void updateSize() {
		int height = base.getSize().height;
		
		if(!cards.isEmpty()) {
			height += offset * (cards.size() - 1);
		}

		this.setPreferredSize(new Dimension(width, height));
		this.setSize(width, height);
	}
	
	
	// Changes the offset of the pile
	public void setOffset(int offset) {
		this.offset = offset;
		updateSize();
	}
	

	// Splits pile, bottom half (starting at Card first) sticks to the mouse for transfer during drag.
	public Pile split(Card first) {
		Pile p = new Pile(100);
		
		for(int i = 0; i < cards.size(); ++i) {
			if(cards.get(i) == first) {
				// TODO: Below for loop just separates selected card as a separate child pile. for loop is unnecessary.
				for(; i < cards.size();) {
					p.addCard(cards.get(i));
					removeCard(cards.get(i));
				}
			}
		}
		
		p.parent = this;
		
		return p;
	}

	// add input pile on top of existing pile.
	public void merge(Pile p) {
		for(Card c: p.cards)
			addCard(c);
		
		updateSize();
	}
	

	// search card based on value and suitName
	public Card searchCard(int value, String suitName) {
		
		for(Card c: cards) {
			if(c.value == value && c.suit.name().equals(suitName))
				return c;
		}
		
		return null;
	}
	
	// check if pile has no cards.
	public boolean isEmpty() {
		return cards.size() == 0;
	}
	
	// See if solitaire card move logic is acceptable.
	public boolean acceptsPile(Pile p) {
		// Can not add to itself
		if(this == p) return false;
		
		Card newCard = p.cards.get(0);
		Card topCard;
		
		switch(type) {
		
			case TABLEAU:
				// If it's empty it can only receive a King
				if(cards.isEmpty()) {
					if(newCard.value == 13) return true;
					return false;
				}
				
				topCard = cards.get(cards.size() - 1);
				// if the top card is reversed in Tableau, then a new card can't be placed there.
				if(topCard.isReversed) return false;
				
				// Different colour, consecutive values, descending
				if(topCard.suit.isRed != newCard.suit.isRed)
				   if(topCard.value == newCard.value + 1) {
					   return true;				
				   }
			break;
			
			case FOUNDATION:
				
				// Merge with a single card, only single cards can be sent at a time.
				if(p.cards.size() > 1) return false;
				
				// Accept starting with Ace
				if(cards.isEmpty() && newCard.value == 1) {
					suitFilter = newCard.suit;
					return true;
				}
				
				// It has to be the same suit.
				if(suitFilter != newCard.suit) return false;
				
				// Consecutive values, ascending
				topCard = cards.get(cards.size() - 1);
				if(topCard.value == newCard.value - 1) {
					return true;
				}
			break;
		}
		return false;
	}
	
	// Solitaire logic card move check, used for auto-tap feature.
	public boolean acceptsCard(Card c) {
		// Can not add to itself
		if(this == (Pile) c.getParent()) return false;
		
		Card newCard = c;
		Card topCard;
		
		switch(type) {
	
			case TABLEAU:
				// If it's empty it can only receive a King
				if(cards.isEmpty()) {
					if(newCard.value == 13) return true;
					return false;
				}
				
				topCard = cards.get(cards.size() - 1);
				// if the top card is reversed in Tableau, then a new card can't be placed there.
				if(topCard.isReversed) return false;
				
				// Different colour, consecutive values, descending
				if(topCard.suit.isRed != newCard.suit.isRed)
				   if(topCard.value == newCard.value + 1) {
					   return true;				
				   }
			break;
			
			case FOUNDATION:
				
				// Start with ace
				if(cards.isEmpty() && newCard.value == 1) {
					suitFilter = newCard.suit;
					return true;
				}
				
				// It has to be the same suit.
				if(suitFilter != newCard.suit) return false;
				
				// Consecutive values, ascending
				topCard = cards.get(cards.size() - 1);
				if(topCard.value == newCard.value - 1) {
					return true;
				}
			break;
		}
		return false;
	}
	
	// Change baseline, so pile is aligned to top
	@Override
	public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
	    return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
	}

	@Override
	public int getBaseline(int width, int height) {
	    return 0;
	}
}
