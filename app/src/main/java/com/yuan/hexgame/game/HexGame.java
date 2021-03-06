package com.yuan.hexgame.game;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.yuan.hexgame.R;
import com.yuan.hexgame.ui.widget.Avatar;
import com.yuan.hexgame.ui.widget.HexView;
import com.yuan.hexgame.util.LogUtil;
import com.yuan.hexgame.util.RandomUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yuan Sun on 2015/9/30.
 */
public class HexGame implements Game {

    private static final String TAG = "HexGame";

    private Context mContext;
    private Handler mMainThreadHandler;

    private Board mBoard;

    private Player mFirstPlayer = Player.A;
    private Player mCurrentPlayer;

    private GameSettings mSettings;

    private Robot mRobot;

    private HexView[] mHexViews; // 1 ~ NxN
    private Avatar mPlayerAAvatar;
    private Avatar mPlayerBAvatar;
    private Animation mAvatarAnim;

    private OnGameOverListener mOnGameOverListener;

    private boolean mIsRobotTurn = false;
    private boolean isGameOver = false;
    private Player mWinner;

    private List<Integer> mOccupiedViewIds = new LinkedList<>();

    public HexGame(Context context, int n, HexView[] hexViews, Avatar playerA, Avatar playerB) {
        mContext = context;
        mMainThreadHandler = new Handler(context.getMainLooper());
        mBoard = new ChessBoard(n);
        mHexViews = hexViews;
        mPlayerAAvatar = playerA;
        mPlayerBAvatar = playerB;
        mSettings = GameSettings.getInstance();
        mFirstPlayer = newGameFirstPlayer();
        mCurrentPlayer = mFirstPlayer;
        if (mSettings.getGameMode() == GameSettings.MODE_HUMAN_VS_ROBOT) {
            mRobot = new MonteCarloRobot(Player.B);
        }
//        mPlayerAAvatar.setLayerType(HexView.LAYER_TYPE_SOFTWARE, null);
//        mPlayerBAvatar.setLayerType(HexView.LAYER_TYPE_SOFTWARE, null);
        mAvatarAnim = AnimationUtils.loadAnimation(context, R.anim.avatar_breath);
//        mAvatarAnim = new AlphaAnimation(1f, 0.4f);
//        mAvatarAnim.setDuration(500);
//        mAvatarAnim.setRepeatMode(Animation.REVERSE);
//        mAvatarAnim.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public void start() {
        Toast.makeText(mContext, mFirstPlayer == Player.A ? mContext.getString(R.string.toast_msg_a_play_first) : mContext.getString(R.string.toast_msg_b_play_first), Toast.LENGTH_SHORT).show();
        if (mSettings.getGameMode() == GameSettings.MODE_HUMAN_VS_ROBOT
                && mFirstPlayer == Player.B) {
            int num = mBoard.getChessNum();
            int firstPos = 0;
            if (num % 2 == 1) {
                firstPos = num / 2 + 1;
            } else {
                firstPos = num / 2 + (int) Math.sqrt(num) / 2;
            }
            doPutPiece(firstPos);
        }
        if (mCurrentPlayer == Player.A) {
            mPlayerAAvatar.startAnimation(mAvatarAnim);
            mPlayerBAvatar.clearAnimation();
        } else {
            mPlayerBAvatar.startAnimation(mAvatarAnim);
            mPlayerAAvatar.clearAnimation();
        }
    }

    @Override
    public void putPiece(int id) {
        if (mBoard.isOccupied(id) || mIsRobotTurn)
            return;
//        mOccupiedViewIds.add(id);
//        mBoard.setOwner(id, mCurrentPlayer);
//        mHexViews[id].setOwner(mCurrentPlayer);
//        mCurrentPlayer = mCurrentPlayer.component();
        doPutPiece(id);
        if (mSettings.getGameMode() == GameSettings.MODE_HUMAN_VS_ROBOT && (!isGameOver)) {
//            int robotChessPos = mRobot.getChessPos(mBoard);
//            mOccupiedViewIds.add(robotChessPos);
//            mBoard.setOwner(robotChessPos, mCurrentPlayer);
//            mHexViews[robotChessPos].setOwner(mCurrentPlayer);
//            mCurrentPlayer = mCurrentPlayer.component();
//            new RobotTask().execute();
            mIsRobotTurn = true;
            final long startT = System.currentTimeMillis();
            mRobot.compute(mBoard, new Robot.RobotStatusListener() {
                @Override
                public void onStart() {
//                    Animator alphaDownAnim = ObjectAnimator.ofFloat(mPlayerBAvatar, "alpha", 1f, 0.5f);
//                    alphaDownAnim.setDuration(500);
//                    Animator alphaUpAnim = ObjectAnimator.ofFloat(mPlayerBAvatar, "alpha", 0.5f, 1f);
//                    alphaUpAnim.setDuration(500);
//                    Animator sizeLargeAnim = ObjectAnimator.ofInt(mPlayerBAvatar, "diameter", 120, 150);
//                    sizeLargeAnim.setDuration(500);
//                    Animator sizeSmallAnim = ObjectAnimator.ofInt(mPlayerBAvatar, "diameter", 150, 120);
//                    sizeSmallAnim.setDuration(500);
//                    AnimatorSet breadth = new AnimatorSet();
//                    breadth.play(alphaDownAnim).with(sizeLargeAnim);
//                    breadth.play(alphaUpAnim).with(sizeSmallAnim);
//                    breadth.play(alphaDownAnim).before(alphaUpAnim);
//                    breadth.start();

//                    AlphaAnimation alphaAnim = new AlphaAnimation(1f, 0.4f);
//                    alphaAnim.setDuration(500);
//                    alphaAnim.setRepeatCount(Animation.INFINITE);
//                    alphaAnim.setRepeatMode(Animation.REVERSE);
//                    mPlayerBAvatar.startAnimation(alphaAnim);

                    mPlayerAAvatar.clearAnimation();
                    mPlayerBAvatar.startAnimation(mAvatarAnim);
                }

                @Override
                public void onCompleted(int optimalPos) {
                    doPutPiece(optimalPos);
                    long endT = System.currentTimeMillis();
                    LogUtil.i(TAG, "Robot cost " + (endT - startT) + "ms");
                    mPlayerBAvatar.clearAnimation();
                    mPlayerAAvatar.startAnimation(mAvatarAnim);
                    mIsRobotTurn = false;
                }
            });
        } else if (mSettings.getGameMode() == GameSettings.MODE_HUMAN_VS_HUMAN && (!isGameOver)) {
            if (mCurrentPlayer == Player.A) {
                mPlayerAAvatar.startAnimation(mAvatarAnim);
                mPlayerBAvatar.clearAnimation();
            } else {
                mPlayerBAvatar.startAnimation(mAvatarAnim);
                mPlayerAAvatar.clearAnimation();
            }
        }
    }

    private void doPutPiece(int id) {
        mOccupiedViewIds.add(id);
        mBoard.setOwner(id, mCurrentPlayer);
        mHexViews[id].setOwner(mCurrentPlayer);
        mCurrentPlayer = mCurrentPlayer.component();
        if (isAWin()) {
            mWinner = Player.A;
            isGameOver = true;
            if (mOnGameOverListener != null) {
                mOnGameOverListener.onGameOver(mWinner);
            }
        } else if (isBWin()) {
            mWinner = Player.B;
            isGameOver = true;
            if (mOnGameOverListener != null) {
                mOnGameOverListener.onGameOver(mWinner);
            }
        }
    }

    @Override
    public boolean isAWin() {
        return mBoard.isAWin();
    }

    @Override
    public boolean isBWin() {
        return mBoard.isBWin();
    }

    @Override
    public void restart() {
        mBoard.restart();
        int i = 0;
        Collections.shuffle(mOccupiedViewIds);
        for (Integer id : mOccupiedViewIds) {
            final HexView view = mHexViews[id];
            view.postDelayed(new Runnable() {

                @Override
                public void run() {
                    ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
                    anim.setDuration(100);
                    anim.start();
                }
            }, 20 * i);
            i++;
        }
        mOccupiedViewIds.clear();
        isGameOver = false;
        mWinner = null;
        mCurrentPlayer = newGameFirstPlayer();
        mMainThreadHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                start();
            }
        }, mOccupiedViewIds.size() * 20 + 1000);
    }

    @Override
    public void setOnGameOverListener(OnGameOverListener onGameOverListener) {
        mOnGameOverListener = onGameOverListener;
    }

    private Player newGameFirstPlayer() {
        switch (mSettings.getFirstPlayerMode()) {
            case GameSettings.FIRST_PLAYER_TAKE_TURNS:
                return mFirstPlayer = mFirstPlayer.component();
            case GameSettings.FIRST_PLAYER_RANDOM:
                return mFirstPlayer = RandomUtil.getInt(2) == 1 ? Player.B : Player.A;
            case GameSettings.FIRST_PLAYER_A:
                return mFirstPlayer = Player.A;
            case GameSettings.FIRST_PLAYER_B:
            default:
                return mFirstPlayer = Player.B;
        }
    }

    private class RobotTask extends AsyncTask<Void, Integer, Integer> {

        private long startT;
        private long endT;

        @Override
        protected Integer doInBackground(Void... params) {
            startT = System.currentTimeMillis();
            return mRobot.getChessPos(mBoard);
        }

        @Override
        protected void onPostExecute(Integer id) {
            doPutPiece(id);
            endT = System.currentTimeMillis();
            LogUtil.i(TAG, "Robot cost " + (endT - startT) + "ms");
        }
    }

}
