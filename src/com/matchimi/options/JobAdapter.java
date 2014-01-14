package com.matchimi.options;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import static com.matchimi.CommonUtilities.TAG;

import com.matchimi.R;

public class JobAdapter extends BaseAdapter {
	private Context context;

	private List<String> listPrice = null;
	private List<String> listAddress = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listTimeLeft = null;
	private List<Integer> listProgressBar = null;
	private List<Boolean> listColorStatus = null;
	
	public JobAdapter(Context context) {
		this.context = context;
	}

	public void updateList(List<String> price, List<String> address,
			List<String> company, List<String> schedule, List<String> timeLeft,
			 List<Integer> progressBar, List<Boolean> colorStatus) {
		listPrice = price;
		listAddress = address;
		listCompany = company;
		listSchedule = schedule;
		listTimeLeft = timeLeft;
		listProgressBar = progressBar;
		listColorStatus = colorStatus;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (listPrice != null) {
			return listPrice.size();
		} else {
			return 0;
		}
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
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.job_item_list, parent, false);

			// cache view fields into the holder
			holder = new ViewHolder();

			// Set Text
			holder.textDate = (TextView) convertView.findViewById(R.id.textDate);
			holder.textPlace = (TextView) convertView.findViewById(R.id.textPlace);
			holder.textPrice = (TextView) convertView.findViewById(R.id.textPrice);
			holder.textTimeLeft = (TextView) convertView.findViewById(R.id.textTimeLeft);
			holder.jobProgressBar = (ProgressBar) convertView.findViewById(R.id.jobProgressPercentage);
			
			// Associate the holder with the view for latter lookup
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.textPrice.setText(Html.fromHtml(listPrice.get(position)));
		holder.textDate.setText(listSchedule.get(position));
		holder.textPlace.setText(listCompany.get(position) + "\n"
				+ listAddress.get(position));
		
		if (listTimeLeft != null && listTimeLeft.size() > position) {
			 holder.textTimeLeft.setText(listTimeLeft.get(position));
		}

		Drawable colorDrawable = convertView.getResources().getDrawable(R.drawable.job_percentage_red);
		if(listColorStatus.get(position) == true) {
			holder.textTimeLeft.setTextColor(convertView.getResources().getColor(R.color.progress_red));
		} else {
			holder.textTimeLeft.setTextColor(convertView.getResources().getColor(R.color.progress_green));			
			colorDrawable = convertView.getResources().getDrawable(R.drawable.job_percentage_green);			
		}
		
		Rect bounds = holder.jobProgressBar.getProgressDrawable().getBounds();
		holder.jobProgressBar.setProgressDrawable(colorDrawable);
		holder.jobProgressBar.getProgressDrawable().setBounds(bounds);
	
		holder.jobProgressBar.setProgress(listProgressBar.get(position));
	
		return convertView;
	}

	static class ViewHolder {
		TextView textPrice;
		TextView textDate;
		TextView textPlace;
		TextView textTimeLeft;
		ProgressBar jobProgressBar;
	}
}
