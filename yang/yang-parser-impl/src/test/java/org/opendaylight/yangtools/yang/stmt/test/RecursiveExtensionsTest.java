/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class RecursiveExtensionsTest {

    @Test
    public void test() throws IOException, URISyntaxException, SourceException, ReactorException {
        try {
            SchemaContext schema = StmtTestUtils.parseYangSources("/bugs/recursive-extensions");
            assertNotNull(schema);
        } catch (Exception e) {
            StmtTestUtils.log(e, "    ");
            fail("Fail test if an exception has been thrown.");
        }
    }
}
