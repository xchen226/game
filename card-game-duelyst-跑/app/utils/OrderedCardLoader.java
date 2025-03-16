package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structures.basic.SpellCard;
import structures.basic.Card;

/**
 * This is a utility class that provides methods for loading the decks for each
 * player, as the deck ordering is fixed. 
 * @author Richard
 *
 */
public class OrderedCardLoader {

	public static String cardsDIR = "conf/gameconfs/cards/";
	
	//lu:Card id counter
	public static int cardID = 0;
	
	/**
	 * Returns all of the cards in the human player's deck in order
	 * @return
	 */
	public static List<Card> getPlayer1Cards(int copies) {
		List<Card> cardsInDeck = new ArrayList<>(20);
		//int cardID = 1;
	
		for (int i = 0; i < copies; i++) {
			for (String filename : new File(cardsDIR).list()) {
				if (filename.startsWith("1_")) { 
					boolean isSpellCard = filename.matches(".*_c_s_.*"); 
					Class<? extends Card> cardType = isSpellCard ? SpellCard.class : Card.class;
	
					Card card = BasicObjectBuilders.loadCard(cardsDIR + filename, ++ cardID, cardType);

					System.out.println("[DEBUG] Loaded " + filename + " as " + card.getClass().getSimpleName());
	
					cardsInDeck.add(card);
				}
			}
		}
	
		return cardsInDeck;
	}
	/**
	 * Returns all of the cards in the human player's deck in order
	 * @return
	 */
	public static List<Card> getPlayer2Cards(int copies) {
	
		List<Card> cardsInDeck = new ArrayList<Card>(20);
		
		// int cardID = 1;
		for (int i =0; i<copies; i++) {
			for (String filename : new File(cardsDIR).list()) {
				if (filename.startsWith("2_")) {
					// this is a deck 2 card
					cardsInDeck.add(BasicObjectBuilders.loadCard(cardsDIR+filename, ++ cardID, Card.class));
				}
			}
		}
		
		return cardsInDeck;
	}
	
}
