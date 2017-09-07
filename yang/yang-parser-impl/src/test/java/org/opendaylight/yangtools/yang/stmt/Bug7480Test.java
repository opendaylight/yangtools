/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
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

import java.net.URI;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class Bug7480Test {
    @Test
    public void libSourcesTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug7480/files", "/bugs/bug7480/lib");
        assertNotNull(context);

        final Set<Module> modules = context.getModules();
        assertEquals(8, modules.size());

        assertNotNull(context.findModuleByNamespaceAndRevision(new URI("foo-imp"), SimpleDateFormatUtil
                .getRevisionFormat().parse("2017-01-23")));
        assertEquals(1, context.findModuleByNamespace(new URI("foo-imp-2")).size());
        assertEquals(1, context.findModuleByNamespace(new URI("foo-imp-imp")).size());
        assertEquals(1, context.findModuleByNamespace(new URI("bar")).size());
        assertEquals(1, context.findModuleByNamespace(new URI("baz")).size());
        assertNotNull(context.findModuleByNamespaceAndRevision(new URI("baz-imp"), SimpleDateFormatUtil
                .getRevisionFormat().parse("2002-01-01")));
        final Set<Module> foo = context.findModuleByNamespace(new URI("foo"));
        assertEquals(1, foo.size());
        final Set<Module> subFoos = foo.iterator().next().getSubmodules();
        assertEquals(1, subFoos.size());

        final Module parentMod = context.findModuleByNamespaceAndRevision(new URI("parent-mod-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-09-07"));
        assertNotNull(parentMod);
        assertEquals(1, parentMod.getSubmodules().size());
    }

    @Test
    public void missingRelevantImportTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug7480/files-2", "/bugs/bug7480/lib-2");
            fail("Test should fail due to missing import of required yang source from library");
        } catch (final SomeModifiersUnresolvedException e) {
            final String message = e.getSuppressed().length > 0 ? e.getSuppressed()[0].getCause().getMessage() : e
                    .getCause().getCause().getMessage();
            assertTrue(message.startsWith("Imported module [missing-lib] was not found."));
        }
    }
}
