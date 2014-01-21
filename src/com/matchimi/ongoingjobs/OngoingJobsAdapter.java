package com.matchimi.ongoingjobs;

import java.util.List;

import com.matchimi.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OngoingJobsAdapter extends BaseAdapter{

	private Context context;
	private List<String> listPrice = null;
	private List<String> listTime = null;
	private List<String> listPosition = null;
	private List<String> listCompany = null;
	
	public OngoingJobsAdapter(Context context){
		this.context = context;
	}

	public void updateList(List<String> price, List<String> timeLeft,
			List<String> position, List<String> company) {
		listPrice = price;
		listTime = timeLeft;
		listPosition = position;
		listCompany = company;
		
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
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder holder;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				
		if (view == null) {
			view = inflater.inflate(R.layout.ongoing_job_item_listview, viewGroup, false);
			
			holder = new ViewHolder();
			
			holder.textPrice = (TextView) view.findViewById(R.id.ongoing_Price);
			holder.textTime = (TextView) view.findViewById(R.id.ongoing_TimeLeft);
			holder.textPosition = (TextView) view.findViewById(R.id.ongoing_Position);
			holder.textCompany = (TextView) view.findViewById(R.id.ongoing_Company);
			
			view.setTag(holder);
		}else{
			holder = (ViewHolder) view.getTag();
		}
		
		holder.textPrice.setText(Html.fromHtml(listPrice.get(i)));
		holder.textTime.setText(listTime.get(i));
		holder.textPosition.setText(listPosition.get(i));
		holder.textCompany.setText(listCompany.get(i));
				
		return view;
	}
	
	static class ViewHolder{
		TextView textPrice;
		TextView textTime;
		TextView textPosition;
		TextView textCompany;
	}
}
