package com.rohit.app.test2;

public class IdAndLabelEncapsulated {
	private Integer id;
	private String label;
	
	public IdAndLabelEncapsulated(){
		
	}
	
	public IdAndLabelEncapsulated(Integer _id, String _label){
		id = _id;
		label = _label;
	}
	
	public Integer getId(){
		return id;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setId(Integer _id){
		id = _id;
	}
	
	public void setLabel(String _label){
		label = _label;
	}
}
