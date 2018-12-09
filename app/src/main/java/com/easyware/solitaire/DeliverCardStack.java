package com.easyware.solitaire;

import java.util.List;

import android.content.Context;

public class DeliverCardStack extends CardStack {

	private boolean inAdding;
	private int countBeforeAdding;
	
	public DeliverCardStack(int x, int y, int width, int height, boolean isHorizontalPadding) {
		super(x, y, width, height, isHorizontalPadding, CardStack.FLOPPED_CARD_IN_DELIVER_STACK_PADDING_RATIO);
	}

	public void beginAdding() {
		inAdding = true;
		countBeforeAdding = cards.size();
		for (Card card : cards) {
			card.moveTo(x, y);
		}
	}
	public void endAdding() {
		inAdding = false;
	}
	
	@Override
	public boolean add(Card card) {
		if (!inAdding || card == null)
			return false;
		
		int x = 0;
		int y = 0;
		if (isHorizontalPadding) {
			x = this.x + (int)(paddingRatio*card.getWidth()*(this.cards.size()-countBeforeAdding));
			y = this.y;
		}
		else {
			x = this.x;
			y = this.y + (int)(paddingRatio*card.getHeight()*(this.cards.size()-countBeforeAdding));
			
		}
		
		card.moveTo(x, y);
		card.flop();
		this.cards.add(card);
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

	public void setCardsFromString(String s, int cardWidth, int cardHeight, Context context) {
		cards.clear();
		if (s== null || s.length() == 0)
			return;
		
		String[] cardStrings = s.split(",");
				
		// collapsed
		if (cardStrings.length-3 > 0) {
			for(int i=0; i<cardStrings.length-3; ++i) {
				String cs = cardStrings[i];
				Card card = new Card(cs, cardWidth, cardHeight, context);
				card.moveTo(x, y);
				cards.add(card);
			}
		}
		
		// padded
		beginAdding();
		for(int i=cardStrings.length-3; i<cardStrings.length; ++i) {
			String cs = cardStrings[i];
			Card card = new Card(cs, cardWidth, cardHeight, context);
			add(card);
		}
		endAdding();
	}
	
}
