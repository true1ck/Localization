
// GridImageView.java
package com.example.loc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

public class GridImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Paint gridPaint;
    private Paint highlightPaint;
    private Paint textPaint;
    private String highlightedCell = null;
    private static final int ROWS = 3;
    private static final int COLS = 3;

    public GridImageView(Context context) {
        super(context);
        init();
    }

    public GridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.argb(100, 33, 150, 243)); // Semi-transparent blue
        highlightPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float cellWidth = width / COLS;
        float cellHeight = height / ROWS;

        // Draw highlighted cell first (if any)
        if (highlightedCell != null) {
            int row = Character.getNumericValue(highlightedCell.charAt(1)) - 1;
            int col = Character.getNumericValue(highlightedCell.charAt(2)) - 1;

            RectF rect = new RectF(
                    col * cellWidth,
                    row * cellHeight,
                    (col + 1) * cellWidth,
                    (row + 1) * cellHeight
            );
            canvas.drawRect(rect, highlightPaint);
        }

        // Draw grid lines
        for (int i = 1; i < ROWS; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, gridPaint);
        }
        for (int i = 1; i < COLS; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, gridPaint);
        }

        // Draw cell labels
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String label = String.format("a%d%d", row + 1, col + 1);
                float x = col * cellWidth + cellWidth / 2;
                float y = row * cellHeight + cellHeight / 2 + textPaint.getTextSize() / 3;
                canvas.drawText(label, x, y, textPaint);
            }
        }
    }

    public void highlightCell(String cell) {
        this.highlightedCell = cell;
        invalidate();
    }

    public void clearHighlight() {
        this.highlightedCell = null;
        invalidate();
    }
}
