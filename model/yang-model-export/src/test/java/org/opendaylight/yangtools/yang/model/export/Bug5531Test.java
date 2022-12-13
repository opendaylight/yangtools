/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug5531Test {
    @Test
    public void test() throws Exception {
        final var schema = YangParserTestUtils.parseYangResourceDirectory("/bugs/bug5531");

        assertNotNull(schema);
        assertNotNull(schema.getModules());
        assertEquals(1, schema.getModules().size());

        var byteArrayOutputStream = new ByteArrayOutputStream();
        var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        // write small module of size less than 8kB
        for (var module : schema.getModuleStatements().values()) {
            YinExportUtils.writeModuleAsYinText(module, bufferedOutputStream);
        }

        String output = byteArrayOutputStream.toString();

        // if all changes were flushed then following conditions are satisfied
        assertNotEquals(0, output.length());
        assertThat(output, allOf(containsString("<module"), containsString("</module>")));
    }
}
