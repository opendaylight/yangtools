/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.export.YinExportUtils;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug5531Test {
    @Test
    public void test() throws Exception {
        SchemaContext schema = YangParserTestUtils.parseYangResourceDirectory("/bugs/bug5531");

        assertNotNull(schema);
        assertNotNull(schema.getModules());
        assertEquals(1, schema.getModules().size());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        // write small module of size less than 8kB
       for (Module module : schema.getModules()) {
           YinExportUtils.writeModuleToOutputStream(schema, module, bufferedOutputStream);
       }

        String output = byteArrayOutputStream.toString();

        // if all changes were flushed then following conditions are satisfied
        assertNotEquals("Output should not be empty", 0, output.length());
        assertTrue("Output should contains start of the module", output.contains("<module"));
        assertTrue("Output should contains end of the module", output.contains("</module>"));
    }
}
