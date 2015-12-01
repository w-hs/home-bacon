package de.whs.homebacon;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.whs.homebaconcore.Note;

/**
 * Created by Daniel on 01.12.2015.
 */
public class NoteFragment extends CardFragment {

    private View mRootView;

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.note_fragment_view, null);

        Bundle bundle =getArguments();
        Note note = (Note)bundle.getSerializable("note");

        TextView title = (TextView)mRootView.findViewById(R.id.textView);
        TextView value = (TextView)mRootView.findViewById(R.id.textView2);

        title.setText(note.getTitle());
        value.setText(note.getText());

        Button button = (Button)mRootView.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return mRootView;
    }


}
