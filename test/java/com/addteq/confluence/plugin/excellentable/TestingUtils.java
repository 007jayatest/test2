package com.addteq.confluence.plugin.excellentable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by rober on 8/13/2016.
 */
public class TestingUtils {
    public static File getResourceFile(Class classContext, String fileName) {
        ClassLoader classLoader = classContext.getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    public static byte[] getResourceFileAsByteArray(Class classContext, String fileName)
            throws FileNotFoundException, IOException {
        ClassLoader classLoader = classContext.getClassLoader();
        File resourceFile = getResourceFile(classContext, fileName);
        FileInputStream fileIn = new FileInputStream(resourceFile);
        return IOUtils.toByteArray(fileIn);
    }

    public static String readFileIntoString(File file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        return convertBufferedReaderToString(br);
    }

    public static JsonObject readFileIntoJson(File file) throws IOException {
        BufferedReader jsonReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        return (JsonObject) (new JsonParser()).parse(jsonReader);
    }

    public static String convertBufferedReaderToString(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
