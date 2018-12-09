package com.easyware.solitaire;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.Rect;

public abstract class CardStack {
	public final static float DESTINATION_CARD_PADDING_RATIO = 0.0f;
	public final static float BACKUP_CARD_PADDING_RATIO = (float) (1.0/200.0);
	public final static float UNDELIVERED_CARD_PADDING_RATIO = (float) (1.0/48.0);
	public final static float UNFLOPPED_CARD_PADDING_RATIO = (float) (1.0/16.0);
	public final static float FLOPPED_CARD_PADDING_RATIO = (float) (1.0/4.0);
	public final static float FLOPPED_CARD_IN_DELIVER_STACK_PADDING_RATIO = (float) (1.0/3.0);
	
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected float paddingRatio;
	protected boolean isHorizontalPadding;
	protected Paint paint;
	protected List<Card> cards;

	public CardStack(int x, int y, int width, int height, boolean isHorizontalPadding, float paddingRatio) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.isHorizontalPadding = isHorizontalPadding;
		this.paddingRatio = paddingRatio;
		
		paint = new Paint();
		paint.setColor(Color.rgb(96, 96, 96));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);

		cards = new ArrayList<Card>();
	}

	public CardStack (CardStack stack) {
		this(stack.x, stack.y, stack.width, stack.height, stack.isHorizontalPadding, stack.paddingRatio);
		for (Card card : stack.cards)
			cards.add(new Card(card));
	}
	
	public abstract boolean add(Card card);
	public abstract int add(List<Card> cards);	
	public abstract void setCardsFromString(String s, int cardWidth, int cardHeight, Context context);

	public void draw(Canvas canvas) {
		canvas.drawRect(new Rect(x, y, x+width-1, y+height-1), paint);
		
		int count = cards.size();
		for (int i=0; i<count; ++i) {
			Card card = null;
			try {
				card = cards.get(i);
			}
			catch (Exception ex) {
				
			}
			if (card != null)
				card.draw(canvas);
		}
	}
	
	public void setCardBackResource(int resId) {
		for (Card card : cards) {
			card.setBackResource(resId);
		}
	}
	
	public void clear() {
		cards.clear();
	}
	
	public void copyCards(List<Card> cards) {
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(new Card(card));
	}
	
	public void setIsHorizontalPadding(boolean isHorizontalPadding) {
		this.isHorizontalPadding = isHorizontalPadding;
	}
	
	public Card removeLast() {
		Card card = getLast();
		if (card == null)
			return null;
		cards.remove(cards.size()-1);
		return card;
	}

	public void removeCards(int numOfLastCardsToRemove) {
		int count = cards.size();
		if (numOfLastCardsToRemove >= count) {
			cards.clear();
			return;
		}
		for (int i = count-1; i>=count-numOfLastCardsToRemove; --i) {
			cards.remove(i);
		}
	}
	
	public Card getLast() {
		if (cards.size() == 0)
			return null;
		return cards.get(cards.size()-1);
	}
	
	public Card getCard(int index) {
		return cards.get(index);
	}
	
	public int getIndexOfCard(Card card) {
		return cards.indexOf(card);
	}
	
	public int getCardCount() {
		return cards.size();
	}
	
	public List<Card> getCards() {
		return cards;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}

	public Rect getRect() {
		Rect rect =  new Rect(x, y, x+width-1, y+height-1);
		Card lastCard = getLast();
		if (lastCard != null) {
			int right = lastCard.getX() + lastCard.getWidth() - 1; 
			int bottom = lastCard.getY() + lastCard.getHeight() - 1;
			rect = new Rect(x, y, right, bottom);
		}
		return rect;
	}
	
	public boolean isInside(Point point) {
		return getRect().contains(point.x, point.y);
	}
	public boolean isInside(int x, int y) {
		return getRect().contains(x, y);
	}
	
	public Card getMovableCard(Point point) {
        return getMovableCard(point.x, point.y);
	}

	public Card getMovableCard(int x, int y) {
		if (!isInside(x, y))
			return null;
		
		Card card = getLast();
		if (card == null || !card.isFlopped() || !card.isInside(x, y))
			return null;
		
		return card;
	}
	
	public LinkedCards getMovableCards(Point point) {
		return getMovableCards(point.x, point.y);
	}
	
	public LinkedCards getMovableCards(int x, int y) {
		Card card = getMovableCard(x, y);
		if (card == null)
			return null;

		return new LinkedCards(this, card);
	}
	
	public String toString() {
		if (cards == null || cards.size() == 0)
			return "";
		String s = cards.get(0).toString();
		for (int i=1; i<cards.size(); ++i) {
			s += "," + cards.get(i).toString();
		}
		return s;
	}
	
}
