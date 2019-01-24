package com.addteq.service.excellentable.exc_io.json;

import com.google.gson.JsonObject;

public abstract class JsonParser{
	
	abstract Object parse(JsonObject json);
	

}