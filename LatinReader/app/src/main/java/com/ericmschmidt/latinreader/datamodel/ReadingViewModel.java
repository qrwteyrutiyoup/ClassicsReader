package com.ericmschmidt.latinreader.datamodel;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.ericmschmidt.latinreader.MyApplication;

import java.util.Locale;

/**
 * A ViewModel that maps reading behaviors to a view.
 */
public class ReadingViewModel {

    private String DEFAULT_READING_POSITION="0,0";

    private WorkInfo _currentWorkInfo;
    private Work _currentWork;
    private Book _currentBook;
    private int _currentLineIndex;
    private int _currentBookIndex;
    private String _currentPage;
    private boolean _isTranslation;
    private String _author;
    private String _title;

    /**
     * Creates an instance of the ReadingViewModel class with a work open.
     * @param work the work to open.
     * @param isTranslation determines whether to return the translation of this work.
     */
    public ReadingViewModel(WorkInfo work, boolean isTranslation) {
        this._isTranslation = isTranslation;
        this._currentWorkInfo = work;

        if (!loadLastReadingPosition()) { // This work hasn't been read yet.
            this._currentLineIndex = 0;
            this._currentBookIndex = 0;
        }

        if (this._isTranslation) {
            this._currentWork = new Work(work.getEnglishLocation());
            this._author = work.getEnglishAuthor();
            this._title = work.getEnglishTitle();
        } else {
            this._currentWork = new Work(work.getLocation());
            this._author = work.getAuthor();
            this._title = work.getTitle();
        }

        this._currentBook = this._currentWork.getBook(this._currentBookIndex);
        updatePage();
    }

    /**
     * Gets the text for the reader's current position in the work.
     * @return String the text to read.
     */
    public String getCurrentPage(int lengthOfPassage) {
        return this._currentPage;
    }

    /**
     * Gets the index of the current book being read from the work.
     * @return int
     */
    public int getCurrentBookIndex() {
        return this._currentBookIndex;
    }

    /**\
     * Gets the index of the current line being read from the book.
     * @return int
     */
    public int getCurrentLineIndex() {
        return this._currentLineIndex;
    }

    /**
     * Scans the position in the book forwards or backwards.
     *
     * If the value goes beyond the end of the current book, it goes to the next book.
     * If the value goes beyond the beginning of the current book, it goes to the previous book.
     * If the value goes beyond the end of the work, it goes to the end of the work and stays there.
     * If the value goes beyond the beginning of the work, it goes to the first page.
     * @param numberOfPages the number of pages to update the reading position by
     */
    public void goToPage(int numberOfPages) {

        int tempLineIndex = this._currentLineIndex + numberOfPages;
        int totalLinesInBook = this._currentBook.getLineCount();

        if (tempLineIndex >= totalLinesInBook) { // Going beyond this book.
            advanceBook(tempLineIndex - totalLinesInBook);

        } else if (tempLineIndex < 0) { // Going to the previous book.
            decreaseBook(tempLineIndex);

        } else { // Going to another position in this book.
            this._currentLineIndex = tempLineIndex;
        }

        updatePage();
    }

    /**
     * Gets a formatted string that specifies the current work and reader's position.
     * @return String
     */
    public String getReadingInfo() {

         return String.format(Locale.US, "%s, %s %d.%d",
                 this._author,
                 this._title,
                 this._currentBookIndex + 1,
                 this._currentLineIndex + 1);
    }

    // Gets the specified work from the Library.

    // Updates the current reading page.
    private void updatePage() {
        this._currentPage = this._currentBook.getLine(this._currentLineIndex);

        // Store the current reading position in SharedPreferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String currentPosition = String.format("%d,%d", this._currentBookIndex, this._currentLineIndex);

        editor.putString(_currentWorkInfo.getId(), currentPosition);
        editor.commit();
    }

    // Open up the next book.
    private void advanceBook(int deltaPages) {
        this._currentBookIndex++;

        // This feels like code smell ...
        if (this._currentBookIndex < this._currentWork.getBookCount()) {
            this._currentBook = this._currentWork.getBook(this._currentBookIndex);
            this._currentLineIndex = 0;

            goToPage(deltaPages);
        } else {
            this._currentBookIndex = this._currentWork.getBookCount() - 1;
            this._currentLineIndex = this._currentBook.getLineCount() - 1;
        }
    }

    // Go to the previous book.
    private void decreaseBook(int deltaPages) {
        this._currentBookIndex--;

        if (this._currentBookIndex >= 0) {
            this._currentBook = this._currentWork.getBook(this._currentBookIndex);
            this._currentLineIndex = this._currentBook.getLineCount();

            goToPage(deltaPages);
        } else {
            this._currentBookIndex = 0;
            this._currentLineIndex = 0;
        }
    }

    // Gets the user's last reading position from device storage or cloud storage.
    private boolean loadLastReadingPosition() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String prefs = sharedPreferences.getString(_currentWorkInfo.getId(), DEFAULT_READING_POSITION);

        if (!prefs.equals(DEFAULT_READING_POSITION)) {
            String[] readingPosition = prefs.split(",");

            // Need to store reading position as bookIndex,lineIndex
            this._currentBookIndex = Integer.parseInt(readingPosition[0]);
            this._currentLineIndex = Integer.parseInt(readingPosition[1]);

            return true;
        }

        return false;
    }
}
