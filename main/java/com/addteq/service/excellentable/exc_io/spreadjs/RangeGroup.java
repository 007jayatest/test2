package com.addteq.service.excellentable.exc_io.spreadjs;

import java.util.ArrayList;
import java.util.List;

public class RangeGroup {

	private List<GroupData> itemsData;
	
	public RangeGroup(List<GroupData> itemsData){
		this.itemsData = itemsData;
	}
	
	public RangeGroup(){
		this.itemsData = new ArrayList<GroupData>();
	}
	
	public void addGroupData(GroupData groupData){
		this.itemsData.add(groupData);
	}
	
}
