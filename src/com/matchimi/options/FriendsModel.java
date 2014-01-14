package com.matchimi.options;

import android.graphics.Bitmap;

public class FriendsModel {

	private String id;
	private String name;
	private Bitmap img;
	private boolean friend;
	
	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}

	public FriendsModel() {
		id = null;
		name = null;
		friend = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFriend() {
		return friend;
	}

	public void setFriend(boolean friend) {
		this.friend = friend;
	}

}
