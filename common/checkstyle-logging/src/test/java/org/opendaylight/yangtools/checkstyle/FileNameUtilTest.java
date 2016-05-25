package org.opendaylight.yangtools.checkstyle;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class FileNameUtilTest {

    @Test
    public void testFileNameUtil() {
        File relativeFile = new File("src/main/java");
        assertFalse(relativeFile.isAbsolute());
        File absoluteFile = relativeFile.getAbsoluteFile();
        assertTrue(absoluteFile.isAbsolute());
        assertEquals("src/main/java", FileNameUtil.getOptionalPathRelativeToMavenProjectRoot(absoluteFile).get().getPath());
    }

}
