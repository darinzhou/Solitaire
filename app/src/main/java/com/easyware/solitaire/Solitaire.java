package com.easyware.solitaire;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.graphics.Canvas;

public class Solitaire {
	public final static int SOURCE_TO_DELIVERY_CARD_NUM_DEFAULT = 3;
	public final static int DESTINATION_STACK_NUM = 4;
	public final static int WORKING_STACK_NUM = 7;
	public final static int LANDSCAPE_VERTICAL_GAP = 12;
	public final static float LANDSCAPE_HEIGHT_ADJUST = 4.5f;

	enum GameStatus {
		NOT_STARTED(1), IN_PROGRESS(2), COMPLETED(3);
		
		private int value;
		
		private GameStatus(int v) {
			value = v;
		}
		
		public int getValue() {
			return value;
		}
	
		public static GameStatus getEnum(int v) {
			for (GameStatus gr : values()) {
				if (gr.getValue() == v)
					return gr;
			}
			
			return NOT_STARTED;
		}
	};
	
	private GameStatus mGameStatus;
	private int mSceneViewWidth;
	private int mSceneViewHeight;
	private boolean mIsLandscape;
	private int mGapH;
	private int mGapV;
	private int mCardWidth;
	private int mCardHeight;

	private SourceCardStack mSourceCardStack;
	private DeliverCardStack mDeliverCardStack;
	private DestinationCardStack[] mDestinationCardStacks = new DestinationCardStack[DESTINATION_STACK_NUM];
	private WorkingCardStack[] mWorkingCardStacks = new WorkingCardStack[WORKING_STACK_NUM];

    public Solitaire(int sceneViewWidth, int sceneViewHeight) {
    	mGameStatus = GameStatus.NOT_STARTED;
    	
		mSceneViewWidth = sceneViewWidth;
		mSceneViewHeight = sceneViewHeight;

		if (sceneViewWidth <= sceneViewHeight) {
			mIsLandscape = false;
			mCardWidth = (int) (mSceneViewWidth / (WORKING_STACK_NUM + 1));
			float ratio = (float) mCardWidth / (float) Card.CARD_WIDTH;
			mCardHeight = (int) (Card.CARD_HEIGHT * ratio);
			mGapH = (mSceneViewWidth - WORKING_STACK_NUM * mCardWidth)
					/ (WORKING_STACK_NUM + 1) - 1;
			mGapV = mGapH - 1;

			for (int i = 0; i < 2; ++i) {
				for (int j = 0; j < WORKING_STACK_NUM; ++j) {
					int x = (j + 1) * mGapH + j * mCardWidth;
					int y = (i + 1) * mGapV + i * mCardHeight;
					if (i == 0 && j == 0) {
						if (mSourceCardStack == null)
							mSourceCardStack = new SourceCardStack(x, y,
									mCardWidth, mCardHeight);
					} else if (i == 0 && j == 1) {
						if (mDeliverCardStack == null)
							mDeliverCardStack = new DeliverCardStack(x, y,
									mCardWidth, mCardHeight, true);
						mDeliverCardStack.setIsHorizontalPadding(true);
					} else if (i == 0 && j > 2) {
						if (mDestinationCardStacks[j - 3] == null)
							mDestinationCardStacks[j - 3] = new DestinationCardStack(
									x, y, mCardWidth, mCardHeight);
					} else if (i == 1) {
						if (mWorkingCardStacks[j] == null)
							mWorkingCardStacks[j] = new WorkingCardStack(x,
									y, mCardWidth, mCardHeight, mIsLandscape);
					}
				}
			}

		} 
		else {
			mIsLandscape = true;
			mCardHeight = (int) (mSceneViewHeight / LANDSCAPE_HEIGHT_ADJUST);
			float ratio = (float) mCardHeight / (float) Card.CARD_HEIGHT;
			mCardWidth = (int) (Card.CARD_WIDTH * ratio);
			mGapH = (mSceneViewWidth - (WORKING_STACK_NUM + 2) * mCardWidth)
					/ (WORKING_STACK_NUM + 3) - 1;
			mGapV = LANDSCAPE_VERTICAL_GAP - 1;

			for (int j = 0; j < WORKING_STACK_NUM + 2; ++j) {
				int x = (j + 1) * mGapH + j * mCardWidth;
				int y = mGapV;

				if (j == 1) {
					mSourceCardStack = new SourceCardStack(x, y,
							mCardWidth, mCardHeight);
					y = 2 * mGapV + mCardHeight;
					if (mDeliverCardStack == null)
						mDeliverCardStack = new DeliverCardStack(x, y,
								mCardWidth, mCardHeight, false);
					mDeliverCardStack.setIsHorizontalPadding(false);
				} else if (j > 1) {
					if (mWorkingCardStacks[j - 2] == null)
						mWorkingCardStacks[j - 2] = new WorkingCardStack(x,
								y, mCardWidth, mCardHeight, mIsLandscape);
				}
			}
			for (int i = 0; i < DESTINATION_STACK_NUM; ++i) {
				int j = 0;
				int x = (j + 1) * mGapH + j * mCardWidth;
				int y = (i + 1) * mGapV + i * mCardHeight;
				if (mDestinationCardStacks[i] == null)
					mDestinationCardStacks[i] = new DestinationCardStack(x,
							y, mCardWidth, mCardHeight);
			}
		}
	}
    
    public Solitaire(Solitaire solitaire) {
    	this(solitaire.mCardWidth, solitaire.mCardHeight);
    	mSourceCardStack.copyCards(solitaire.getSourceStack().getCards());
    	mDeliverCardStack.copyCards(solitaire.getDeliverStack().getCards());
		for (int i = 0; i < DESTINATION_STACK_NUM; ++i) {
			mDestinationCardStacks[i].copyCards(solitaire.mDestinationCardStacks[i].getCards());
		}
		for (int i = 0; i < WORKING_STACK_NUM; ++i) {
			mWorkingCardStacks[i].copyCards(solitaire.mWorkingCardStacks[i].getCards());
		}
    }
    
    public int getSceneViewWidth() {
    	return mSceneViewWidth;
    }
    public int getSceneViewHeight() {
    	return mSceneViewHeight;
    }

    public boolean  startGame() {
    	if (isEmpty())
    		return false;
    	clear();
    	mGameStatus = GameStatus.IN_PROGRESS;
    	return true;
    }
	
    public GameStatus getGameStatus() {
    	return mGameStatus;
    }
    
    public void setGameStatus(GameStatus gs) {
    	mGameStatus = gs;
    }
    
	public int getCardWidth() {
		return mCardWidth;
	}
	public int getCardHeight() {
		return mCardHeight;
	}
	public SourceCardStack getSourceStack() {
		return mSourceCardStack;
	}
	public DeliverCardStack getDeliverStack() {
		return mDeliverCardStack;
	}
	public DestinationCardStack[] getDestinationStacks() {
		return mDestinationCardStacks;
	}
	public WorkingCardStack[] getWorkingStacks() {
		return mWorkingCardStacks;
	}
	public DestinationCardStack getDestinationStack(int index) {
		return mDestinationCardStacks[index];
	}
	public WorkingCardStack getWorkingStack(int index) {
		return mWorkingCardStacks[index];
	}

	public void draw(Canvas canvas) {
		if (mSourceCardStack != null)
			mSourceCardStack.draw(canvas);
		if (mDeliverCardStack != null)
		 	mDeliverCardStack.draw(canvas);
		for (int i = 0; i < WORKING_STACK_NUM; ++i)
			if (mWorkingCardStacks[i] != null)
				mWorkingCardStacks[i].draw(canvas);
		for (int i = 0; i < DESTINATION_STACK_NUM; ++i)
			if (mDestinationCardStacks[i] != null)
				mDestinationCardStacks[i].draw(canvas);
	}
	
	public boolean isSucceed() {
		if (mSourceCardStack.getCardCount() != 0 || mDeliverCardStack.getCardCount() != 0)
			return false;

		for (int i = 0; i < WORKING_STACK_NUM; ++i) {
			if (!mWorkingCardStacks[i].isAllFlopped()) {
				return false;
			}
		}

		mGameStatus = GameStatus.COMPLETED;		
		return true;
	}
	
	public void setCardBackResource(int resId) {
		mSourceCardStack.setCardBackResource(resId);
		mDeliverCardStack.setCardBackResource(resId);
		for (int i = 0; i < DESTINATION_STACK_NUM; ++i)
			mDestinationCardStacks[i].setCardBackResource(resId);
		for (int i = 0; i < WORKING_STACK_NUM; ++i)
			mWorkingCardStacks[i].setCardBackResource(resId);
	}

	public void clear() {
		mSourceCardStack.clear();
		mDeliverCardStack.clear();
		for (int i = 0; i < DESTINATION_STACK_NUM; ++i)
			mDestinationCardStacks[i].clear();
		for (int i = 0; i < WORKING_STACK_NUM; ++i)
			mWorkingCardStacks[i].clear();
	}
	
	public boolean isEmpty() {
		if (mSourceCardStack == null)
			return true;
		if (mDeliverCardStack == null)
			return true;
		for (int i = 0; i < DESTINATION_STACK_NUM; ++i)
			if (mDestinationCardStacks[i] == null)
				return true;
		for (int i = 0; i < WORKING_STACK_NUM; ++i)
			if (mWorkingCardStacks[i] == null)
				return true;
		return false;
	}
		
	public LinkedCards takeCards(int x, int y) {
    	if (mGameStatus != GameStatus.IN_PROGRESS)
    		return null;
		
		// 1. check deliver stack
		LinkedCards movingCards = mDeliverCardStack.getMovableCards(x, y);
		if (movingCards != null)
			return movingCards;
		
		// 2. destination stacks
		for (int i = 0; i < DESTINATION_STACK_NUM; ++i) {
			movingCards = mDestinationCardStacks[i].getMovableCards(x, y);
			if (movingCards != null)
				break;
		}
		if (movingCards != null)
			return movingCards;

		// 3. working stacks
		for (int i = 0; i < WORKING_STACK_NUM; ++i) {
			movingCards = mWorkingCardStacks[i].getMovableCards(x, y);
			if (movingCards != null)
				break;
		}

		// to here, return what we have any way
		return movingCards;
	}
	
	public boolean dropCards(LinkedCards movingCards) {
    	if (mGameStatus != GameStatus.IN_PROGRESS)
    		return false;

    	int w = -1;

		// 1. destination stacks
		DestinationCardStack targetDCS = null;
		if (movingCards.getCardCount() == 1) {
			for (int i = 0; i < DESTINATION_STACK_NUM; ++i) {
				int iw = movingCards.intersectWidth(mDestinationCardStacks[i]);
				if (iw > w && mDestinationCardStacks[i].canAdd(movingCards)) {
					w = iw;
					targetDCS = mDestinationCardStacks[i];
				}
			}
		}
		
		// 2. working stacks
		WorkingCardStack targetWCS = null;
		for (int i = 0; i < WORKING_STACK_NUM; ++i) {
			int iw = movingCards.intersectWidth(mWorkingCardStacks[i]);
			if (iw > w && mWorkingCardStacks[i].canAdd(movingCards)) {
				w = iw;
				targetWCS = mWorkingCardStacks[i];
			}
		}

		// not near any stack
		if (targetDCS == null && targetWCS == null) {
			// move back
			movingCards.moveBack();
			return false;
		}
		
		// near a working stack
		if (targetWCS != null) {
			// check if can add
			if (targetWCS.add(movingCards) > 0) {
				movingCards.getOriginalCardStack().removeCards(movingCards.getCardCount());
				if (movingCards.getOriginalCardStack() instanceof WorkingCardStack) {
					Card lastCard =	movingCards.getOriginalCardStack().getLast();
					if (lastCard != null)
						lastCard.flop();
				}
			}
		}
		// near a destination stack
		else {
			if (targetDCS.add(movingCards) > 0) {
				movingCards.getOriginalCardStack().removeCards(movingCards.getCardCount());
				if (movingCards.getOriginalCardStack() instanceof WorkingCardStack) {
					Card lastCard =	movingCards.getOriginalCardStack().getLast();
					if (lastCard != null)
						lastCard.flop();
				}
			}
		}
			
		return true;
	}
	
	public String getSourceStackString() {
		return mSourceCardStack.toString();
	}
	public String getDeliverStackString() {
		return mDeliverCardStack.toString();
	}
	public String[] getDestinationStackStrings() {
		String[] ss = new String[DESTINATION_STACK_NUM];
		for (int i=0; i<DESTINATION_STACK_NUM; ++i)
			ss[i] = mDestinationCardStacks[i].toString();
		return ss;
	}
	public String[] getWorkingStackStrings() {
		String[] ss = new String[WORKING_STACK_NUM];
		for (int i=0; i<WORKING_STACK_NUM; ++i)
			ss[i] = mWorkingCardStacks[i].toString();
		return ss;
	}

	public void setSourceStack(String s, Context context) {
		mSourceCardStack.setCardsFromString(s, mCardWidth, mCardHeight, context);
	}
	public void setDeliverStack(String s, Context context) {
		mDeliverCardStack.beginAdding();
		mDeliverCardStack.setCardsFromString(s, mCardWidth, mCardHeight, context);
		mDeliverCardStack.endAdding();
	}
	public void setDestinationStack(int index, String s, Context context) {
		mDestinationCardStacks[index].setCardsFromString(s, mCardWidth, mCardHeight, context);
	}
	public void setWorkingStack(int index, String s, Context context) {
		mWorkingCardStacks[index].setCardsFromString(s, mCardWidth, mCardHeight, context);
	}
	public void setDestinationStacks(String[] ss, Context context) {
		for (int i=0; i<DESTINATION_STACK_NUM; ++i)
			setDestinationStack(i, ss[i], context);
	}
	public void setWorkingStacks(String[] ss, Context context) {
		for (int i=0; i<WORKING_STACK_NUM; ++i)
			setWorkingStack(i, ss[i], context);
	}
	
	public void save(OutputStream outStream) throws IOException {
		if (outStream == null)
			return;

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outStream);
        outputStreamWriter.write(getGameStatus().getValue());
        outputStreamWriter.write(getSourceStackString()+"\n");
        outputStreamWriter.write(getDeliverStackString()+"\n");
        String[] destinationStrings = getDestinationStackStrings();
		for (int i=0; i<DESTINATION_STACK_NUM; ++i)
	        outputStreamWriter.write(destinationStrings[i]+"\n");
        String[] workingStrings = getWorkingStackStrings();
		for (int i=0; i<WORKING_STACK_NUM; ++i)
	        outputStreamWriter.write(workingStrings[i]+"\n");
        outputStreamWriter.close();
	}

	public void load(InputStream inStream, Context context) throws IOException {
		if (inStream == null)
			return;

		InputStreamReader inputStreamReader = new InputStreamReader(inStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        setGameStatus(GameStatus.getEnum(bufferedReader.read()));
        setSourceStack(bufferedReader.readLine(), context);
        setDeliverStack(bufferedReader.readLine(), context);
		for (int i=0; i<DESTINATION_STACK_NUM; ++i)
	        setDestinationStack(i, bufferedReader.readLine(), context);
		for (int i=0; i<WORKING_STACK_NUM; ++i)
	        setWorkingStack(i, bufferedReader.readLine(), context);		
	}
}
