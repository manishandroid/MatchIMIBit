package com.matchimi.options;

import static com.matchimi.CommonUtilities.PARAM_FEEDBACK_COMPANY_COMMENT;
import static com.matchimi.CommonUtilities.PARAM_FEEDBACK_COMPANY_GRADE;
import static com.matchimi.CommonUtilities.PARAM_FEEDBACK_COMPANY_NAME;

import java.util.List;
import java.util.Map;

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
	protected List feedbackList;

	public FeedbackAdapter(Context context, List feedbackData) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.feedbackList = feedbackData;
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
			Map<String, Object> companyData = (Map<String, Object>) feedbackList
					.get(position);
			Map<String, String> details = (Map<String, String>) companyData
					.get("availabilities");

			// cache view fields into the holder
			holder = new ViewHolder();

			// Set branch name
			// holder.branch_name = (TextView) v
			// .findViewById(R.id.feedback_branch_name);
			// holder.branch_name.setText(details.get(
			// PARAM_FEEDBACK_COMPANY_BRANCH).toString());

			// Set feedback from company
			holder.comment = (TextView) v.findViewById(R.id.feedback_comment);
			holder.comment.setText(details.get(PARAM_FEEDBACK_COMPANY_COMMENT)
					.toString());

			// Set company name
			holder.company_name = (TextView) v
					.findViewById(R.id.feedback_company_name);
			holder.company_name.setText(details
					.get(PARAM_FEEDBACK_COMPANY_NAME).toString());

			// Set rating grade
			holder.grade = (RatingBar) v.findViewById(R.id.feedback_grade);
			String ratingText = details.get(PARAM_FEEDBACK_COMPANY_GRADE)
					.toString();
			holder.grade.setRating((Float.parseFloat(ratingText)));

			// Associate the holder with the view for latter lookup
			v.setTag(holder);

		} else {
			holder = (ViewHolder) v.getTag();
		}

		return v;
	}

	static class ViewHolder {
		// TextView branch_name;
		TextView company_name;
		TextView comment;
		TextView start_date_time;
		RatingBar grade;
		int position;
	}
}
