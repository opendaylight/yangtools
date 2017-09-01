/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.export.YinExportUtils;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4504Test {
    @Test
    public void test() throws Exception {
        SchemaContext schema = YangParserTestUtils.parseYangResources(Bug4504Test.class, "/bugs/bug4504");
        assertNotNull(schema);
        final File outDir = new File("target/bug4504-export");
        outDir.mkdirs();
        for (final Module module : schema.getModules()) {
            exportModule(schema, module, outDir);
        }
    }

    private static File exportModule(final SchemaContext schemaContext, final Module module, final File outDir)
            throws Exception {
        final File outFile = new File(outDir, YinExportUtils.wellFormedYinName(module.getName(), module.getRevision()));
        try (OutputStream output = new FileOutputStream(outFile)) {
            YinExportUtils.writeModuleToOutputStream(schemaContext, module, output);
        }
        return outFile;
    }
}
