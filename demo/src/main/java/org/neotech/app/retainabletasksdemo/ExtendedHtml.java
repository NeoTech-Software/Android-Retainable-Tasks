package org.neotech.app.retainabletasksdemo;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import org.xml.sax.XMLReader;

/**
 * Created by Rolf on 17-3-2016.
 */
public final class ExtendedHtml {

    private static final ExtendedTagHandler TAG_HANDLER = new ExtendedTagHandler();

    public static Spanned fromHtml(String source){
        return Html.fromHtml(source, null, TAG_HANDLER);
    }

    private static class CodeSpan extends TypefaceSpan {

        private final int backgroundColor;

        public CodeSpan(int backgroundColor){
            super("monospace");
            this.backgroundColor = backgroundColor;
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            super.updateDrawState(tp);
            tp.bgColor = backgroundColor;
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            super.updateMeasureState(paint);
            paint.bgColor = backgroundColor;
        }
    }

    /**
     * Based on: http://stackoverflow.com/questions/4044509/android-how-to-use-the-html-taghandler
     */
    private static class ExtendedTagHandler implements Html.TagHandler {

        private static final int COLOR = 0xFFCCCCCC;

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if(tag.equals("pre") || tag.equals("code")){
                if(opening){
                    start((SpannableStringBuilder) output, new CodeSpan(COLOR));
                } else {
                    end((SpannableStringBuilder) output,CodeSpan.class, new CodeSpan(COLOR));
                }
            }
        }

        private static <T> Object getLast(@NonNull Spanned text, final @NonNull Class<T> kind) {
            final Object[] objects = text.getSpans(0, text.length(), kind);
            if (objects.length == 0) {
                return null;
            } else {
                return objects[objects.length - 1];
            }
        }

        private static void start(@NonNull SpannableStringBuilder text, final @NonNull Object mark) {
            final int len = text.length();
            text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
        }

        private static <T> void end(@NonNull SpannableStringBuilder text, final @NonNull Class<T> kind, final @NonNull Object representation) {
            final Object lastRepresentation = getLast(text, kind);

            final int len = text.length();
            final int where = text.getSpanStart(lastRepresentation);

            text.removeSpan(lastRepresentation);

            if (where != len) {
                text.setSpan(representation, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
