package com.tictactoe.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

public class BoardView extends View {
    private int boardSize = 3;
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BoardView(Context context) {
        super(context);
        init(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(ContextCompat.getColor(context, R.color.win_line_color));
        float strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        linePaint.setStrokeWidth(strokeWidth);
    }

    public void setBoardSize(int boardSize) {
        if (boardSize <= 0) return;
        this.boardSize = boardSize;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (boardSize < 2) return;

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        float offsetX = (width - size) / 2f;
        float offsetY = (height - size) / 2f;
        float cellSize = size / (float) boardSize;

        // Вертикальные линии
        for (int i = 1; i < boardSize; i++) {
            float x = offsetX + i * cellSize;
            canvas.drawLine(x, offsetY, x, offsetY + size, linePaint);
        }

        // Горизонтальные линии
        for (int i = 1; i < boardSize; i++) {
            float y = offsetY + i * cellSize;
            canvas.drawLine(offsetX, y, offsetX + size, y, linePaint);
        }
    }
}


