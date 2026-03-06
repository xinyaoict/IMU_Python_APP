package com.llui.idrink.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.llui.idrink.Interfaces.OnHistoricalClickListener;
import com.llui.idrink.Models.MeasurementSession;
import com.llui.idrink.R;

import java.util.List;

/**
 * HistoricalAdapter is an Adapter for the historical view.
 * It holds the list of all measurement sessions completed and display the number of the session,
 * the speed and the walking aid selected as well as the measurements taken.
 * Clicking on an item launches the MeasurementSelectionActivity of the selected measurement session.
 */
public class HistoricalAdapter extends ArrayAdapter<MeasurementSession> {
    private final Context context;
    private final List<MeasurementSession> sessions;
    private final OnHistoricalClickListener itemClickListener;


    public HistoricalAdapter(Context context, List<MeasurementSession> sessions, OnHistoricalClickListener itemClickListener) {
        super(context, R.layout.item_list, sessions);
        this.context = context;
        this.sessions = sessions;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        }

        MeasurementSession session = sessions.get(position);

        TextView titleTxt = convertView.findViewById(R.id.titleTxt);
        TextView commentTxt = convertView.findViewById(R.id.commentTxt);

        // Build the title string with "Session nb" bold and the rest normal
        String title = "Session " + (position + 1) + " : " + session.getHistoricalTitle();

        // Create a SpannableString to apply bold style to "Session nb"
        SpannableString spannableString = new SpannableString(title);

        // Apply bold style to "Session nb"
        int sessionLength = ("Session " + (position + 1)).length(); // Calculate the length of "Session nb"
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, sessionLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleTxt.setText(spannableString);
        if (session.getMainComment().isEmpty()) {
            commentTxt.setVisibility(View.GONE);
        } else {
            commentTxt.setText(session.getMainComment());
        }

        // Set the click listener
        Animation scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(scaleUp); // Start the animation
                itemClickListener.onHistoricalClick(position);
            }
        });

        return convertView;
    }
}
