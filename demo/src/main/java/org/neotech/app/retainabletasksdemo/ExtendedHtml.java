package org.neotech.app.retainabletasksdemo;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.internal.ParcelableSparseArray;
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

    public static final int FROM_HTML_MODE_LEGACY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_MODE_LEGACY:0;
    public static final int FROM_HTML_MODE_COMPACT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_MODE_COMPACT:0;
    public static final int FROM_HTML_OPTION_USE_CSS_COLORS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_OPTION_USE_CSS_COLORS:0;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE:0;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_DIV = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV:0;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_HEADING = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING:0;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_LIST = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST:0;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM:0;
    public static final int FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH:0;

    private static final ExtendedTagHandler TAG_HANDLER = new ExtendedTagHandler();

    public static Spanned fromHtml(String source, int flags){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return Html.fromHtml(source, flags, null, TAG_HANDLER);
        } else {
            return Html.fromHtml(source, null, TAG_HANDLER);
        }
    }

    private static class CodeSpan extends TypefaceSpan {

        private final int backgroundColor;

        public CodeSpan(int backgroundColor){
            super("monospace");
            this.backgroundColor = backgroundColor;
        }

        protected CodeSpan(Parcel in) {
            super(in);
            this.backgroundColor = in.readInt();
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

        public static final Creator<CodeSpan> CREATOR = new Creator<CodeSpan>() {

            @Override
            public CodeSpan createFromParcel(Parcel in) {
                return new CodeSpan(in);
            }

            @Override
            public CodeSpan[] newArray(int size) {
                return new CodeSpan[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(backgroundColor);
        }

        public int describeContents() {
            return 0;
        }
    }

    /**
     * Based on: http://stackoverflow.com/questions/4044509/android-how-to-use-the-html-taghandler
     */
    private static class ExtendedTagHandler implements Html.TagHandler {

        private static final int COLOR = 0xFFDDDDDD;

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
