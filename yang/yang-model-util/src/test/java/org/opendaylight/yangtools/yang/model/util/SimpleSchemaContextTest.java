/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;

public class SimpleSchemaContextTest {
    @Test
    public void testGetModulesOrdering() {
        final Module foo0 = mockModule("foo", Optional.empty());
        final Module foo1 = mockModule("foo", Revision.ofNullable("2018-01-01"));
        final Module foo2 = mockModule("foo", Revision.ofNullable("2018-01-16"));

        final List<Module> expected = ImmutableList.of(foo2, foo1, foo0);
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

        final URI uri = URI.create("foo");
        assertFindModules(expected, uri, foo0, foo1, foo2);
        assertFindModules(expected, uri, foo0, foo2, foo1);
        assertFindModules(expected, uri, foo1, foo0, foo2);
        assertFindModules(expected, uri, foo1, foo2, foo0);
        assertFindModules(expected, uri, foo2, foo0, foo1);
        assertFindModules(expected, uri, foo2, foo1, foo0);
    }

    private static void assertGetModules(final List<Module> expected, final Module... modules) {
        final Set<Module> actual = SimpleSchemaContext.forModules(ImmutableSet.copyOf(modules)).getModules();
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private static void assertFindModules(final List<Module> expected, final String name, final Module... modules) {
        final Set<Module> actual = SimpleSchemaContext.forModules(ImmutableSet.copyOf(modules)).findModules(name);
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private static void assertFindModules(final List<Module> expected, final URI uri, final Module... modules) {
        final Set<Module> actual = SimpleSchemaContext.forModules(ImmutableSet.copyOf(modules)).findModules(uri);
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private static Module mockModule(final String name, final Optional<Revision> revision) {
        final QNameModule mod = QNameModule.create(URI.create(name), revision);
        final Module ret = mock(Module.class);
        doReturn(name).when(ret).getName();
        doReturn(mod.getNamespace()).when(ret).getNamespace();
        doReturn(mod.getRevision()).when(ret).getRevision();
        doReturn(mod).when(ret).getQNameModule();
        doReturn(mod.toString()).when(ret).toString();
        doReturn(ImmutableSet.of()).when(ret).getImports();
        doReturn(ImmutableSet.of()).when(ret).getSubmodules();
        doReturn(ImmutableList.of()).when(ret).getUnknownSchemaNodes();
        return ret;
    }
}
