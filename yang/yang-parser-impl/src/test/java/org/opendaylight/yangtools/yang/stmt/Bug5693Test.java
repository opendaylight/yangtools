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

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.xml.sax.SAXException;

public class Bug5693Test {

    private Module foo;

    /**
     * Use input stream to load Yin module.
     */
    @Before
    public void initTest() throws ReactorException, SAXException, IOException {
        foo = TestUtils.loadYinModule(YinTextSchemaSource.forResource(getClass(), "/bugs/bug5693/foo.yin"));
    }

    /**
     * Test presence of testing feature (parsed in the last phase), if it is present then parsing was successful.
     * Meaning that stream was not closed after the first parsing phase.
     */
    @Test
    public void bug5693Test() {
        assertNotNull(foo.getFeatures());
        assertEquals("Module should has exactly one feature", 1, foo.getFeatures().size());
        assertEquals("Present feature should has expected local name", "test-input-stream-not-closed",
                foo.getFeatures().iterator().next().getQName().getLocalName());
    }
}
