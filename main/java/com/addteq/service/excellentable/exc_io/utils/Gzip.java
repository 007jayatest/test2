/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.service.excellentable.exc_io.utils;

/**
 *
 * @author vikashkumar
 */
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class Gzip {

    private Gzip() {

    }

    public static String compressString(String srcTxt) {
        // If the string is already compressed then return as it is.
        if (StringUtils.isBlank(srcTxt) || isCompressed(srcTxt)) {
            return srcTxt;
        }
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos;
        try {
            zos = new GZIPOutputStream(rstBao);
            zos.write(srcTxt.getBytes());
            IOUtils.closeQuietly(zos);
        } catch (IOException ex) {
            Logger.getLogger(Gzip.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] bytes = rstBao.toByteArray();
        return Base64.encodeBase64String(bytes);
    }

    public static String uncompressString(String zippedBase64Str) {
        String result = null;
        // If the string is already uncompressed then do not uncompresss and return as it is.
        if (StringUtils.isBlank(zippedBase64Str) || !isCompressed(zippedBase64Str)) {
            return zippedBase64Str;
        }
        byte[] bytes = Base64.decodeBase64(zippedBase64Str);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            result = IOUtils.toString(zi);
        } catch (IOException ex) {
            Logger.getLogger(Gzip.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            IOUtils.closeQuietly(zi);
        }
        return result;
    }

    public static boolean isCompressed(final String compressedString) {
        if (StringUtils.isBlank(compressedString)) {
            return true;
        }
        byte[] compressed = Base64.decodeBase64(compressedString);
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    public static int getStringSizeInBytes(final String compressedString){
        final String uncompressedString = uncompressString(compressedString);
        if(compressedString == null){
            return 0;
        }
        return uncompressedString.length();
    }
}
