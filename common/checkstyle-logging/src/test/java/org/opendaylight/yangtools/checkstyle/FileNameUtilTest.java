/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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
