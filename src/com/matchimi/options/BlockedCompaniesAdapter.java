package com.matchimi.options;

import static com.matchimi.CommonUtilities.PARAM_BLOCKED_COMPANIES_ADDRESS;
import static com.matchimi.CommonUtilities.PARAM_BLOCKED_COMPANIES_EMAIL_1;
import static com.matchimi.CommonUtilities.PARAM_BLOCKED_COMPANIES_EMAIL_2;
import static com.matchimi.CommonUtilities.PARAM_BLOCKED_COMPANIES_GRADE_ID;
import static com.matchimi.CommonUtilities.PARAM_BLOCKED_COMPANIES_NAME;
import static com.matchimi.CommonUtilities.PARAM_BLOCKED_COMPANIES_POSTAL_CODE;
import static com.matchimi.CommonUtilities.TAG;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.matchimi.R;

public class BlockedCompaniesAdapter extends BaseAdapter {
	private Context context;
	protected List blockedcompaniesList;

	public BlockedCompaniesAdapter(Context context, List blockedList) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.blockedcompaniesList = blockedList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return blockedcompaniesList.size();
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
			v = li.inflate(R.layout.blocked_companies_list, parent, false);

			// cache view fields into the holder
			holder = new ViewHolder();

			Map<String, Object> companyData = (Map<String, Object>) blockedcompaniesList
					.get(position);
			Log.d(TAG, companyData.get("company_name").toString());

			// Set Company Name
			holder.name = (TextView) v
					.findViewById(R.id.blockedcompanies_company_name);
			holder.name.setText(companyData.get(PARAM_BLOCKED_COMPANIES_NAME)
					.toString());

			// Set Address
			holder.address = (TextView) v
					.findViewById(R.id.blockedcompanies_address);
			holder.address.setText(companyData.get(
					PARAM_BLOCKED_COMPANIES_ADDRESS).toString());

			// Set Information
			holder.information = (TextView) v
					.findViewById(R.id.blockedcompanies_information);
			String information = "";

			String postalCode = companyData.get(
					PARAM_BLOCKED_COMPANIES_POSTAL_CODE).toString();

			if (postalCode != "") {
				information += "Postal Code: " + postalCode + "\n";
			}

			if (companyData.get(PARAM_BLOCKED_COMPANIES_EMAIL_1) != null) {
				String email_1 = companyData.get(
						PARAM_BLOCKED_COMPANIES_EMAIL_1).toString();
				if (email_1 != "" && email_1 != null) {
					information += "Email: " + email_1 + "\n";
				}
			}

			if (companyData.get(PARAM_BLOCKED_COMPANIES_EMAIL_2) != null) {
				String email_2 = companyData.get(
						PARAM_BLOCKED_COMPANIES_EMAIL_2).toString();
				if (email_2 != "" && email_2 != null) {
					information += "Email: " + email_2 + "\n";
				}
			}

			holder.information.setText(information);

			holder.rating = (RatingBar) v
					.findViewById(R.id.blockedcompanies_ratingBar);
			holder.rating.setRating(Float.parseFloat(companyData.get(
					PARAM_BLOCKED_COMPANIES_GRADE_ID).toString()));

			// Set Text
			// Associate the holder with the view for latter lookup
			v.setTag(holder);

		} else {
			holder = (ViewHolder) v.getTag();
		}

		return v;
	}

	static class ViewHolder {
		TextView name;
		TextView address;
		TextView information;
		RatingBar rating;
		int position;

	}
}
