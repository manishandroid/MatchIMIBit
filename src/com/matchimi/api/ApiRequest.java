package com.matchimi.api;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.volley_examples.toolbox.GsonRequest;
import com.github.volley_examples.toolbox.MyClass;

public class ApiRequest {
	public void getRequest() {
		RequestQueue queue = MyVolley.getRequestQueue();
		GsonRequest<MyClass> myReq = new GsonRequest<MyClass>(Method.GET,
				"http://validate.jsontest.com/?json={'key':'value'}",
				MyClass.class, createMyReqSuccessListener(),
				createMyReqErrorListener());
		queue.add(myReq);
	}

	private Response.Listener<MyClass> createMyReqSuccessListener() {
		return new Response.Listener<MyClass>() {
			@Override
			public void onResponse(MyClass response) {
				// mTvResult.setText(Long.toString(response.toString()));
			}
		};
	}

	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// mTvResult.setText(error.getMessage());
			}
		};
	}
}
