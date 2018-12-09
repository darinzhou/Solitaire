package com.easyware.solitaire;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;

public class WorkingCardStack extends CardStack {

	private float paddingRatioFolppedCards;
	
	public WorkingCardStack(int x, int y, int width, int height, boolean isLandscapeMode) {
		super(x, y, width, height, false, CardStack.UNFLOPPED_CARD_PADDING_RATIO);
		paddingRatioFolppedCards = isLandscapeMode ? CardStack.FLOPPED_CARD_PADDING_RATIO : CardStack.FLOPPED_CARD_IN_DELIVER_STACK_PADDING_RATIO;
	}
	
	public Point deliverTo(Card card) {
		int x = this.x;
		int y = this.y;
		int lastIndex = cards.size()-1;
		if (lastIndex >= 0) {
			if (cards.get(lastIndex).isFlopped())
				y =  cards.get(lastIndex).getY() + (int)(paddingRatioFolppedCards*card.getHeight());
			else
				y =  cards.get(lastIndex).getY() + (int)(paddingRatio*card.getHeight());
		}
		
		card.moveTo(x, y);
		cards.add(card);
		return new Point(x, y);
	}

	public List<Card> getCardsAfter(Card card) {
		List<Card> cardsAfter = new ArrayList<Card>();
		
		if (card == null)
			return cardsAfter;
		
		int start = getIndexOfCard(card);
		if (start == -1)
			return cardsAfter;

		int count = getCardCount();
		for (int i=start; i<count; ++i)
			cardsAfter.add(cards.get(i));
		
		return cardsAfter;
	}

	public void removeAfter(Card card) {
		if (card == null)
			return;
		int end = getIndexOfCard(card);
		if (end == -1)
			return;

		int count = getCardCount();
		for (int i=count-1; i>=end; --i)
			cards.remove(i);
	}
	
	public LinkedCards getMovableCards(Point point) {
		return getMovableCards(point.x, point.y);
	}
	
	public LinkedCards getMovableCards(int x, int y) {
		Card firstCard = null;
		for (int i=cards.size()-1; i>=0; --i) {
			if (cards.get(i).isFlopped()) {
		        if (cards.get(i).getRect().contains(x, y)) {
		        	firstCard = cards.get(i);
		        	break;
		        }
			}
		}
		
		if (firstCard == null)
			return null;
		
		return new LinkedCards(this, firstCard);
	}
	
	public boolean canAdd(Card card) {
		if (card == null || !card.isFlopped())
			return false;
		
		int lastIndex = cards.size()-1;
		if (lastIndex < 0) {
			if (card.getValue() == 13)
				return true;
			return false;
		}
		
		if (card.isDifferentColors(cards.get(lastIndex)) && cards.get(lastIndex).getValue() - card.getValue() == 1)
			return true;
		return false;
	}

	public boolean canAdd(List<Card> cards) {
		if (cards == null || cards.size() == 0)
			return false;	
		return canAdd(cards.get(0));
	}

	public boolean canAdd(LinkedCards cards) {
		return canAdd(cards.getCards());
	}

	@Override
	public boolean add(Card card) {
		if (card == null || !card.isFlopped())
			return false;
		
		int lastIndex = cards.size()-1;
		if (lastIndex < 0) {
			if (card.getValue() != 13)
				return false;
		}
		else {
			if (!card.isDifferentColors(cards.get(lastIndex)) || cards.get(lastIndex).getValue() - card.getValue() != 1)
				return false;
		}
		
		int x = this.x;
		int y = this.y;
		if (lastIndex >= 0) {
			if (cards.get(lastIndex).isFlopped())
				y =  cards.get(lastIndex).getY() + (int)(paddingRatioFolppedCards*card.getHeight());
			else
				y =  cards.get(lastIndex).getY() + (int)(paddingRatio*card.getHeight());
		}
		
		card.moveTo(x, y);
		cards.add(card);
		return true;
	}

	@Override
	public int add(List<Card> cards) {
		if (cards == null || cards.size() == 0)
			return 0;

		int n = 0;
		for (Card card : cards) {
			if (add(card))
				n++;
			else
				break;
		}
		
		return n;
	}

	public int add(LinkedCards cards) {
		return add(cards.getCards());
	}
	
	public boolean isAllFlopped() {
		for (Card card : cards) {
			if (!card.isFlopped())
				return false;
		}
		return true;
	}
	
	public void setCardsFromString(String s, int cardWidth, int cardHeight, Context context) {
		cards.clear();
		if (s== null || s.length() == 0)
			return;
		
		String[] cardStrings = s.split(",");
		for(int i=0; i<cardStrings.length; ++i) {
			String cs = cardStrings[i];
			Card card = new Card(cs, cardWidth, cardHeight, context);
			deliverTo(card);
		}
	}
	
}
