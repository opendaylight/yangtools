/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;

public class YangSourceInZipFileTest {

    @Test
    public void yangSourceInZipFileTest() throws Exception {
        final File yang = new File(getClass().getResource("/test.zip").getFile());
        final ZipFile file = new ZipFile(yang);
        final ZipEntry entry = new ZipEntry(file.getEntry("test_zip.yang"));
        final YangSourceInZipFile yangSource = new YangSourceInZipFile(file, entry);
        Assert.assertEquals(519, yangSource.size());
        Assert.assertNotNull(yangSource.openStream());
    }

}
