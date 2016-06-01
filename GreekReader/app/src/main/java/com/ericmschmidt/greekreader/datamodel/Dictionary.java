package com.ericmschmidt.greekreader.datamodel;

import com.ericmschmidt.greekreader.R;
import com.ericmschmidt.greekreader.utilities.DictionaryXMLHelper;
import com.ericmschmidt.greekreader.MyApplication;
import com.ericmschmidt.greekreader.utilities.ResourceHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Contains the data and methods for getting dictionary entries.
 */
public class Dictionary  {

    private ArrayList<String>  _entryHeaders;
    private WorkInfo dictionaryInfo;

    /**
     * Creates a new instance of the dictionary class.
     */
    public Dictionary() {
        this.dictionaryInfo = Manifest.getDictionaryInfo();
        initEntries();
    }

    /**
     * Gets the number of entries in this dictionary.
     * @return integer
     */
    public int getEntryCount() {
        return this._entryHeaders.size();
    }

    /**
     * Checks whether a specific string is an entry in the dictionary.
     * @param searchEntry the entry to check for.
     * @return boolean
     */
    public boolean isInDictionary(String searchEntry) {
        return this._entryHeaders.contains(searchEntry);
    }

    /**
     * Gets a definition from the dictionary.
     * @param searchEntry the entry to search for.
     * @return String
     */
    public String getEntry(String searchEntry) {
        String definition = null;
        try {
            if (isInDictionary(searchEntry)) {
                InputStream stream = ResourceHelper.getResourceStream(this.dictionaryInfo.getLocation());
                definition = DictionaryXMLHelper.getEntry(stream, searchEntry);
            }
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            MyApplication.logError(errorMessage);
        }
        return definition;
    }

    /**
     * Gets a randomly selected from the dictionary
     * @return String randomly selected dictionary entry.
     */
    public String getRandomEntry() {
        Random random = new Random();
        int numberOfEntries = this.getEntryCount();

        int randomNumber = random.nextInt(numberOfEntries + 1);
        String randomEntryKey = this._entryHeaders.get(randomNumber);
        return getEntry(randomEntryKey);
    }

    // Gets the number of alphabet chapters in dictionary.
    private void initEntries() {
        try {
            InputStream stream = ResourceHelper.getResourceStream(R.raw.dictionary_entries);
            this._entryHeaders = DictionaryXMLHelper.getEntryHeaders2(stream);

        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            MyApplication.logError(errorMessage);
        }
    }
}