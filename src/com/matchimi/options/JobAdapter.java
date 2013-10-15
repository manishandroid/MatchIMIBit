package com.matchimi.options;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.matchimi.R;

public class JobAdapter extends BaseAdapter {
	private Context context;

	private List<String> listPrice = null;
	private List<String> listAddress = null;
	private List<String> listCompany = null;
	private List<String> listSchedule = null;
	private List<String> listTimeLeft = null;
	private List<String> listProgressBar = null;

	public JobAdapter(Context context) {
		this.context = context;
	}

	public void updateList(List<String> price, List<String> address,
			List<String> company, List<String> schedule, List<String> timeLeft,
			 List<String> progressBar) {
		listPrice = price;
		listAddress = address;
		listCompany = company;
		listSchedule = schedule;
		listTimeLeft = timeLeft;
		listProgressBar = progressBar;

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
		View v = convertView;

		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(context);
			v = li.inflate(R.layout.job_item_list, parent, false);

			// cache view fields into the holder
			holder = new ViewHolder();

			// Set Text
			holder.textDate = (TextView) v.findViewById(R.id.textDate);
			holder.textPlace = (TextView) v.findViewById(R.id.textPlace);
			holder.textPrice = (TextView) v.findViewById(R.id.textPrice);
			holder.textTimeLeft = (TextView) v.findViewById(R.id.textTimeLeft);
			holder.jobProgressBar = (ProgressBar) v.findViewById(R.id.jobProgressPercentage);
			
			// Associate the holder with the view for latter lookup
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		holder.textPrice.setText(Html.fromHtml(listPrice.get(position)));
		holder.textDate.setText(listSchedule.get(position));
		holder.textPlace.setText(listCompany.get(position) + "\n"
				+ listAddress.get(position));
		if (listTimeLeft != null && listTimeLeft.size() > position) {
			 holder.textTimeLeft.setText(listTimeLeft.get(position));
		}
		
		holder.jobProgressBar.setProgress(Integer.valueOf(listProgressBar.get(position)));

		return v;
	}

	static class ViewHolder {
		TextView textPrice;
		TextView textDate;
		TextView textPlace;
		TextView textTimeLeft;
		ProgressBar jobProgressBar;
	}
}
