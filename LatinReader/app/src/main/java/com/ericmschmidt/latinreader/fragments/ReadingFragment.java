package com.ericmschmidt.latinreader.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ericmschmidt.latinreader.datamodel.Library;
import com.ericmschmidt.latinreader.datamodel.ReadingViewModel;
import com.ericmschmidt.latinreader.R;
import com.ericmschmidt.latinreader.datamodel.WorkInfo;

public class ReadingFragment extends Fragment {

    public static final String WORKTOGET = "workToGet";
    public static final String TRANSLATION_FLAG = "translation";
    public static final int HIT_AREA_RATIO = 4;

    private final int MENU_SWITCH_VIEW = 1;

    private String workToGetId;
    private boolean translationFlag;
    private OnReadingViewSwitch mListener;
    private ReadingViewModel viewModel;
    private int numLines;

    public ReadingFragment() {
        // Required empty public constructor
    }

    public static ReadingFragment newInstance(String workId, String isTranslation) {
        ReadingFragment fragment = new ReadingFragment();
        Bundle args = new Bundle();
        args.putString(WORKTOGET, workId);
        args.putString(TRANSLATION_FLAG, isTranslation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workToGetId = getArguments().getString(WORKTOGET);
            String isTranslation = getArguments().getString(TRANSLATION_FLAG);

            translationFlag = isTranslation != null && isTranslation.equals("true");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reading, container, false);
    }

    /**
     * Loads the fragment and the associated ReadingViewModel.
     * @param onSavedInstanceState Bundle
     */
    public void onActivityCreated(Bundle onSavedInstanceState) {

        super.onActivityCreated(onSavedInstanceState);

        Library library = new Library();
        WorkInfo work = library.getWorkInfoByID(workToGetId);
        viewModel = new ReadingViewModel(work, translationFlag);

        TextView readingPane = (TextView)getActivity().findViewById(R.id.reading_surface);

        // Register the context menu
        registerForContextMenu(readingPane);

        // Set text size.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String textSize = sharedPreferences.getString(SettingsFragment.TEXT_SIZE, SettingsFragment.TEXT_SIZE_DEFAULT);
        readingPane.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(textSize));

        // After parsing the XML, the app presents poetry lines one at a time.
        // The user can override the number of lines to show per page.
        // This setting doesn't matter for prose, since one "line" equals one paragraph.
        if (work.getWorkType() == WorkInfo.WorkType.poem) {
            String linesPerPage = sharedPreferences.getString(SettingsFragment.POEM_LINES, SettingsFragment.POEM_LINES_DEFAULT);
            numLines = Integer.parseInt(linesPerPage);
        } else {
            numLines = 1;
        }

        updateReadingSurface(0);

        /*
            Set touch responses:
            - touch the left side of the reading area, go back a page.
            - touch the right side of the reading area, go forward a page.
            - touch the middle of the reading area, either scroll or bring up context menu.
        */
        readingPane.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int viewWidth = v.getWidth();
                    float eventX = event.getX();
                    int hitArea = viewWidth / HIT_AREA_RATIO;

                    // the user has touched an edge; change the page.
                    if ((eventX < hitArea) ||
                            (eventX > viewWidth - hitArea)) {

                        int pageDelta = (eventX > viewWidth/2) ?
                                1 : -1;

                        updateReadingSurface(pageDelta);
                    } else { // The user touched the middle of the screen.
                        getActivity().openContextMenu(v);
                    }

                    return true;
                }
                return true;
            }
        });
    }

    // Change the text on the page by advancing the reading position.
    private void updateReadingSurface(int pageDelta) {

        TextView readingPane = (TextView)getActivity().findViewById(R.id.reading_surface);
        TextView readingInfo = (TextView)getActivity().findViewById(R.id.reading_info);

        viewModel.goToPage(pageDelta);

        readingPane.setText(viewModel.getCurrentPage(numLines));
        readingInfo.setText(viewModel.getReadingInfo());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReadingViewSwitch) {
            mListener = (OnReadingViewSwitch) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        int menuLabel = translationFlag ?
                R.string.context_menu_source :
                R.string.context_menu_translation;

        menu.add(0, 1, 0,menuLabel);
    }

    // Switch views, translation to/from source
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case MENU_SWITCH_VIEW:
                mListener.onReadingViewSwitch(workToGetId, Boolean.toString(!translationFlag));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public interface OnReadingViewSwitch {
        void onReadingViewSwitch(String workId, String isTranslation);
    }
}