package org.opendaylight.yangtools.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
