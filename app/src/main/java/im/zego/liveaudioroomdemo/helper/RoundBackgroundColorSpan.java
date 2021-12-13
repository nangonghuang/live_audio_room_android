package im.zego.liveaudioroomdemo.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SizeUtils;

public class RoundBackgroundColorSpan extends ReplacementSpan {
    private int bgColor;
    private int textColor;
    private int corner;
    private int paddingStart;

    public RoundBackgroundColorSpan(int bgColor, int textColor) {
        super();
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.corner = SizeUtils.dp2px(2);
        this.paddingStart = SizeUtils.dp2px(3);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return ((int) paint.measureText(text, start, end) + paddingStart * 3);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        int color1 = paint.getColor();
        paint.setColor(this.bgColor);
        canvas.drawRoundRect(new RectF(x, top, x + ((int) paint.measureText(text, start, end) + paddingStart * 2), bottom), corner, corner, paint);
        paint.setColor(this.textColor);
        canvas.drawText(text, start, end, x + paddingStart, y, paint);
        paint.setColor(color1);
    }
}