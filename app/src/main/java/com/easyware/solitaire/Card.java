package com.easyware.solitaire;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;


public class Card {
    
	public enum CardType { 
		Club(1), Diamond(2), Spade(3), Heart(4), Unknown(5);
		
		private int value;
	
		private CardType(int v) {
				value = v;
		}

		public int getValue() {
			return value;
		}

		public static CardType getEnum(int v) {
			for (CardType t : values()) {
				if (t.getValue() == v)
					return t;
			}
			
			return Unknown;
		}
		
	};

	public final static int CARD_WIDTH = 66;
	public final static int CARD_HEIGHT = 96;

	private CardType type;
	private int value;
	private Bitmap bitmap;
	private Bitmap bitmapBack;
	private Context context;
	private Paint paint;
	private boolean flopped;
	private Rect rectOriginal;
	private int x;
	private int y;
	private int width;
	private int height;
	
	public Card(CardType type, int value, int width, int height, Context context) {
		this.type = type;
		this.value = value;
		this.width = width;
		this.height = height;
		this.context= context; 
		paint = new Paint();
		paint.setFilterBitmap(true);
		loadBitmap();
	}

	public Card(Card card) {
		this(card.getType(), card.getValue(), card.getWidth(), card.getHeight(), card.getContext());
	}
	
	public Card(String s, int width, int height, Context context) {
		String[] ss = s.split(":");
		CardType t = CardType.getEnum(Integer.parseInt(ss[0]));
		int v = Integer.parseInt(ss[1]);
		flopped = (Integer.parseInt(ss[2]) == 1 ? true : false);
		this.type = t;
		this.value = v;
		this.width = width;
		this.height = height;
		this.context= context; 
		paint = new Paint();
		paint.setFilterBitmap(true);
		loadBitmap();
	}
	
	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the type
	 */
	public CardType getType() {
		return type;
	}
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the rectOriginal
	 */
	public Rect getRectOriginal() {
		return rectOriginal;
	}

	/**
	 * @return the rect
	 */
	public Rect getRect() {
		return new Rect(x, y, x+width, y+height);
	}

	/**
	 * @param x, y  new location to set
	 */
	public void moveTo(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void translate(int deltaX, int deltaY) {
		x += deltaX;
		y += deltaY;
	}
	
	public Point getLocation() {
		return new Point(x, y);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the bitmap
	 */
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	// load bitmap based n type and value
	private void loadBitmap() {
		int resId = R.drawable.back_blue;
		
		if (type == CardType.Club) {
			switch (value) {
			case 1:
				resId = R.drawable.c_a;
				break;
			case 2:
				resId = R.drawable.c_2;
				break;
			case 3:
				resId = R.drawable.c_3;
				break;
			case 4:
				resId = R.drawable.c_4;
				break;
			case 5:
				resId = R.drawable.c_5;
				break;
			case 6:
				resId = R.drawable.c_6;
				break;
			case 7:
				resId = R.drawable.c_7;
				break;
			case 8:
				resId = R.drawable.c_8;
				break;
			case 9:
				resId = R.drawable.c_9;
				break;
			case 10:
				resId = R.drawable.c_10;
				break;
			case 11:
				resId = R.drawable.c_j;
				break;
			case 12:
				resId = R.drawable.c_q;
				break;
			case 13:
				resId = R.drawable.c_k;
				break;
			}
		}
		else if (type == CardType.Diamond) {
			switch (value) {
			case 1:
				resId = R.drawable.d_a;
				break;
			case 2:
				resId = R.drawable.d_2;
				break;
			case 3:
				resId = R.drawable.d_3;
				break;
			case 4:
				resId = R.drawable.d_4;
				break;
			case 5:
				resId = R.drawable.d_5;
				break;
			case 6:
				resId = R.drawable.d_6;
				break;
			case 7:
				resId = R.drawable.d_7;
				break;
			case 8:
				resId = R.drawable.d_8;
				break;
			case 9:
				resId = R.drawable.d_9;
				break;
			case 10:
				resId = R.drawable.d_10;
				break;
			case 11:
				resId = R.drawable.d_j;
				break;
			case 12:
				resId = R.drawable.d_q;
				break;
			case 13:
				resId = R.drawable.d_k;
				break;
			}
		}
		else if (type == CardType.Heart) {
			switch (value) {
			case 1:
				resId = R.drawable.h_a;
				break;
			case 2:
				resId = R.drawable.h_2;
				break;
			case 3:
				resId = R.drawable.h_3;
				break;
			case 4:
				resId = R.drawable.h_4;
				break;
			case 5:
				resId = R.drawable.h_5;
				break;
			case 6:
				resId = R.drawable.h_6;
				break;
			case 7:
				resId = R.drawable.h_7;
				break;
			case 8:
				resId = R.drawable.h_8;
				break;
			case 9:
				resId = R.drawable.h_9;
				break;
			case 10:
				resId = R.drawable.h_10;
				break;
			case 11:
				resId = R.drawable.h_j;
				break;
			case 12:
				resId = R.drawable.h_q;
				break;
			case 13:
				resId = R.drawable.h_k;
				break;
			}
		}
		else if (type == CardType.Spade) {
			switch (value) {
			case 1:
				resId = R.drawable.s_a;
				break;
			case 2:
				resId = R.drawable.s_2;
				break;
			case 3:
				resId = R.drawable.s_3;
				break;
			case 4:
				resId = R.drawable.s_4;
				break;
			case 5:
				resId = R.drawable.s_5;
				break;
			case 6:
				resId = R.drawable.s_6;
				break;
			case 7:
				resId = R.drawable.s_7;
				break;
			case 8:
				resId = R.drawable.s_8;
				break;
			case 9:
				resId = R.drawable.s_9;
				break;
			case 10:
				resId = R.drawable.s_10;
				break;
			case 11:
				resId = R.drawable.s_j;
				break;
			case 12:
				resId = R.drawable.s_q;
				break;
			case 13:
				resId = R.drawable.s_k;
				break;
			}
		}
		else if (type == CardType.Unknown) {
			// just show card back
		}
		
		bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		bitmapBack = BitmapFactory.decodeResource(context.getResources(), R.drawable.back_green);
		rectOriginal = new Rect(0, 0, bitmap.getWidth()-1, bitmap.getHeight()-1);
	}
	
	public void setBackResource(int resId) {
		bitmapBack = BitmapFactory.decodeResource(context.getResources(), resId);
	}
	
	public void draw(Canvas canvas) {
		canvas.drawBitmap(flopped ? bitmap : bitmapBack, rectOriginal, getRect(), paint);
	}
	
	public boolean isFlopped() {
		return flopped;
	}
	
	public void flop() {
		flopped = true;
	}
	public void unflop() {
		flopped = false;
	}
	
	public boolean isDifferentColors(Card otherCard) {
		if (type == CardType.Club || type == CardType.Spade)
			return (otherCard.getType() == CardType.Diamond || otherCard.getType() == CardType.Heart);
		return (otherCard.getType() == CardType.Club || otherCard.getType() == CardType.Spade);
	}
	
	public boolean isInside(Point point) {
		return getRect().contains(point.x, point.y);
	}
	public boolean isInside(int x, int y) {
		return getRect().contains(x, y);
	}

	public String toString() {
		String s = String.valueOf(type.getValue()) + ":" + String.valueOf(value) + ":" + (flopped ? "1" : "0");
		return s;
	}
}
