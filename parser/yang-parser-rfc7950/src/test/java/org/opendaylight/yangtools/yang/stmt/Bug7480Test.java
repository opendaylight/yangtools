/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

class Bug7480Test {
    @Test
    void libSourcesTest() throws Exception {
        final SchemaContext context = parseYangSources("/bugs/bug7480/files", "/bugs/bug7480/lib");
        assertNotNull(context);

        final Collection<? extends Module> modules = context.getModules();
        assertEquals(8, modules.size());

        assertNotNull(context.findModule(XMLNamespace.of("foo-imp"), Revision.of("2017-01-23")));
        assertEquals(1, context.findModules(XMLNamespace.of("foo-imp-2")).size());
        assertEquals(1, context.findModules(XMLNamespace.of("foo-imp-imp")).size());
        assertEquals(1, context.findModules(XMLNamespace.of("bar")).size());
        assertEquals(1, context.findModules(XMLNamespace.of("baz")).size());
        assertTrue(context.findModule(XMLNamespace.of("baz-imp"), Revision.of("2002-01-01")).isPresent());
        final Collection<? extends Module> foo = context.findModules(XMLNamespace.of("foo"));
        assertEquals(1, foo.size());
        final Collection<? extends Submodule> subFoos = foo.iterator().next().getSubmodules();
        assertEquals(1, subFoos.size());

        final Module parentMod = context.findModule(XMLNamespace.of("parent-mod-ns"), Revision.of("2017-09-07")).get();
        assertEquals(1, parentMod.getSubmodules().size());
    }

    @Test
    void missingRelevantImportTest() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> parseYangSources("/bugs/bug7480/files-2", "/bugs/bug7480/lib-2"));
        final String message = ex.getSuppressed().length > 0
            ? ex.getSuppressed()[0].getMessage() : ex.getCause().getMessage();
        assertThat(message, startsWith("Imported module [missing-lib] was not found."));
    }

    @Test
    void testHandlingOfMainSourceConflictingWithLibSource() throws Exception {
        // parent module as main source and as lib source at the same time
        // parser should remove it from the required lib sources and thus avoid module namespace collision
        final SchemaContext schemaContext =  RFC7950Reactors.defaultReactor().newBuild()
            .addSource(StmtTestUtils.sourceForResource(
                "/bugs/bug7480/main-source-lib-source-conflict-test/parent-module.yang"))
            .addLibSources(
                StmtTestUtils.sourceForResource(
                    "/bugs/bug7480/main-source-lib-source-conflict-test/child-module.yang"),
                StmtTestUtils.sourceForResource(
                    "/bugs/bug7480/main-source-lib-source-conflict-test/parent-module.yang"))
            .buildEffective();
        assertNotNull(schemaContext);
    }

    @Test
    void testHandlingOfMainSourceConflictingWithLibSource2() throws Exception {
        // submodule as main source and as lib source at the same time
        // parser should remove it from the required lib sources and thus avoid submodule name collision
        final SchemaContext schemaContext = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(StmtTestUtils.sourceForResource(
                "/bugs/bug7480/main-source-lib-source-conflict-test/child-module.yang"))
            .addLibSources(
                StmtTestUtils.sourceForResource(
                    "/bugs/bug7480/main-source-lib-source-conflict-test/parent-module.yang"),
                StmtTestUtils.sourceForResource(
                    "/bugs/bug7480/main-source-lib-source-conflict-test/child-module.yang"))
            .buildEffective();
        assertNotNull(schemaContext);
    }

    private static EffectiveModelContext parseYangSources(final String yangFilesDirectoryPath,
        final String yangLibsDirectoryPath) throws Exception {
        return RFC7950Reactors.defaultReactor().newBuild()
            .addSources(TestUtils.loadSources(yangFilesDirectoryPath))
            .addLibSources(TestUtils.loadSources(yangLibsDirectoryPath))
            .buildEffective();
    }
}
