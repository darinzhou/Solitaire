package com.easyware.solitaire;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;

public class SourceCardStack extends CardStack {

	public SourceCardStack(int x, int y, int width, int height) {
		super(x, y, width, height, true, CardStack.BACKUP_CARD_PADDING_RATIO);
	}

	@Override
	public boolean add(Card card) {
		if (card == null)
			return false;
		
		int x = this.x + (int)(paddingRatio*card.getWidth()*this.cards.size());
		int y = this.y;
		card.moveTo(x, y);
		card.unflop();
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

	public void setCards(List<Card> cards) {
		this.cards.clear();
		add(cards);
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

	/* (non-Javadoc)
	 * @see com.mytest.solitaire.CardStack#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas canvas) {

		canvas.drawCircle(x+width/2, y+height/2, width/3, paint);
		canvas.drawCircle(x+width/2, y+height/2, width/3-2, paint);
		canvas.drawCircle(x+width/2, y+height/2, width/3-4, paint);
		
		super.draw(canvas);
	}
}
