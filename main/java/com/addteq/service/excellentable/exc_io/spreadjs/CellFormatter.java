package com.addteq.service.excellentable.exc_io.spreadjs;

public class CellFormatter {

	private String formatCached;
	private String customerCultureName;
	
	public CellFormatter(){
	}

	public String getFormatCached() {
		return formatCached;
	}

	public void setFormatCached(String formatCached) {
		this.formatCached = formatCached;
	}

	public String getCustomerCultureName() {
		return customerCultureName;
	}

	public void setCustomerCultureName(String customerCultureName) {
		this.customerCultureName = customerCultureName;
	}
	
	
}