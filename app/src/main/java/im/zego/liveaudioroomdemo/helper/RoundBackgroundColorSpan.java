package im.zego.liveaudioroomdemo.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.SizeUtils;

public class RoundBackgroundColorSpan extends ReplacementSpan {

    private int bgColor;
    private int textColor;
    private int corner;
    private int paddingStart;
    private int paddingTop;
    private Rect textRect = new Rect();
    private RectF bgRectF = new RectF();

    public RoundBackgroundColorSpan(int bgColor, int textColor) {
        super();
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.corner = SizeUtils.dp2px(2);
        this.paddingStart = SizeUtils.dp2px(3);
        this.paddingTop = SizeUtils.dp2px(2);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        textRect.setEmpty();
        paint.getTextBounds(text.toString(), start, end, textRect);
        return ((int) textRect.width() + paddingStart * 3);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
        @NonNull Paint paint) {
        int color1 = paint.getColor();
        paint.setColor(this.bgColor);
        textRect.setEmpty();
        bgRectF.setEmpty();
        paint.getTextBounds(text.toString(), start, end, textRect);
        FontMetrics fontMetrics = paint.getFontMetrics();
        bgRectF.set(x, y + fontMetrics.ascent - paddingTop, x + textRect.width() + 2 * paddingStart,
            y + fontMetrics.descent + paddingTop);
        canvas.drawRoundRect(bgRectF, corner, corner, paint);
        paint.setColor(this.textColor);
        canvas.drawText(text, start, end, x + paddingStart, y, paint);
        paint.setColor(color1);
    }
}