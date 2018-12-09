package com.easyware.solitaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;

public class CardSet {

	public final static int SET_CARD_NUM = 13*4;
	
	private int cardWidth;
	private int cardHeight;
	private boolean isBackgroundGreen;
	private Context context;
	private List<Card> cards = new ArrayList<Card>();
    private Random rnd = new Random();
	
	public CardSet(int cardWidth, int cardHeight, boolean isBackgroundGreen, Context context) {
		this.cardWidth = cardWidth;
		this.cardHeight = cardHeight;
		this.isBackgroundGreen = isBackgroundGreen;
		this.context = context;
		
		addCardsWithType(Card.CardType.Club);
		addCardsWithType(Card.CardType.Diamond);
		addCardsWithType(Card.CardType.Heart);
		addCardsWithType(Card.CardType.Spade);
		
		shuffle();
	}

	private void addCardsWithType(Card.CardType type)
    {
        for (int v = 1; v <= 13; ++v)
        {
        	Card card = new Card(type, v, cardWidth, cardHeight, context);
        	card.setBackResource(isBackgroundGreen ? R.drawable.back_blue : R.drawable.back_green);
            cards.add(card);
        }
    }
	
    public void shuffle()
    {
    	int count = cards.size();

        List<Card> originlCards = new ArrayList<Card>();
        originlCards.addAll(cards);
        
        cards.clear();
        for (int i = 0; i < count-1; ++i)
        {
            int index = rnd.nextInt(count - i);
            cards.add(originlCards.get(index));
            originlCards.remove(index);
        }
        cards.add(originlCards.get(0));
    }
	
    public List<Card> getCards() {
    	return cards;
    }
    
    public int getCardCount() {
    	return cards.size();
    }
}
