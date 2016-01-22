package com.github.crystal.util;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

public class JsonRawString {
	
	private final String value;
	
	public JsonRawString(String value){
		this.value = value;
	}

	@JsonValue
	@JsonRawValue
	public String value(){
		return value;
	}
	
	

}
