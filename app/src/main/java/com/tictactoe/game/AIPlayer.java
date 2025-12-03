package com.tictactoe.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    private Random random;
    private int boardSize;

    public AIPlayer(int boardSize) {
        this.random = new Random();
        this.boardSize = boardSize;
    }

    public int[] makeMove(int[][] board) {
        // Сначала проверяем, можем ли мы выиграть
        int[] winningMove = findWinningMove(board, 2); // 2 = O (ИИ)
        if (winningMove != null) {
            return winningMove;
        }

        // Затем блокируем победный ход игрока
        int[] blockingMove = findWinningMove(board, 1); // 1 = X (игрок)
        if (blockingMove != null) {
            return blockingMove;
        }

        // Иначе делаем случайный ход
        return makeRandomMove(board);
    }

    private int[] findWinningMove(int[][] board, int player) {
        // Проверяем все возможные ходы
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 0) {
                    // Пробуем поставить символ
                    board[i][j] = player;
                    if (wouldWin(board, i, j, player)) {
                        board[i][j] = 0;
                        return new int[]{i, j};
                    }
                    board[i][j] = 0;
                }
            }
        }
        return null;
    }

    private boolean wouldWin(int[][] board, int row, int col, int player) {
        // Проверка строки
        boolean win = true;
        for (int c = 0; c < boardSize; c++) {
            if (board[row][c] != player) {
                win = false;
                break;
            }
        }
        if (win) return true;

        // Проверка столбца
        win = true;
        for (int r = 0; r < boardSize; r++) {
            if (board[r][col] != player) {
                win = false;
                break;
            }
        }
        if (win) return true;

        // Проверка главной диагонали
        if (row == col) {
            win = true;
            for (int i = 0; i < boardSize; i++) {
                if (board[i][i] != player) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }

        // Проверка побочной диагонали
        if (row + col == boardSize - 1) {
            win = true;
            for (int i = 0; i < boardSize; i++) {
                if (board[i][boardSize - 1 - i] != player) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }

        return false;
    }

    private int[] makeRandomMove(int[][] board) {
        List<int[]> availableMoves = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 0) {
                    availableMoves.add(new int[]{i, j});
                }
            }
        }

        if (availableMoves.isEmpty()) {
            return null;
        }

        return availableMoves.get(random.nextInt(availableMoves.size()));
    }
}

