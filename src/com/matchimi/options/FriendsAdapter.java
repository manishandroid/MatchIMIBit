package com.matchimi.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.matchimi.R;

public class FriendsAdapter extends BaseAdapter implements Filterable {
	private Context context;
	
	private ItemFilter mFilter = new ItemFilter();
	protected List<FriendsModel> friends;
	protected List<FriendsModel> friendsFilter;

	public FriendsAdapter(Context context, List<FriendsModel> friends) {
		this.context = context;
		this.friends = friends;
		this.friendsFilter = friends;
	}

	public void updateView(List<FriendsModel> friends) {
		this.friends = friends;
		this.friendsFilter = friends;
		this.notifyDataSetChanged();
	}

	public void setFriendStatus(int position, boolean friend) {
		FriendsModel m = this.friendsFilter.get(position);
		m.setFriend(friend);
		this.friendsFilter.set(position, m);
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (friendsFilter != null && friendsFilter.size() > 0) {
			return friendsFilter.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int arg0) {
		return friendsFilter.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder; // to reference the child views for later actions
		View v = convertView;

		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(context);
			v = li.inflate(R.layout.friend_item_list, parent, false);

			// cache view fields into the holder
			holder = new ViewHolder();

			// Set Text
			holder.name = (TextView) v.findViewById(R.id.textCenter);
			holder.button = (TextView) v.findViewById(R.id.textRight);
			holder.image = (ImageView) v.findViewById(R.id.imageView);
			
			// Associate the holder with the view for latter lookup
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		holder.name.setText(friendsFilter.get(position).getName());
		if (friendsFilter.get(position).isFriend()) {
			holder.button.setText(R.string.unfriend);
		} else {
			holder.button.setText(R.string.add_friends);
		}
		holder.image.setImageBitmap(friendsFilter.get(position).getImg());

		final int thisPosition = position;
		holder.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent iBroadcast = new Intent("friends.receiver");
				iBroadcast.putExtra("position", thisPosition);
				iBroadcast.putExtra("id", friendsFilter.get(thisPosition).getId());
				iBroadcast.putExtra("add", !friendsFilter.get(thisPosition).isFriend());
				context.sendBroadcast(iBroadcast);
			}
		});

		return v;
	}

	static class ViewHolder {
		// TextView branch_name;
		TextView name;
		TextView button;
		ImageView image;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
	
	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			
			String filterString = constraint.toString().toLowerCase(Locale.getDefault());
			
			FilterResults results = new FilterResults();
			
			final List<FriendsModel> list = friends;
 
			int count = list.size();
			final ArrayList<FriendsModel> nlist = new ArrayList<FriendsModel>(count);
 
			String filterableString ;
			
			for (int i = 0; i < count; i++) {
				filterableString = list.get(i).getName();
				if (filterableString.toLowerCase(Locale.getDefault()).contains(filterString)) {
					nlist.add(list.get(i));
				}
			}
			
			results.values = nlist;
			results.count = nlist.size();
 
			return results;
		}
 
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			friendsFilter = (ArrayList<FriendsModel>) results.values;
			notifyDataSetChanged();
		}
 
	}
}
