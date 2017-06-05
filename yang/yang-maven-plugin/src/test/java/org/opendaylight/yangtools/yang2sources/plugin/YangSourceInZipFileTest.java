/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YangSourceInZipFileTest {

    @Test
    public void yangSourceInZipFileTest() throws Exception {
        final ZipFile file = Mockito.mock(ZipFile.class);
        final ZipEntry entry = Mockito.mock(ZipEntry.class);
        Mockito.when(entry.getSize()).thenReturn(519L);
        final InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.when(file.getInputStream(entry)).thenReturn(inputStream);
        final YangSourceInZipFile yangSource = new YangSourceInZipFile(file, entry);
        Assert.assertEquals(519, yangSource.size());
        Assert.assertNotNull(yangSource.openStream());
    }

}
