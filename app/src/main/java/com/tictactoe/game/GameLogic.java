package com.tictactoe.game;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private int boardSize;
    private int[][] board;
    private int currentPlayer; // 1 = X, 2 = O
    private boolean gameOver;
    private int winner;
    private List<int[]> winningLine;

    public GameLogic(int boardSize) {
        this.boardSize = boardSize;
        this.board = new int[boardSize][boardSize];
        this.currentPlayer = 1; // X начинает
        this.gameOver = false;
        this.winner = 0;
        this.winningLine = new ArrayList<>();
    }

    public boolean makeMove(int row, int col) {
        if (gameOver || board[row][col] != 0) {
            return false;
        }

        board[row][col] = currentPlayer;

        if (checkWin(row, col)) {
            gameOver = true;
            winner = currentPlayer;
            return true;
        }

        if (isBoardFull()) {
            gameOver = true;
            winner = 0;
            return true;
        }

        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        return true;
    }

    private boolean checkWin(int row, int col) {
        int player = board[row][col];
        winningLine.clear();

        // Проверка строки
        List<int[]> line = new ArrayList<>();
        boolean win = true;
        for (int c = 0; c < boardSize; c++) {
            if (board[row][c] != player) {
                win = false;
                break;
            }
            line.add(new int[]{row, c});
        }
        if (win) {
            winningLine = line;
            return true;
        }

        // Проверка столбца
        line = new ArrayList<>();
        win = true;
        for (int r = 0; r < boardSize; r++) {
            if (board[r][col] != player) {
                win = false;
                break;
            }
            line.add(new int[]{r, col});
        }
        if (win) {
            winningLine = line;
            return true;
        }

        // Проверка главной диагонали
        if (row == col) {
            line = new ArrayList<>();
            win = true;
            for (int i = 0; i < boardSize; i++) {
                if (board[i][i] != player) {
                    win = false;
                    break;
                }
                line.add(new int[]{i, i});
            }
            if (win) {
                winningLine = line;
                return true;
            }
        }

        // Проверка побочной диагонали
        if (row + col == boardSize - 1) {
            line = new ArrayList<>();
            win = true;
            for (int i = 0; i < boardSize; i++) {
                if (board[i][boardSize - 1 - i] != player) {
                    win = false;
                    break;
                }
                line.add(new int[]{i, boardSize - 1 - i});
            }
            if (win) {
                winningLine = line;
                return true;
            }
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void clearBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = 0;
            }
        }
        winningLine.clear();
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinner() {
        return winner;
    }

    public List<int[]> getWinningLine() {
        return winningLine;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void reset() {
        clearBoard();
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        winningLine.clear();
    }

    public void resetWithStartingPlayer(int startingPlayer) {
        clearBoard();
        currentPlayer = startingPlayer;
        gameOver = false;
        winner = 0;
        winningLine.clear();
    }
}

