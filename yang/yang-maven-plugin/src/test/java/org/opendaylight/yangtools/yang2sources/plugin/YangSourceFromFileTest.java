/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;

public class YangSourceFromFileTest {

    @Test
    public void yangSourceFromFileTest() throws Exception {
        final File source = new File(getClass().getResource("/yang-source-from-file.yang").getFile());
        final YangSourceFromFile yangSource = new YangSourceFromFile(source);
        final InputStream openStream = yangSource.openStream();
        Assert.assertNotNull(openStream);
        Assert.assertEquals(541, yangSource.size());
    }

}
