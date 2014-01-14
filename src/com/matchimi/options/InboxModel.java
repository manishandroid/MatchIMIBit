package com.matchimi.options;

import android.util.Log;

import com.matchimi.CommonUtilities;

public class InboxModel {

	private String id;
	private String sub_slot;
	private String avail_id;
	private String body;
	private Boolean is_has_read;
	private Boolean is_from_part_timer;
	private String read_at;
	private String sent_at;
	private String created_at;
	
	public InboxModel() {
		id = null;
		sub_slot = null;
		avail_id = null;
		body = null;
		is_has_read = false;
		is_from_part_timer = false;
		read_at = null;
		sent_at = null;
		created_at = null;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAvailId() {
		return avail_id;
	}

	public void setAvailId(String id) {
		this.avail_id = avail_id;
	}

	public String getSubSlot() {
		return sub_slot;
	}

	public void setSubSlot(String sub_slot) {
		this.sub_slot = sub_slot;
	}

	public String getSentAt() {
		return sent_at;
	}

	public void setSentAt(String sent_at) {
		this.sent_at = sent_at;
	}
	
	public String getCreatedAt() {
		return sent_at;
	}

	public void setCreatedAt(String created_at) {
		this.created_at = created_at;
	}

	public String getReadAt() {
		return read_at;
	}

	public void setReadAt(String read_at) {
		this.read_at = read_at;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean getIsFromPartTimer() {
		return is_from_part_timer;
	}

	public void setIsFromPartTimer(boolean is_from_part_timer) {
		this.is_from_part_timer = is_from_part_timer;
	}
	
	public boolean getIsHasRead() {
		return is_has_read;
	}

	public void setIsHasRead(boolean is_has_read) {
		this.is_has_read = is_has_read;
	}
}
