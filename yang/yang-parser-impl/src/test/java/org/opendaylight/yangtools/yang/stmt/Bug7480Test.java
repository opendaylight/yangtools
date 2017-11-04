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
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class Bug7480Test {
    @Test
    public void libSourcesTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug7480/files", "/bugs/bug7480/lib");
        assertNotNull(context);

        final Set<Module> modules = context.getModules();
        assertEquals(8, modules.size());

        assertNotNull(context.findModule(new URI("foo-imp"), Revision.of("2017-01-23")));
        assertEquals(1, context.findModules(new URI("foo-imp-2")).size());
        assertEquals(1, context.findModules(new URI("foo-imp-imp")).size());
        assertEquals(1, context.findModules(new URI("bar")).size());
        assertEquals(1, context.findModules(new URI("baz")).size());
        assertTrue(context.findModule(new URI("baz-imp"), Revision.of("2002-01-01")).isPresent());
        final Set<Module> foo = context.findModules(new URI("foo"));
        assertEquals(1, foo.size());
        final Set<Module> subFoos = foo.iterator().next().getSubmodules();
        assertEquals(1, subFoos.size());

        final Module parentMod = context.findModule(new URI("parent-mod-ns"), Revision.of("2017-09-07")).get();
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

    @Test
    public void testHandlingOfMainSourceConflictingWithLibSource() throws Exception {
        // parent module as main source and as lib source at the same time
        // parser should remove it from the required lib sources and thus avoid module namespace collision
        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSource(StmtTestUtils.sourceForResource(
                "/bugs/bug7480/main-source-lib-source-conflict-test/parent-module.yang"));
        reactor.addLibSources(StmtTestUtils.sourceForResource(
                "/bugs/bug7480/main-source-lib-source-conflict-test/child-module.yang"),
                StmtTestUtils.sourceForResource(
                        "/bugs/bug7480/main-source-lib-source-conflict-test/parent-module.yang"));
        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);
    }

    @Test
    public void testHandlingOfMainSourceConflictingWithLibSource2() throws Exception {
        // submodule as main source and as lib source at the same time
        // parser should remove it from the required lib sources and thus avoid submodule name collision
        final CrossSourceStatementReactor.BuildAction reactor = YangParserFactoryImpl.defaultParser();
        reactor.addSource(StmtTestUtils.sourceForResource(
                "/bugs/bug7480/main-source-lib-source-conflict-test/child-module.yang"));
        reactor.addLibSources(StmtTestUtils.sourceForResource(
                "/bugs/bug7480/main-source-lib-source-conflict-test/parent-module.yang"),
                StmtTestUtils.sourceForResource(
                        "/bugs/bug7480/main-source-lib-source-conflict-test/child-module.yang"));
        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);
    }
}
