package com.matchimi.options;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.matchimi.R;

public class FeedbackAdapter extends BaseAdapter {
	private Context context;
	protected ArrayList<String> feedbackList;

	public FeedbackAdapter(Context context, ArrayList<String> list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.feedbackList = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return feedbackList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder; // to reference the child views for later actions
		View v = convertView;

		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(context);
			v = li.inflate(R.layout.feedback_list, parent, false);

			// cache view fields into the holder
			holder = new ViewHolder();

			// Set Text
			holder.place = (TextView) v.findViewById(R.id.feedback_place);
			holder.place.setText(feedbackList.get(position));

			// Set Text
			holder.comment = (TextView) v.findViewById(R.id.feedback_comment);
			holder.comment.setText(feedbackList.get(position));

			// Associate the holder with the view for latter lookup
			v.setTag(holder);

		} else {
			holder = (ViewHolder) v.getTag();
		}

		return v;
	}

	static class ViewHolder {
		TextView place;
		TextView comment;
		RatingBar rating;
		int position;

	}
}
