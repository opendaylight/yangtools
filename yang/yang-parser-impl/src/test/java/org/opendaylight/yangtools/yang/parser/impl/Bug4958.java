/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug4958 {
    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        URL resourceDir = Bug4958.class.getResource("/bugs/bug-4958");
        File testSourcesDir = new File(resourceDir.toURI());
        SchemaContext context = YangParserImpl.getInstance().parseFiles(Arrays.asList(testSourcesDir.listFiles()));
        assertNotNull(context);
    }
}
