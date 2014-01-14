package com.matchimi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.matchimi.CommonUtilities;
import com.matchimi.R;
import static com.matchimi.CommonUtilities.*;

import android.content.Context;
import android.util.Log;

public class JSONParser {

	private InputStream is;
	private String httpResponse;

	public JSONParser() {
		is = null;
		httpResponse = "";
	}

	public String getHttpsResultUrlGet(Context context, String url) {
		Log.e("getHttpResultUrlGet", "Getting from " + url);
		
		try {
			// defaultHttpClient
			SecureHttpClient httpClient = new SecureHttpClient(context);
			httpClient.getParams().setParameter("http.socket.timeout", 20000);
			HttpGet httpGet = new HttpGet(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			Log.e("getHttpResultUrlGet", "UnsupportedEncodingException, err: "
					+ e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e("getHttpResultUrlGet",
					"ClientProtocolException, err: " + e.getMessage());
		} catch (IOException e) {
			Log.e("getHttpResultUrlGet", "IOException, err: " + e.getMessage());
			JSONObject json = new JSONObject(); 
			try {
				if(e.getMessage() == null) {
					json.put("status",  CommonUtilities.SERVER_PROBLEM);					
				} else {
					json.put("status", CommonUtilities.NOINTERNET);
				}
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			return json.toString();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			httpResponse = sb.toString();
		} catch (Exception e) {
			Log.e("getHttpResultUrlGet", "Exception, err: " + e.getMessage());
		}
		return httpResponse;
	}

	
	public String getHttpResultUrlGet(String url) {
		Log.e("getHttpResultUrlGet", "Getting from " + url);
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter("http.socket.timeout", 20000);
			HttpGet httpGet = new HttpGet(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			Log.e("getHttpResultUrlGet", "UnsupportedEncodingException, err: "
					+ e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e("getHttpResultUrlGet",
					"ClientProtocolException, err: " + e.getMessage());
		} catch (IOException e) {
			Log.e("getHttpResultUrlGet", "IOException, err: " + e.getMessage());
			JSONObject json = new JSONObject(); 
			
			try {
				if(e.getMessage() == null) {
					json.put("status",  CommonUtilities.SERVER_PROBLEM);					
				} else {
					json.put("status", CommonUtilities.NOINTERNET);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			return json.toString();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			httpResponse = sb.toString();
		} catch (Exception e) {
			Log.e("getHttpResultUrlGet", "Exception, err: " + e.getMessage());
		}
		return httpResponse;
	}

	public String getHttpResultUrlPut(String url, String[] params,
			String[] values) {
		Log.e("getHttpResultUrlPut", "Put to " + url + ": " + params[0] + "("
				+ values[0] + ")");
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPut httpPut = new HttpPut(url);

			if (params != null && params.length > 0) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				for (int i = 0; i < params.length; i++) {
					pairs.add(new BasicNameValuePair(params[i], values[i]));
				}
				httpPut.setEntity(new UrlEncodedFormEntity(pairs));
			}

			HttpResponse httpResponse = httpClient.execute(httpPut);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			Log.e("getHttpResultUrlPut", "UnsupportedEncodingException, err: "
					+ e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e("getHttpResultUrlPut",
					"ClientProtocolException, err: " + e.getMessage());
		} catch (IOException e) {
			JSONObject json = new JSONObject(); 
			try {
				if(e.getMessage() == null) {
					json.put("status",  CommonUtilities.SERVER_PROBLEM);					
				} else {
					json.put("status", CommonUtilities.NOINTERNET);
				}
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			Log.e("getHttpResultUrlPut", "IOException, err: " + e.getMessage());
			return json.toString();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			httpResponse = sb.toString();
		} catch (Exception e) {
			Log.e("getHttpResultUrlPut", "Exception, err: " + e.getMessage());
		}

		return httpResponse;
	}

	public String getHttpResultUrlPost(String url, String[] params,
			String[] values) {
		Log.e("getHttpResultUrlPost", "Post to " + url + ": " + params[0] + "("
				+ values[0] + ")");
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter("http.socket.timeout", 20000);
			HttpPost httpPost = new HttpPost(url);

			if (params != null && params.length > 0) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				for (int i = 0; i < params.length; i++) {
					pairs.add(new BasicNameValuePair(params[i], values[i]));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(pairs));
			}

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			Log.e("getHttpResultUrlPost", "UnsupportedEncodingException, err: "
					+ e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e("getHttpResultUrlPost",
					"ClientProtocolException, err: " + e.getMessage());
		} catch (IOException e) {
			JSONObject json = new JSONObject(); 
			try {
				if(e.getMessage() == null) {
					json.put("status",  CommonUtilities.SERVER_PROBLEM);					
				} else {
					json.put("status", CommonUtilities.NOINTERNET);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			Log.e("getHttpResultUrlPost", "IOException, err: " + e.getMessage());
			return json.toString();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			httpResponse = sb.toString();
			Log.e("getHttpResultUrlPost", "Result" + ": " + httpResponse);

		} catch (Exception e) {
			Log.e("getHttpResultUrlPut", "Exception, err: " + e.getMessage());
		}

		return httpResponse;
	}

	public String getHttpResultUrlDelete(String url, String[] params,
			String[] values) {
		Log.e("getHttpResultUrlDelete", "Delete to " + url + ": " + params[0]
				+ "(" + values[0] + ")");
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);

			if (params != null && params.length > 0) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				for (int i = 0; i < params.length; i++) {
					pairs.add(new BasicNameValuePair(params[i], values[i]));
				}
				httpDelete.setEntity(new UrlEncodedFormEntity(pairs));
			}

			HttpResponse httpResponse = httpClient.execute(httpDelete);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			Log.e("getHttpResultUrlDelete",
					"UnsupportedEncodingException, err: " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e("getHttpResultUrlDelete", "ClientProtocolException, err: "
					+ e.getMessage());
		} catch (IOException e) {
			JSONObject json = new JSONObject(); 
			try {
				if(e.getMessage() == null) {
					json.put("status",  CommonUtilities.SERVER_PROBLEM);					
				} else {
					json.put("status", CommonUtilities.NOINTERNET);
				}
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			Log.e("getHttpResultUrlDelete",
					"IOException, err: " + e.getMessage());
			return json.toString();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			httpResponse = sb.toString();
		} catch (Exception e) {
			Log.e("getHttpResultUrlDelete", "Exception, err: " + e.getMessage());
		}

		return httpResponse;
	}

	public JSONArray getJSONArray(JSONObject json, String tag) {
		try {
			return json.getJSONArray(tag);
		} catch (JSONException e) {
			Log.e("getJSONArray", "JSONException, err: " + e.getMessage());
		}
		return null;
	}

	public String getString(JSONObject json, String tag) {
		try {
			return json.getString(tag);
		} catch (JSONException e) {
			Log.e("getString", "JSONException, err: " + e.getMessage());
		}
		return null;
	}

	public double getDouble(JSONObject json, String tag) {
		try {
			return json.getDouble(tag);
		} catch (JSONException e) {
			Log.e("getInt", "JSONException, err: " + e.getMessage());
		}
		return -1;
	}

	public int getInt(JSONObject json, String tag) {
		try {
			return json.getInt(tag);
		} catch (JSONException e) {
			Log.e("getInt", "JSONException, err: " + e.getMessage());
		}
		return -1;
	}

	public boolean getBoolean(JSONObject json, String tag) {
		try {
			return json.getBoolean(tag);
		} catch (JSONException e) {
			Log.e("getBoolean", "JSONException, err: " + e.getMessage());
		}
		return false;
	}
}
