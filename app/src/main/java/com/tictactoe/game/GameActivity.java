package com.tictactoe.game;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private GameLogic gameLogic;
    private AIPlayer aiPlayer;
    private GridLayout gameBoard;
    private BoardView boardView;
    private TextView turnTextView;
    private MaterialButton backButton;
    private String gameMode;
    private boolean isAITurn = false;
    private Handler handler;
    private SoundManager soundManager;
    private SharedPreferencesHelper prefsHelper;
    private MaterialButton[][] boardButtons;
    private int[][] boardState;
    private int boardSize;
    private int startingPlayer = 1;
    private View[] verticalLines;
    private View[] horizontalLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        prefsHelper = new SharedPreferencesHelper(this);
        soundManager = new SoundManager(this, prefsHelper);
        handler = new Handler(Looper.getMainLooper());

        gameMode = getIntent().getStringExtra("mode");
        boardSize = getIntent().getIntExtra("boardSize", 3);

        gameLogic = new GameLogic(boardSize);
        startingPlayer = new Random().nextBoolean() ? 1 : 2;
        gameLogic.resetWithStartingPlayer(startingPlayer);
        if ("pve".equals(gameMode)) {
            aiPlayer = new AIPlayer(boardSize);
        }

        boardView = findViewById(R.id.boardView);
        gameBoard = findViewById(R.id.gameBoard);
        turnTextView = findViewById(R.id.turnTextView);
        backButton = findViewById(R.id.backButton);

        if (boardView != null) {
            boardView.setBoardSize(boardSize);
        }

        boardButtons = new MaterialButton[boardSize][boardSize];
        boardState = new int[boardSize][boardSize];

        setupGameBoard();
        updateTurnDisplay();

        backButton.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.SOUND_SELECT);
            finish();
        });
    }

    private void setupGameBoard() {
        gameBoard.removeAllViews();

        gameBoard.setColumnCount(boardSize);
        gameBoard.setRowCount(boardSize);
        gameBoard.setUseDefaultMargins(false);
        gameBoard.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        gameBoard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameBoard.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int gridWidth = gameBoard.getWidth();
                int gridHeight = gameBoard.getHeight();
                int size = Math.min(gridWidth, gridHeight);
                int offsetX = (gridWidth - size) / 2;
                int offsetY = (gridHeight - size) / 2;
                gameBoard.setPadding(offsetX, offsetY, offsetX, offsetY);

                int cellSize = size / boardSize;

                for (int row = 0; row < boardSize; row++) {
                    for (int col = 0; col < boardSize; col++) {
                        MaterialButton button = createCellButton(row, col, cellSize);
                        boardButtons[row][col] = button;
                        boardState[row][col] = 0;

                        GridLayout.Spec rowSpec = GridLayout.spec(row, 1f);
                        GridLayout.Spec colSpec = GridLayout.spec(col, 1f);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                        params.width = cellSize;
                        params.height = cellSize;
                        params.setMargins(0, 0, 0, 0);
                        button.setLayoutParams(params);
                        
                        gameBoard.addView(button);
                    }
                }

                scheduleAIMoveIfNeeded();
            }
        });
    }

    private MaterialButton createCellButton(int row, int col, int size) {
        MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);

        button.setText("");

        float textSize = boardSize == 3 ? 48 : (boardSize == 4 ? 36 : 28);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setCornerRadius(0);

        button.setBackgroundColor(Color.TRANSPARENT);
        button.setBackground(null);
        button.setRippleColor(null);
        button.setElevation(0f);
        button.setStateListAnimator(null);
        button.setForeground(null);
        button.setClickable(true);
        button.setFocusable(true);
        button.setSoundEffectsEnabled(true);
        button.setAllCaps(false);

        button.setTag(new int[]{row, col});

        button.setOnClickListener(this::onCellClick);

        button.setScaleX(1f);
        button.setScaleY(1f);
        button.setAlpha(1f);
        
        return button;
    }

    private void onCellClick(View view) {
        if (isAITurn || gameLogic.isGameOver()) {
            return;
        }

        int[] position = (int[]) view.getTag();
        int row = position[0];
        int col = position[1];

        // Проверяем, не занята ли ячейка
        if (boardState[row][col] != 0) {
            return;
        }

        // Сохраняем текущего игрока перед ходом
        int playerWhoMoved = gameLogic.getCurrentPlayer();

        // Делаем ход в логике игры
        if (gameLogic.makeMove(row, col)) {
            MaterialButton button = (MaterialButton) view;
            
            // Обновляем состояние доски
            boardState[row][col] = playerWhoMoved;
            
            // Отображаем символ
            String symbol = playerWhoMoved == 1 ? "X" : "O";
            button.setText(symbol);
            button.setTextColor(playerWhoMoved == 1 ? 
                ContextCompat.getColor(this, R.color.x_color) : 
                ContextCompat.getColor(this, R.color.o_color));
            button.setEnabled(false);

            animateCellAppearance(button);
            soundManager.playSound(SoundManager.SOUND_MOVE);

            if (gameLogic.isGameOver()) {
                if (gameLogic.getWinner() == 0) {
                    // Ничья: автоматически очищаем поле и меняем первого игрока
                    handleDraw();
                } else {
                    handleGameEnd();
                }
            } else {
                updateTurnDisplay();
                scheduleAIMoveIfNeeded();
            }
        }
    }

    private void makeAIMove() {
        if (gameLogic.isGameOver()) {
            setBoardEnabled(true);
            isAITurn = false;
            return;
        }

        int[][] boardCopy = new int[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(gameLogic.getBoard()[i], 0, boardCopy[i], 0, boardSize);
        }

        int[] move = aiPlayer.makeMove(boardCopy);

        if (move != null && move[0] >= 0 && move[0] < boardSize &&
                move[1] >= 0 && move[1] < boardSize &&
                boardState[move[0]][move[1]] == 0) {

            if (gameLogic.makeMove(move[0], move[1])) {
                MaterialButton button = boardButtons[move[0]][move[1]];

                boardState[move[0]][move[1]] = 2;

                button.setText("O");
                button.setTextColor(ContextCompat.getColor(this, R.color.o_color));
                button.setEnabled(false);

                animateCellAppearance(button);
                soundManager.playSound(SoundManager.SOUND_MOVE);

                if (gameLogic.isGameOver()) {
                    if (gameLogic.getWinner() == 0) {
                        handleDraw();
                    } else {
                        handleGameEnd();
                    }
                } else {
                    updateTurnDisplay();
                    isAITurn = false;
                    setBoardEnabled(true);
                }
            } else {
                isAITurn = false;
                setBoardEnabled(true);
            }
        } else {
            isAITurn = false;
            setBoardEnabled(true);
        }
    }

    private boolean hasAvailableMoves() {
        int[][] board = gameLogic.getBoard();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void scheduleAIMoveIfNeeded() {
        if (!"pve".equals(gameMode)) return;
        if (aiPlayer == null) return;
        if (gameLogic.isGameOver()) return;
        if (gameLogic.getCurrentPlayer() != 2) return;
        if (!hasAvailableMoves()) return;

        gameBoard.post(() -> handler.postDelayed(() -> {
            try {
                isAITurn = true;
                setBoardEnabled(false);
                makeAIMove();
            } catch (Exception e) {
                e.printStackTrace();
                isAITurn = false;
                setBoardEnabled(true);
            }
        }, 150));
    }

    private void setBoardEnabled(boolean enabled) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (boardState[i][j] == 0) {
                    boardButtons[i][j].setEnabled(enabled);
                }
            }
        }
    }

    private void animateCellAppearance(MaterialButton button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0f, 1.1f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator(1.5f));
        animatorSet.start();
    }

    // Обработка ничьей: очистка поля и смена первого ходящего игрока.
    private void handleDraw() {
        startingPlayer = new Random().nextBoolean() ? 1 : 2;

        gameLogic.resetWithStartingPlayer(startingPlayer);
        resetBoard();
        updateTurnDisplay();
        scheduleAIMoveIfNeeded();
    }

    private void handleGameEnd() {
        int winner = gameLogic.getWinner();
        List<int[]> winningLine = gameLogic.getWinningLine();

        if (winningLine != null && !winningLine.isEmpty()) {
            highlightWinningLine(winningLine);
        }

        String title;
        String message;
        int cupsChange = 0;
        int oldCups = prefsHelper.getCups();

        if ("pve".equals(gameMode)) {
            if (winner == 1) {
                // игрок победил
                soundManager.playSound(SoundManager.SOUND_WIN);

                title = getString(R.string.you_win);
                int reward = Math.max(2, 30 - oldCups / 50);
                prefsHelper.addCups(reward);
                cupsChange = reward;
                message = getString(R.string.cups_change_positive, reward);
            } else {
                // игрок проиграл
                soundManager.playSound(SoundManager.SOUND_LOSE); // ← ВАЖНО

                title = getString(R.string.you_lose);
                int penalty = Math.min(40, 10 + oldCups / 50);
                prefsHelper.addCups(-penalty);
                cupsChange = -penalty;
                message = getString(R.string.cups_change_negative, penalty);
            }
        } else {
            // PvP
            soundManager.playSound(SoundManager.SOUND_WIN);

            String winnerSymbol = winner == 1 ? "X" : "O";
            title = getString(R.string.player_wins, winnerSymbol);
            message = "";
        }

        showGameEndDialog(title, message, cupsChange);
    }


    private void showGameEndDialog(String title, String cupsMessage, int cupsChange) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        String fullMessage = title;
        if (!cupsMessage.isEmpty()) {
            fullMessage += "\n" + cupsMessage;
        }
        builder.setMessage(fullMessage);

        builder.setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                soundManager.playSound(SoundManager.SOUND_SELECT);
                startingPlayer = new Random().nextBoolean() ? 1 : 2;
                gameLogic.resetWithStartingPlayer(startingPlayer);
                resetBoard();
                updateTurnDisplay();

                scheduleAIMoveIfNeeded();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.to_main_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                soundManager.playSound(SoundManager.SOUND_SELECT);
                finish();
            }
        });
        
        builder.setCancelable(false);
        builder.show();
    }

    private void highlightWinningLine(List<int[]> winningLine) {
        for (int[] pos : winningLine) {
            if (pos[0] >= 0 && pos[0] < boardSize && pos[1] >= 0 && pos[1] < boardSize) {
                MaterialButton button = boardButtons[pos[0]][pos[1]];
                animateWinHighlight(button);
            }
        }
    }

    private void animateWinHighlight(MaterialButton button) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.5f, 1f);
        animator.setDuration(500);
        animator.setRepeatCount(3);
        animator.start();
    }

    private void createGridLines() {
        ConstraintLayout root = (ConstraintLayout) gameBoard.getParent();

        if (verticalLines != null) {
            for (View v : verticalLines) {
                if (v != null) {
                    root.removeView(v);
                }
            }
        }
        if (horizontalLines != null) {
            for (View h : horizontalLines) {
                if (h != null) {
                    root.removeView(h);
                }
            }
        }

        int gridLeft = gameBoard.getLeft();
        int gridTop = gameBoard.getTop();
        int gridWidth = gameBoard.getWidth();
        int gridHeight = gameBoard.getHeight();

        if (gridWidth <= 0 || gridHeight <= 0 || boardSize <= 1) {
            return;
        }

        int cellWidth = gridWidth / boardSize;
        int cellHeight = gridHeight / boardSize;

        int lineThickness = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int lineColor = ContextCompat.getColor(this, R.color.win_line_color);

        verticalLines = new View[boardSize - 1];
        horizontalLines = new View[boardSize - 1];

        // Вертикальные линии
        for (int i = 1; i < boardSize; i++) {
            View line = new View(this);
            line.setBackgroundColor(lineColor);

            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                    lineThickness, gridHeight);
            lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.leftMargin = gridLeft + i * cellWidth - lineThickness / 2;
            lp.topMargin = gridTop;

            root.addView(line, lp);
            verticalLines[i - 1] = line;
        }

        // Горизонтальные линии
        for (int i = 1; i < boardSize; i++) {
            View line = new View(this);
            line.setBackgroundColor(lineColor);

            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                    gridWidth, lineThickness);
            lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.leftMargin = gridLeft;
            lp.topMargin = gridTop + i * cellHeight - lineThickness / 2;

            root.addView(line, lp);
            horizontalLines[i - 1] = line;
        }

        gameBoard.bringToFront();
    }

    private boolean isBoardEmpty() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (boardState[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void resetBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                MaterialButton button = boardButtons[i][j];
                button.setText("");
                button.setEnabled(true);
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setAlpha(1f);
                button.setScaleX(1f);
                button.setScaleY(1f);
                boardState[i][j] = 0;
            }
        }
        isAITurn = false;
    }

    private void updateTurnDisplay() {
        int currentPlayer = gameLogic.getCurrentPlayer();
        String symbol = currentPlayer == 1 ? "X" : "O";
        turnTextView.setText(getString(R.string.player_turn, symbol));

        int colorRes = (currentPlayer == 1) ? R.color.x_color : R.color.o_color;
        turnTextView.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
    }
}
