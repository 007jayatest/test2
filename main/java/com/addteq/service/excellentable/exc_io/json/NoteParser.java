package com.addteq.service.excellentable.exc_io.json;

import com.addteq.service.excellentable.exc_io.spreadjs.Note;
import com.google.gson.JsonObject;

public class NoteParser {
	
	public static Note parse(JsonObject noteJson){
		
		Note note = (Note) CellStyleParser.parse(noteJson);

		int row = noteJson.get("rowIndex").getAsInt();
		int col = noteJson.get("colIndex").getAsInt();
		String text = noteJson.get("text").getAsString();
		
		note.setRowIndex(row);
		note.setColIndex(col);
		note.setText(text);
		
		return note;

	}

}
