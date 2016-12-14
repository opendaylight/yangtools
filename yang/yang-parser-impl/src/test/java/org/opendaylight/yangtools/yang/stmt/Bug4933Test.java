/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug4933Test {

    @Test
    public void test() throws ReactorException, FileNotFoundException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug4933/correct");
        assertNotNull(context);

        Set<Deviation> deviations = context.getModules().iterator().next().getDeviations();
        assertEquals(4, deviations.size());
    }

    @Test
    public void incorrectKeywordTest() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug4933/incorrect");
            fail("ReactorException should be thrown.");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("String 'not_supported' is not valid deviate argument"));
        }
    }
}
