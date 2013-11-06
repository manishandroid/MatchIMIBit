package com.matchimi.availability;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.matchimi.R;

import static com.matchimi.CommonUtilities.TAG;

public class LocationPreferenceRegionAdapter extends BaseAdapter {
	private Context context;

	protected List<LocationModel> locationModels;

	public LocationPreferenceRegionAdapter(Context context,
			List<LocationModel> locationModels) {
		this.context = context;
		this.locationModels = locationModels;
	}

	public void updateView(List<LocationModel> locationModels) {
		this.locationModels = locationModels;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (locationModels != null && locationModels.size() > 0) {
			return locationModels.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int arg0) {
		return locationModels.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (convertView == null) {
			final ViewHolder holder = new ViewHolder();
			LayoutInflater li = LayoutInflater.from(context);
			v = li.inflate(R.layout.availability_location_preference_region_list,
					parent, false);

			// Set Text
			holder.name = (TextView) v.findViewById(R.id.regionLabel);
			holder.checkbox = (CheckBox) v.findViewById(R.id.regionCheck);
			holder.layout = (RelativeLayout) v.findViewById(R.id.regionListWrapper);
			
			holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {
                	LocationModel element = (LocationModel) holder.checkbox
                            .getTag();
                    element.setSelected(buttonView.isChecked());
                }
            });
			
			v.setTag(holder);
			holder.checkbox.setTag(locationModels.get(position));

			holder.layout.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					holder.checkbox.toggle();
				}
			});

		} else {
			ViewHolder holder = (ViewHolder) v.getTag();
			((ViewHolder) v.getTag()).checkbox.setTag(locationModels.get(position));
		}

		ViewHolder holder = (ViewHolder) v.getTag();
		holder.name.setText(locationModels.get(position).getName());
		holder.checkbox.setChecked(locationModels.get(position).isSelected());

		if(locationModels.get(position).isSelected()) {
			holder.checkbox.setChecked(true);
		}
		

		return v;
	}

	static class ViewHolder {
		// TextView branch_name;
		TextView name;
		CheckBox checkbox;
		RelativeLayout layout;
	}
}