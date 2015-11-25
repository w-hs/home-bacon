package de.whs.homebacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;

import java.util.List;

public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private List mRows;

    public SampleGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }
/*
    static final int[] BG_IMAGES = new int[] {
            R.drawable.debug_background_1,
    R.drawable.debug_background_5
};*/

    @Override
    public Fragment getFragment(int row, int col) {
        Page page = PAGES[row][col];
        /*String title =
                page.titleRes != 0 ? mContext.getString(page.titleRes) : null;
        String text =
                page.textRes != 0 ? mContext.getString(page.textRes) : null;*/
        CardFragment fragment = CardFragment.create(page.title, page.text);

        // Advanced settings (card gravity, card expansion/scrolling)
        fragment.setCardGravity(Gravity.CENTER);
        fragment.setExpansionEnabled(true);
      //  fragment.setExpansionDirection(Path.Direction.NONE);
       // fragment.setExpansionFactor(page.expansionFactor);
        return fragment;
    }

    // Obtain the background image for the row
    @Override
    public Drawable getBackgroundForRow(int row) {
        return mContext.getResources().getDrawable(R.drawable.card_background);
    }

    // Obtain the number of pages (vertical)
    @Override
    public int getRowCount() {
        return PAGES.length;
    }

    // Obtain the number of pages (horizontal)
    @Override
    public int getColumnCount(int rowNum) {
        return PAGES[rowNum].length;
    }

    // A simple container for static data in each page
private static class Page {
    // static resources
    String title;
        String text;

        public Page(String title, String text)
        {
            this.title = title;
            this.text = text;

        }


}

// Create a static set of pages in a 2D array
private final Page[][] PAGES = {
        {new Page("1","1"),new Page("2","1"),new Page("3","1")},
        {new Page("4","1"),new Page("5","1"),new Page("6","1")},
        {new Page("7","1"),new Page("8","1"),new Page("9","1")} };

}