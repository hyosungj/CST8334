package solitaire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import solitaire.Card.Suit;

public class Deck {
	
	ArrayList<Card> cards;
	
	public Deck() {
		
		// Create all 52 cards
		cards = new ArrayList<Card>();
		
		for(Suit suit : Suit.values()) {
			for(int value = 1; value <= 13; ++value) {
				cards.add(new Card(value, suit));
			}
		}
	}

	// Shuffle deck 20 times by swapping the every card in deck with a random card somewhere in the pile.
	public void shuffle() {
		Random randIndex = new Random();
		int size = cards.size();
		
		for(int shuffles = 1; shuffles <= 20; ++shuffles)
			for (int i = 0; i < size; i++) 
				Collections.swap(cards, i, randIndex.nextInt(size));
		
	}
	
	// Find number of cards in deck
	public int size() {
		return cards.size();
	}
	
	// Draw first card in deck.
	public Card drawCard() {
		Card c = cards.get(0);
		cards.remove(0);

		return c;
	}
	
}
