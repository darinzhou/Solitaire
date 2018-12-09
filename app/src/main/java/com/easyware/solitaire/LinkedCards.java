package com.easyware.solitaire;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

public class LinkedCards {
	private List<Card> cards = new ArrayList<Card>();
	private Point originalLocation;
	private CardStack originalCardStack;
	private Point location;
	private int width;
	private int height;
	private Card firstCard;
	
	public LinkedCards(CardStack originalCardStack, Card card) {
		if (originalCardStack == null)
			return;
		this.originalCardStack = originalCardStack;
		if (card != null) {
			cards.add(card);
			originalLocation = card.getLocation();
			location = card.getLocation();
			width = card.getWidth();
			height = card.getHeight();
			this.firstCard = card;
		}			
	}
	
	public LinkedCards(WorkingCardStack wcs, Card firstCard) {
		if (wcs == null)
			return;
		originalCardStack = wcs;
		List<Card> targetCards = wcs.getCardsAfter(firstCard);
		int count = targetCards.size();
		if (count > 0) {
			this.cards.addAll(targetCards);
			originalLocation = firstCard.getLocation();
			location = firstCard.getLocation();
			width = firstCard.getWidth();
			height = (int) (firstCard.getHeight() + (count-1)*firstCard.getHeight()*CardStack.FLOPPED_CARD_PADDING_RATIO);
			this.firstCard = firstCard;
		}
	}
	
	public CardStack getOriginalCardStack() {
		return originalCardStack;
	}
	
	public int getCardCount() {
		return cards.size();
	}

	public void clear() {
		cards.clear();
	}

	public Card getFirst() {
		return firstCard;
	}

	public List<Card> getCards() {
		return cards;
	}
	
	public int getX() {
		return location.x;
	}
	public int getY() {
		return location.y;
	}
	public Point getLocation() {
		return location;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void moveTo(int x, int y) {
		int deltaX = x-location.x;
		int deltaY = y-location.y;
		for (Card card : cards) {
			card.translate(deltaX, deltaY);
		}
		location = new Point(x, y);
	}
	public void moveTo(Point location) {
		moveTo(location.x, location.y);
	}
	
	public void draw(Canvas canvas) {
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

	public Card getLast() {
		if (cards.size() == 0)
			return null;
		return cards.get(cards.size()-1);
	}

	public Rect getRect() {
		if (firstCard == null)
			return null;
		Card lastCard = getLast();
		int x = firstCard.getX();
		int y = firstCard.getY();
		int right = lastCard.getX() + lastCard.getWidth() - 1; 
		int bottom = lastCard.getY() + lastCard.getHeight() - 1;
		return new Rect(x, y, right, bottom);
	}

	public boolean intersects(CardStack stack) {
		if (getRect() == null || stack == null)
			return false;
		Rect rect1 = getRect();
		Rect rect2 = stack.getRect();
		return rect1.intersect(rect2);
	}

	public int intersectWidth(CardStack stack) {
		if (getRect() == null || stack == null)
			return -1;
		Rect rect1 = getRect();
		Rect rect2 = stack.getRect();
		if (!rect1.intersect(rect2))
			return -1;
		
		int l = Math.max(rect1.left, rect2.left);
		int r = Math.min(rect1.right, rect2.right);
		return r-l+1;
	}
	
	public void moveBack() {
		moveTo(originalLocation);
	}
}
