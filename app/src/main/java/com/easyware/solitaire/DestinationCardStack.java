package com.easyware.solitaire;

import java.util.List;

import com.easyware.solitaire.Card.CardType;

import android.content.Context;

public class DestinationCardStack extends CardStack {

	private Card.CardType type = CardType.Unknown;
	
	public DestinationCardStack(int x, int y, int width, int height) {
		super(x, y, width, height, true, CardStack.DESTINATION_CARD_PADDING_RATIO);
	}

	public CardType getType() {
		return type;
	}
	
	public boolean canAdd(Card card) {
		if (card == null || !card.isFlopped())
			return false;
		
		int lastIndex = cards.size()-1;
		if (lastIndex < 0) {
			if (card.getValue() != 1)
				return false;
			type = card.getType();
		}
		else {
			if (card.getType() != type || card.getValue() - cards.get(lastIndex).getValue() != 1)
				return false;
		}
		
		return true;
	}

	public boolean canAdd(List<Card> cards) {
		if (cards == null || cards.size() != 1)
			return false;
		return canAdd(cards.get(0));
	}

	public boolean canAdd(LinkedCards cards) {
		if (cards == null || cards.getCards().size() != 1)
			return false;
		return canAdd(cards.getFirst());
	}

	@Override
	public boolean add(Card card) {
		if (card == null || !card.isFlopped())
			return false;
		
		int lastIndex = cards.size()-1;
		if (lastIndex < 0) {
			if (card.getValue() != 1)
				return false;
			type = card.getType();
		}
		else {
			if (card.getType() != type || card.getValue() - cards.get(lastIndex).getValue() != 1)
				return false;
		}
		
		int x = this.x + (int)(paddingRatio*card.getWidth()*this.cards.size());
		int y = this.y;
		card.moveTo(x, y);
		cards.add(card);
		return true;
	}

	@Override
	public int add(List<Card> cards) {
		if (cards == null || cards.size() != 1)
			return 0;
		return add(cards.get(0)) ? 1 : 0;
	}

	public int add(LinkedCards cards) {
		if (cards == null || cards.getCards().size() != 1)
			return 0;
		return add(cards.getFirst()) ? 1 : 0;
	}
	
	public void setCardsFromString(String s, int cardWidth, int cardHeight, Context context) {
		cards.clear();
		if (s== null || s.length() == 0)
			return;
		
		String[] cardStrings = s.split(",");
		for(int i=0; i<cardStrings.length; ++i) {
			String cs = cardStrings[i];
			add(new Card(cs, cardWidth, cardHeight, context));
		}
	}
	
}
