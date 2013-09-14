package com.matchimi.options;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.matchimi.R;

public class AvailabilityAdapter extends BaseAdapter {
	private Context context;

	private List<String> listDate = null;
	private List<Integer> listRepeat = null;
	private List<Boolean> listFrozen = null;	
	private String[] repeatString = null;

	public AvailabilityAdapter(Context context) {
		this.context = context;
		repeatString = context.getResources().getStringArray(
				R.array.repeat_value);
	}

	public void updateList(List<String> date, List<Integer> repeat, List<Boolean> is_frozen) {
		listDate = date;
		listRepeat = repeat;
		listFrozen = is_frozen;
		
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (listDate != null) {
			return listDate.size();
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
			v = li.inflate(R.layout.availability_item_list, parent, false);

			// cache view fields into the holder
			holder = new ViewHolder();

			// Set Text
			holder.textDate = (TextView) v.findViewById(R.id.textDate);
			holder.textRepeat = (TextView) v.findViewById(R.id.textRepeat);
			holder.freezeStatus = (TextView) v.findViewById(R.id.detail_availability_freeze_status);

			// Associate the holder with the view for latter lookup
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		holder.textDate.setText(listDate.get(position));		
		if (listRepeat != null && position < listRepeat.size()
				&& listRepeat.get(position) != null) {
			holder.textRepeat.setText("Repeats "
					+ repeatString[listRepeat.get(position)]);
		} else {
			holder.textRepeat.setText("Repeats None");
		}
		
		if(listFrozen.get(position)) {
			holder.freezeStatus.setVisibility(View.VISIBLE);
		} else {
			holder.freezeStatus.setVisibility(View.INVISIBLE);			
		}

		return v;
	}

	static class ViewHolder {
		TextView textDate;
		TextView textRepeat;
		TextView freezeStatus;		
	}
}
