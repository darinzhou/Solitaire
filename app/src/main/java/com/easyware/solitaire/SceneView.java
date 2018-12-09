package com.easyware.solitaire;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SceneView extends View implements View.OnTouchListener {
	private Solitaire mSolitaire;
	private int mDrawNumOfCards = Solitaire.SOURCE_TO_DELIVERY_CARD_NUM_DEFAULT;
	private LinkedCards mMovingCards;
	private boolean mIsMoving;
	private int mDeltaX;
	private int mDeltaY;
	
	private boolean mRestored;
	private Solitaire.GameStatus mGameStatus;
	private String mSourceStackString;
	private String mDeliverStackString;
	private String[] mDestinationStackStrings;
	private String[] mWorkingStackStrings;

	private AsyncPlayer mAsyncPlayer;
	private AnimationForSucceedTask mAnimationForSucceedTask;
	private boolean mIsInNewGame;

	private MainActivity mMainActivity;

	public SceneView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SceneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SceneView(Context context) {
		super(context);
		init();
	}

	private void init() {
		if (!isInEditMode()) {
			setOnTouchListener(this);
			mAsyncPlayer = new AsyncPlayer("Solitaire");
		}
	}

	public void setMainActivity(MainActivity activity) {
		mMainActivity = activity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// the safe place to get the actual dimension of the view
		if (w > 0 && h > 0) {
			// re-create Solitaire whenever orientation is changed
			mSolitaire = new Solitaire(w, h);

			// need to restore old game, or it's a new game
			if (mRestored) {
				mRestored = false;				
		        if (mIsInNewGame) {
		        	mIsInNewGame = false;
					fastNewGame();
		        }
		        else {
			        mSolitaire.setSourceStack(mSourceStackString, getContext());
			        mSolitaire.setDeliverStack(mDeliverStackString, getContext());
			        mSolitaire.setDestinationStacks(mDestinationStackStrings, getContext());
			        mSolitaire.setWorkingStacks(mWorkingStackStrings, getContext());
		        }
		        
		        if (mGameStatus == Solitaire.GameStatus.COMPLETED) {
		        	mSolitaire.clear();
		        	mGameStatus = Solitaire.GameStatus.NOT_STARTED;
		        }
		        mSolitaire.setGameStatus(mGameStatus);
			}
			
			if (!isInEditMode()) {
				MainActivity activity = (MainActivity)getContext();
				activity.showNewGameButton(
						mSolitaire.getGameStatus() == Solitaire.GameStatus.NOT_STARTED);
				changeAppearance(activity.isBackgroundGreen());
				mDrawNumOfCards = activity.isDrawOne() ? 1 : 3;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mSolitaire != null)
			mSolitaire.draw(canvas);
		if (mMovingCards != null)
			mMovingCards.draw(canvas);		
	}

	public void changeAppearance(boolean isGreen) {
		setBackgroundResource(isGreen ? R.drawable.background_green : R.drawable.background_blue);
		getSolitaire().setCardBackResource(isGreen ? R.drawable.back_blue : R.drawable.back_green);
	}
	
	public Solitaire getSolitaire() {
		return mSolitaire;
	}
	
	public void newGame() {
		if (mAnimationForSucceedTask != null) {
			mAnimationForSucceedTask.cancel(true);
			mAnimationForSucceedTask = null;
		}
		mMovingCards = null;
		
		if (mSolitaire.startGame())
			(new NewGameTask()).execute();
	}

	public boolean unflopSourceToDeliverCardStack(int x, int y) {
		if (mSolitaire.getSourceStack().getRect().contains(x, y)) {
			(new UnflopSourceTask()).execute();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!unflopSourceToDeliverCardStack((int)event.getX(), (int)event.getY())) {
				takeCards(event);
			}
			if (mAnimationForSucceedTask != null) {
				mAnimationForSucceedTask.cancel(true);
				mAnimationForSucceedTask = null;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			moveCards(event);
			break;
		case MotionEvent.ACTION_UP:
			dropCards(event);
			break;
		}
		return true;
	}

	public void setDrawOne(boolean isDrawOne) {
		mDrawNumOfCards = isDrawOne ? 1 : 3;
	}
	
	public void takeCards(MotionEvent e) {
		if (mIsMoving)
			return;

		mMovingCards = mSolitaire.takeCards((int)e.getX(), (int)e.getY());

		if (mMovingCards != null) {
			asyncPlay(getContext(), R.raw.take_cards, false);
			mDeltaX = (int) e.getX() - mMovingCards.getX();
			mDeltaY = (int) e.getY() - mMovingCards.getY();
		}
	}

	public void moveCards(MotionEvent e) {
		if (mMovingCards == null)
			return;

		mIsMoving = true;
		int x = (int) e.getX() - mDeltaX;
		int y = (int) e.getY() - mDeltaY;
		mMovingCards.moveTo(x, y);
		invalidate();
	}

	public void dropCards(MotionEvent e) {
		if (!mIsMoving || mMovingCards == null)
			return;
		
		if (mSolitaire.dropCards(mMovingCards)) {
			asyncPlay(getContext(), R.raw.drop_cards, false);
		}
		else {
			asyncPlay(getContext(), R.raw.move_back_cards, false);
		}
		
		mMovingCards = null;
		mIsMoving = false;
		invalidate();

		// check if completed
		if (mSolitaire.isSucceed()) {
			mAnimationForSucceedTask = new AnimationForSucceedTask();
			mAnimationForSucceedTask.execute();
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		if (mAnimationForSucceedTask != null) {
			mAnimationForSucceedTask.cancel(true);
			mAnimationForSucceedTask = null;
		}
		
        Bundle b = new Bundle();
        Parcelable s = super.onSaveInstanceState();
        b.putParcelable("super_state", s);
        
        b.putInt("game_status", mSolitaire.getGameStatus().getValue());
        b.putString("source_stack", mSolitaire.getSourceStackString());
        b.putString("deliver_stack", mSolitaire.getDeliverStackString());
        b.putStringArray("destination_stacks", mSolitaire.getDestinationStackStrings());
        b.putStringArray("working_stacks", mSolitaire.getWorkingStackStrings());
        b.putBoolean("in_new_game_process", mIsInNewGame);
        
        return b;
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof Bundle)) {
            // Not supposed to happen.
            super.onRestoreInstanceState(state);
            return;
        }

        Bundle b = (Bundle) state;
        Parcelable superState = b.getParcelable("super_state");

        mRestored = true;

        mGameStatus = Solitaire.GameStatus.getEnum(b.getInt("game_status", Solitaire.GameStatus.NOT_STARTED.getValue()));
    	mSourceStackString = b.getString("source_stack", null);
    	mDeliverStackString = b.getString("deliver_stack", null);
    	mDestinationStackStrings = b.getStringArray("destination_stacks");
    	mWorkingStackStrings = b.getStringArray("working_stacks");
    	mIsInNewGame = b.getBoolean("in_new_game_process", true);
        
        super.onRestoreInstanceState(superState);
	}

	public void asyncPlay(Context context, int soundId, boolean looping) {
		if (!((MainActivity)context).isPlaySound())
			return;
		String url = "android.resource://" + context.getPackageName() + "/" + soundId;
		mAsyncPlayer.play(context, Uri.parse(url), looping, AudioManager.STREAM_MUSIC);
	}
	public void asyncStop() {
		mAsyncPlayer.stop();
	}

	public void fastNewGame() {
		mSolitaire.clear();
		MainActivity activity = (MainActivity)getContext();
		CardSet cardSet = new CardSet(mSolitaire.getCardWidth(), mSolitaire.getCardHeight(), activity.isBackgroundGreen(), getContext());
		mSolitaire.getSourceStack().setCards(cardSet.getCards());
		for (int i = 0; i < Solitaire.WORKING_STACK_NUM; ++i) {
			for (int j = 0; j < i + 1; ++j) {
				Card card = mSolitaire.getSourceStack().getLast();
				if (card != null) {
					Point location1 = mSolitaire.getWorkingStacks()[i].deliverTo(card);
					mSolitaire.getSourceStack().removeLast();
					card.moveTo(location1.x, location1.y);
				}
			}
		}		
		for (int i = 0; i < Solitaire.WORKING_STACK_NUM; ++i) {
			Card card = mSolitaire.getWorkingStacks()[i].getLast();
			if (card != null) {
				card.flop();
				invalidate();
			}
		}
	}
	
	// tasks
	
	public class NewGameTask extends AsyncTask<Void, Void, Void> {

		private void sleep(int ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			asyncPlay(mMainActivity, R.raw.shuffling_cards, true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			mIsInNewGame = true;
			
			// Testing code ----------------------------------------------->
			/*
			for (int k=13; k>0; --k) {
				Card card0 = new Card(((k%2 == 1) ? Card.CardType.Club : Card.CardType.Diamond), k, mSolitaire.getCardWidth(), mSolitaire.getCardHeight(), getContext());
				card0.flop();
				Card card1 = new Card(((k%2 == 0) ? Card.CardType.Club : Card.CardType.Diamond), k, mSolitaire.getCardWidth(), mSolitaire.getCardHeight(), getContext());
				card1.flop();
				Card card2 = new Card(((k%2 == 1) ? Card.CardType.Spade : Card.CardType.Heart), k, mSolitaire.getCardWidth(), mSolitaire.getCardHeight(), getContext());
				card2.flop();
				Card card3 = new Card(((k%2 == 0) ? Card.CardType.Spade : Card.CardType.Heart), k, mSolitaire.getCardWidth(), mSolitaire.getCardHeight(), getContext());
				card3.flop();

				mSolitaire.getWorkingStack(0).add(card0);
				mSolitaire.getWorkingStack(1).add(card1);
				mSolitaire.getWorkingStack(2).add(card2);
				mSolitaire.getWorkingStack(3).add(card3);
			}
			*/
			// Testing code <-----------------------------------------------
			
			CardSet cardSet = new CardSet(mSolitaire.getCardWidth(), mSolitaire.getCardHeight(), mMainActivity.isBackgroundGreen(), mMainActivity);
			mSolitaire.getSourceStack().setCards(cardSet.getCards());
			publishProgress();

			for (int i = 0; i < Solitaire.WORKING_STACK_NUM; ++i) {
				for (int j = 0; j < i + 1; ++j) {
					Card card = mSolitaire.getSourceStack().getLast();
					if (card != null) {
						Point location0 = card.getLocation();
						Point location1 = mSolitaire.getWorkingStacks()[i].deliverTo(card);
						mSolitaire.getSourceStack().removeLast();

						int mpx = (location0.x + location1.x) / 2;
						int mpy = (location0.y + location1.y) / 2;
						int pmpx = (location0.x + mpx) / 2;
						int pmpy = (location0.y + mpy) / 2;
						int ampx = (location1.x + mpx) / 2;
						int ampy = (location1.y + mpy) / 2;
						
						card.moveTo(pmpx, pmpy);
						publishProgress();
						sleep(20);
						card.moveTo(mpx, mpy);
						publishProgress();
						sleep(20);
						card.moveTo(ampx, ampy);
						publishProgress();
						sleep(20);
						card.moveTo(location1.x, location1.y);
						publishProgress();
						sleep(20);
					}
				}
			}
			
			mIsInNewGame = false;
			
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			invalidate();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			for (int i = 0; i < Solitaire.WORKING_STACK_NUM; ++i) {
				Card card = mSolitaire.getWorkingStacks()[i].getLast();
				if (card != null) {
					card.flop();
					invalidate();
				}
			}
			mIsInNewGame = false;
			asyncStop();
		}
	}

	public class UnflopSourceTask extends AsyncTask<Void, Void, Void> {

		private void sleep(int ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			asyncPlay(getContext(), R.raw.draw_cards, false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mSolitaire.getSourceStack().getCardCount() > 0) {
				mSolitaire.getDeliverStack().beginAdding();
				for (int i=0; i<mDrawNumOfCards; ++i) {
					Card card = mSolitaire.getSourceStack().getLast();
					if (card != null) {
						Point location0 = card.getLocation();
						card.flop();
						publishProgress();
						sleep(20);

						mSolitaire.getDeliverStack().add(card);
						mSolitaire.getSourceStack().removeLast();
						Point location1 = mSolitaire.getDeliverStack().getLast().getLocation();
						int mpx = (location0.x + location1.x) / 2;
						int mpy = (location0.y + location1.y) / 2;
						
						card.moveTo(mpx,  mpy);
						publishProgress();
						sleep(20);
						card.moveTo(location1.x,  location1.y);
						publishProgress();
						sleep(20);
					}
					else {
						break;
					}
				}
				mSolitaire.getDeliverStack().endAdding();
			}
			else {
				int count = mSolitaire.getDeliverStack().getCardCount();
				for (int i=count-1; i>=0; --i) {
					Card card = mSolitaire.getDeliverStack().getLast();
					Point location0 = card.getLocation();
					card.unflop();
					publishProgress();
					//sleep(20);

					mSolitaire.getSourceStack().add(card);
					mSolitaire.getDeliverStack().removeLast();
					Point location1 = mSolitaire.getSourceStack().getLast().getLocation();
					int mpx = (location0.x + location1.x) / 2;
					int mpy = (location0.y + location1.y) / 2;
					
					card.moveTo(mpx,  mpy);
					publishProgress();
					//sleep(20);
					card.moveTo(location1.x,  location1.y);
					publishProgress();
					sleep(20);
				}
				
			}
			
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			invalidate();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			asyncStop();
		}
	}
	
	public class AnimationForSucceedTask extends AsyncTask<Void, Void, Void> {

		private void sleep(int ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			asyncPlay(getContext(), R.raw.succeed, true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mSolitaire == null || mSolitaire.isEmpty() || !mSolitaire.isSucceed())
				return null;

			// make sure all destination stacks are not empty
			for (int i=0; i<Solitaire.DESTINATION_STACK_NUM; ++i) {
				if (mSolitaire.getDestinationStack(i).getCardCount() == 0) {
					for (int j=0; j<Solitaire.WORKING_STACK_NUM; ++j) {
						Card lastCard = mSolitaire.getWorkingStack(j).getLast();
						if (lastCard.getValue() == 1) {							
							int x0 = lastCard.getX();
							int y0 = lastCard.getY();
							int x1 = mSolitaire.getDestinationStack(i).getX();
							int y1 = mSolitaire.getDestinationStack(i).getY();
							int mpx = (x0 + x1) / 2;
							int mpy = (y0 + y1) / 2;
							int pmpx = (x0 + mpx) / 2;
							int pmpy = (y0 + mpy) / 2;
							int ampx = (x1 + mpx) / 2;
							int ampy = (y1 + mpy) / 2;
							
							lastCard.moveTo(pmpx, pmpy);
							publishProgress();
							sleep(20);
							lastCard.moveTo(mpx, mpy);
							publishProgress();
							sleep(20);
							lastCard.moveTo(ampx, ampy);
							publishProgress();
							sleep(20);
							lastCard.moveTo(x1, y1);
							publishProgress();
							sleep(20);

							mSolitaire.getWorkingStack(j).removeLast();
							mSolitaire.getDestinationStack(i).add(lastCard);
							publishProgress();
							break;
						}
					}
				}
			}
			
			// move all cards to destination stacks
			//int count = solitaire.getWorkingStack(i).getCardCount();
			for (int j=0; j<13*Solitaire.WORKING_STACK_NUM; ++j) {
				int v = 14;
				int idx = -1;
				Card minCard = null;
				for (int i=0; i<Solitaire.WORKING_STACK_NUM; ++i) {
					Card lastCard = mSolitaire.getWorkingStack(i).getLast();
					if (lastCard == null)
						continue;
					if (lastCard.getValue() < v) {
						v = lastCard.getValue();
						minCard = lastCard;
						idx = i;
					}
				}
				
				if (minCard == null)
					break;
				
				for (int k=0; k<Solitaire.DESTINATION_STACK_NUM; ++k) {						
					if (minCard.getType() == mSolitaire.getDestinationStack(k).getType()) {
						int x0 = minCard.getX();
						int y0 = minCard.getY();
						int x1 = mSolitaire.getDestinationStack(k).getX();
						int y1 = mSolitaire.getDestinationStack(k).getY();
						int mpx = (x0 + x1) / 2;
						int mpy = (y0 + y1) / 2;
						int pmpx = (x0 + mpx) / 2;
						int pmpy = (y0 + mpy) / 2;
						int ampx = (x1 + mpx) / 2;
						int ampy = (y1 + mpy) / 2;
						
						minCard.moveTo(pmpx, pmpy);
						publishProgress();
						sleep(20);
						minCard.moveTo(mpx, mpy);
						publishProgress();
						sleep(20);
						minCard.moveTo(ampx, ampy);
						publishProgress();
						sleep(20);
						minCard.moveTo(x1, y1);
						publishProgress();
						sleep(20);

						mSolitaire.getWorkingStack(idx).removeLast();
						mSolitaire.getDestinationStack(k).add(minCard);
						publishProgress();
						break;
					}
					if (isCancelled())
						break;
				}
				if (isCancelled())
					break;
			}

			if (!isCancelled()) {
				// all cards dance
				Random rnd = new Random();
				int[] outCards = new int[Solitaire.DESTINATION_STACK_NUM*13];
				int[] deltaX = new int[Solitaire.DESTINATION_STACK_NUM*13];
				int[] deltaY = new int[Solitaire.DESTINATION_STACK_NUM*13];
				int[] delta = new int[Solitaire.DESTINATION_STACK_NUM*13];
				int[] xs = new int[Solitaire.DESTINATION_STACK_NUM*13];
				int[] ys = new int[Solitaire.DESTINATION_STACK_NUM*13];
				for (int i=12; i>=0; --i) {
					for (int j=0; j<Solitaire.DESTINATION_STACK_NUM; ++j) {
						Card card = mSolitaire.getDestinationStack(j).getCard(i);
						int index = j*13+i;
						outCards[index] = 1;
						deltaX[index] = ((i+j)%2 == 0 ? 1 : -1);
						deltaY[index] = ((i+j)%2 == 0 ? 1 : -1);
						delta[index] = rnd.nextInt(24)+1;
						xs[index] = card.getX();
						ys[index] = card.getY();
					}
				}
				
				boolean allOut = false;
				for (int k=0; k<10000; k++) {
					for (int i=12; i>=0; --i) {
						for (int j=0; j<Solitaire.DESTINATION_STACK_NUM; ++j) {
							int index = j*13+i;
	
							xs[index] += delta[index]*deltaX[index];
							ys[index] += delta[index]*deltaY[index];
							
							if (k<128) {
								if (xs[index] <= 0) {
									xs[index] = 0;
									deltaX[index] *= -1;
								}
								else if (xs[index]+mSolitaire.getCardWidth() >= mSolitaire.getSceneViewWidth()) {
									xs[index] = mSolitaire.getSceneViewWidth() - mSolitaire.getCardWidth();
									deltaX[index] *= -1;
								}
		
								if (ys[index] <= 0) {
									ys[index] = 0;
									deltaY[index] *= -1;
								}
								else if (ys[index]+mSolitaire.getCardHeight() >= mSolitaire.getSceneViewHeight()) {
									ys[index] = mSolitaire.getSceneViewHeight() - mSolitaire.getCardHeight();
									deltaY[index] *= -1;
								}
							}
							else {
								deltaX[index] = 0;
								deltaY[index] = 1;
								delta[index] = i*3 + 8;
								
								if (ys[index] > mSolitaire.getSceneViewHeight())
									outCards[index] = 0;
							}
							mSolitaire.getDestinationStack(j).getCard(i).moveTo(xs[index], ys[index]);
							publishProgress();
							
							int count = 0;
							for (int n =0; n<Solitaire.DESTINATION_STACK_NUM*13; ++n)
								count += outCards[n];
							allOut = (count == 0);
							
							if (isCancelled() || allOut)
								break;
						}
						if (isCancelled() || allOut)
							break;
					}		
					if (isCancelled() || allOut)
						break;
					sleep(20);
				}
			}
			
			return null;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			asyncStop();			
			mSolitaire.clear();
			invalidate();
			MainActivity activity = (MainActivity)getContext();
			activity.showNewGameButton(true);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
		 */
		@Override
		protected void onCancelled(Void result) {
			super.onCancelled(result);
			asyncStop();
			mSolitaire.clear();
			invalidate();
			MainActivity activity = (MainActivity)getContext();
			activity.showNewGameButton(true);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			invalidate();
		}
	}

}
