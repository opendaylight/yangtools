/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.spi.SimpleSchemaContext;

class SimpleSchemaContextTest {
    @Test
    void testGetModulesOrdering() {
        final var foo0 = mockModule("foo", null);
        final var foo1 = mockModule("foo", "2018-01-01");
        final var foo2 = mockModule("foo", "2018-01-16");

        final var expected = List.of(foo2, foo1, foo0);
        assertGetModules(expected, foo0, foo1, foo2);
        assertGetModules(expected, foo0, foo2, foo1);
        assertGetModules(expected, foo1, foo0, foo2);
        assertGetModules(expected, foo1, foo2, foo0);
        assertGetModules(expected, foo2, foo0, foo1);
        assertGetModules(expected, foo2, foo1, foo0);

        assertFindModules(expected, "foo", foo0, foo1, foo2);
        assertFindModules(expected, "foo", foo0, foo2, foo1);
        assertFindModules(expected, "foo", foo1, foo0, foo2);
        assertFindModules(expected, "foo", foo1, foo2, foo0);
        assertFindModules(expected, "foo", foo2, foo0, foo1);
        assertFindModules(expected, "foo", foo2, foo1, foo0);

        final XMLNamespace uri = XMLNamespace.of("foo");
        assertFindModules(expected, uri, foo0, foo1, foo2);
        assertFindModules(expected, uri, foo0, foo2, foo1);
        assertFindModules(expected, uri, foo1, foo0, foo2);
        assertFindModules(expected, uri, foo1, foo2, foo0);
        assertFindModules(expected, uri, foo2, foo0, foo1);
        assertFindModules(expected, uri, foo2, foo1, foo0);
    }

    private static void assertGetModules(final List<Module> expected, final Module... modules) {
        final var actual = SimpleSchemaContext.forModules(List.of(modules)).getModules();
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private static void assertFindModules(final List<Module> expected, final String name, final Module... modules) {
        final var actual = SimpleSchemaContext.forModules(List.of(modules)).findModules(name);
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private static void assertFindModules(final List<Module> expected, final XMLNamespace uri,
            final Module... modules) {
        final var actual = SimpleSchemaContext.forModules(List.of(modules)).findModules(uri);
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private static Module mockModule(final String name, final String revision) {
        final var mod = QNameModule.ofRevision(name, revision);
        final var ret = mock(Module.class);
        doReturn(name).when(ret).getName();
        doReturn(mod.namespace()).when(ret).getNamespace();
        doReturn(mod.findRevision()).when(ret).getRevision();
        doReturn(mod).when(ret).getQNameModule();
        doReturn(mod.toString()).when(ret).toString();
        doReturn(Set.of()).when(ret).getImports();
        doReturn(Set.of()).when(ret).getSubmodules();
        doReturn(List.of()).when(ret).getUnknownSchemaNodes();
        return ret;
    }
}
