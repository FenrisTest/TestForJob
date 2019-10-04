package com.petersburg_studio.testforjob;

import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.HashMap;

class PaginationController {

    private final TextView textView;

    private int pageIndex;
    private int pageSymbol;
    private String text;
    private HashMap<Integer, Boundary> boundaries;
    private int lastPageIndex;
    private int pageSize = 500;
    private int length;

    PaginationController(@NonNull TextView textView) {
        this.textView = textView;
        boundaries = new HashMap<>();
        lastPageIndex = - 1;
    }

    void onTextLoaded(@NonNull String text, int page, @NonNull final OnInitializedListener listener) {
        pageIndex = page - 1;
        pageSymbol = pageIndex * 500;
        this.text = text;

        if (textView.getLayout() == null) {
            textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = textView.getViewTreeObserver();
                    obs.removeOnGlobalLayoutListener(this);
                    setTextWithCaching(pageIndex, 0);
                    listener.onInitialized();
                }
            });
        } else {
            setTextWithCaching(pageIndex, 0);
            listener.onInitialized();
        }
    }

    private void selectPage(int pageIndex) {
        String displayedText;
        if (boundaries.containsKey(pageIndex)) {
            Boundary boundary = boundaries.get(pageIndex);
            assert boundary != null;
            displayedText = text.substring(boundary.start, boundary.end);
            textView.setText(displayedText);
        } else if (boundaries.containsKey(pageIndex - 1)) {
            Boundary previous = boundaries.get(pageIndex - 1);
            assert previous != null;
            setTextWithCaching(pageIndex, previous.end);
        }
    }

    private void setTextWithCaching(int i, int pageStartSymbol) {
        String restText = text.substring(pageStartSymbol);
        textView.setText(restText);
        length = text.length();

        if (pageSymbol > 0) {
            pageStartSymbol = pageSymbol;
        }

        int start = pageStartSymbol;
        int end;
        if (start < length - pageSize) {
            end = start + pageSize;
        } else {
            end = length;
        }

        if (end == text.length()) {
            lastPageIndex = i;
        }

        String displayedText = text.substring(start, end);
        textView.setText(displayedText);
        boundaries.put(i, new Boundary(start, end));
    }

    void next() {
        throwIfNotInitialized();
        if (isNextEnabled()) {
            selectPage(++pageIndex);
            if (pageSymbol >= 0 && pageSymbol < length - pageSize) {
                pageSymbol += pageSize;
                setTextWithCaching(pageIndex, pageSymbol);
            }
        }
    }

    void previous() {
        throwIfNotInitialized();
        if (isPreviousEnabled()) {
            selectPage(--pageIndex);
            if (pageSymbol > 0) {
                pageSymbol -= pageSize;
                setTextWithCaching(pageIndex, pageSymbol);
            }
        }
    }

    private boolean isNextEnabled() {
        throwIfNotInitialized();
        return pageIndex < lastPageIndex || lastPageIndex < 0;
    }

    private boolean isPreviousEnabled() {
        throwIfNotInitialized();
        return pageIndex > 0;
    }

    private void throwIfNotInitialized() {
        if (text == null) {
            throw new IllegalStateException("Call onTextLoaded(String) first");
        }
    }

    private class Boundary {
        final int start;
        final int end;

        private Boundary(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public interface OnInitializedListener {
        void onInitialized();
    }
}
