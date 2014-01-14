package com.matchimi.schedule;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.matchimi.R;

public class RatingListAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final String[] description;

	public RatingListAdapter(Context context, String[] description) {
		super(context, R.layout.rating_dialog_list, description);
		this.context = context;
		this.description = description;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View rowView = inflater.inflate(R.layout.rating_dialog_list, null, true);
        
		TextView tv1 = (TextView) rowView.findViewById(R.id.rateTextNumber);
		TextView tv2 = (TextView) rowView.findViewById(R.id.rateTextDescription);
		
		tv1.setText(String.valueOf(4-position));
		tv2.setText(Html.fromHtml(description[position]));
		
		return rowView;
	}
}
