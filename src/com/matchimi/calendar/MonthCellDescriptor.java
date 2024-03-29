// Copyright 2012 Square, Inc.

package com.matchimi.calendar;

import java.util.Date;

/** Describes the state of a particular date cell in a {@link MonthView}. */
public class MonthCellDescriptor {
	public enum RangeState {
		NONE, FIRST, MIDDLE, LAST
	}

	private final Date date;
	private final int value;
	private final boolean isCurrentMonth;
	private boolean isSelected;
	private boolean isOccupied;
	private final boolean isToday;
	private final boolean isSelectable;
	private RangeState rangeState;

	public MonthCellDescriptor(Date date, boolean currentMonth, boolean selectable,
			boolean selected, boolean occupied, boolean today, int value, RangeState rangeState) {
		this.date = date;
		isCurrentMonth = currentMonth;
		isSelectable = selectable;
		isSelected = selected;
		isToday = today;
		isOccupied = occupied;
		this.value = value;
		this.rangeState = rangeState;
	}

	public Date getDate() {
		return date;
	}

	public boolean isCurrentMonth() {
		return isCurrentMonth;
	}

	public boolean isSelectable() {
		return isSelectable;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	public void setOccupied(boolean occupied) {
		isOccupied = occupied;
	}
	
	public boolean isToday() {
		return isToday;
	}

	public RangeState getRangeState() {
		return rangeState;
	}

	public void setRangeState(RangeState rangeState) {
		this.rangeState = rangeState;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "MonthCellDescriptor{" + "date=" + date + ", value=" + value
				+ ", isCurrentMonth=" + isCurrentMonth + ", isSelected="
				+ isSelected + ", isOccupied=" + isOccupied + ", isToday=" + isToday + ", isSelectable="
				+ isSelectable + ", rangeState=" + rangeState + '}';
	}
}
