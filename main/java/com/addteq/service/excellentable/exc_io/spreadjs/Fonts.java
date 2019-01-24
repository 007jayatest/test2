package com.addteq.service.excellentable.exc_io.spreadjs;

public enum Fonts {
        ALEGREYA("Alegreya"),
        ALEGREYA_BOLD("Alegreya Bold"),
        ALEGREYA_ITALIC("Alegreya Italic"),
        AMATIC_SC("Amatic SC"),
        AMATIC_SC_BOLD("Amatic SC Bold"),
        ARIAL_BLACK("Arial Black"),
        ARIAL_BLACK_ITALIC("Arial Black Italic"),
        ARIAL("Arial"),
        BREE_SERIF("Bree Serif"),
        CALIBRI("Calibri"),
        CALIBRI_BOLD("Calibri Bold"),
        CALIBRI_ITALIC("Calibri Italic"),
        CAMBRIA("Cambria"),
        CAMBRIA_BOLD("Cambria Bold"),
        CAMBRIA_ITALIC("Cambria Italic"),
        CENTURY("Century"),
        CENTURY_BOLD("Century Bold"),
        CENTURY_ITALIC("Century Italic"),
        COMIC_SANS_MS("Comic Sans MS"),
        COMIC_SANS_MS_BOLD("Comic Sans MS Bold"),
        COURIER_NEW("Courier New"),
        GARAMOND("Garamond"),
        GARAMOND_BOLD("Garamond Bold"),
        GARAMOND_ITALIC("Garamond Italic"),
        GEORGIA("Georgia"),
        GEORGIA_BOLD("Georgia Bold"),
        GEORGIA_ITALIC("Georgia Italic"),
        IMPACT("Impact"),
        MERRIWEATHER("Merriweather"),
        MERRIWEATHER_BOLD("Merriweather Bold"),
        MERRIWEATHER_ITALIC("Merriweather Italic"),
        PERMANENT_MARKER("Permanent Marker"),
        PINYON_SCRIPT("Pinyon Script"),
        PLAYFAIR_DISPLAY("Playfair Display"),
        PLAYFAIR_DISPLAY_BOLD("Playfair Display Bold"),
        PLAYFAIR_DISPLAY_ITALIC("Playfair Display Italic"),
        ROBOTO_MONO("Roboto Mono"),
        ROBOTO_MONO_BOLD("Roboto Mono Bold"),
        ROBOTO_MONO_ITALIC("Roboto Mono Italic"),
        ROBOTO("Roboto"),
        ROBOTO_BOLD("Roboto Bold"),
        ROBOTO_ITALIC("Roboto Italic"),
        TAHOMA("Tahoma"),
        TAHOMA_BOLD("Tahoma Bold"),
        TIMES_NEW_ROMAN("Times New Roman"),
        TREBUCHET_MS("Trebuchet MS"),
        TREBUCHET_MS_BOLD("Trebuchet MS Bold"),
        TREBUCHET_MS_ITALIC("Trebuchet MS Italic"),
        ULTRA("Ultra"),
        VARELA_ROUND("Varela Round"),
        VERDANA("Verdana"),
        VERDANA_BOLD("Verdana Bold"),
        VERDANA_ITALIC("Verdana Italic"),
        WINGDINGS("Wingdings");

		private String font;
	
        Fonts(String font) {
        	this.font = font;
        }
        
        public String getFontFamily(){
        	return this.font;
        }

        public static boolean hasFont(String fontName)  {
            for (Fonts name : Fonts.values()) {
                    if (fontName.contains(name.getFontFamily())) {
                        return true;
                    }
                }
            
            return false;
        }
        
        public static String getDefault(){
        	return Fonts.VERDANA.getFontFamily();
        }

        public static String getAvailableFamily(String fontName){

                for (Fonts name : Fonts.values()) {
                        if (fontName.contains(name.getFontFamily())) {
                                return name.getFontFamily();
                        }
                }

                return Fonts.VERDANA.getFontFamily();

        }

}