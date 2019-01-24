package com.addteq.service.excellentable.exc_io.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayParser<T> {

	@SuppressWarnings("unchecked")
	public List<T> parse(JsonArray json, JsonParser parser){

			List<T> res = new ArrayList<T>();
			
			 if(json == null)
	             return res;
			 
			for (int i = 0; i < json.size(); i++) {
			
				JsonObject obj = null;
				if(!json.get(i).isJsonNull())	{
					obj = json.get(i).getAsJsonObject();
				}

				res.add( (T) parser.parse(obj));
			}
			return res;
		}
	
}
