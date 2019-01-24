package com.addteq.confluence.plugin.excellentable.importfile.impl;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by yagnesh.bhat on 10/2/18.
 */
public class FileExtensionTest {

    @Test
    public void shouldReturnExtensionCorrectly() {
        assertEquals("Extension should be xlsx","xlsx", FilenameUtils.getExtension("test.xlsx"));
        assertEquals("Extension should still be xlsx","xlsx", FilenameUtils.getExtension("test.html.xlsx"));
        assertEquals("Extension should be html", "html", FilenameUtils.getExtension("test.xlsx.html"));
        assertEquals("Blank Extension","",FilenameUtils.getExtension("test"));
    }
}