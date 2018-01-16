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
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;

public class SimpleSchemaContextTest {
    @Test
    public void testModulesOrdering() {
        final Module foo0 = mockModule("foo", Optional.empty());
        final Module foo1 = mockModule("foo", Revision.ofNullable("2018-01-01"));
        final Module foo2 = mockModule("foo", Revision.ofNullable("2018-01-16"));

        final List<Module> expected = ImmutableList.of(foo2, foo1, foo0);
        testOrdering(expected, foo0, foo1, foo2);
        testOrdering(expected, foo0, foo2, foo1);
        testOrdering(expected, foo1, foo0, foo2);
        testOrdering(expected, foo1, foo2, foo0);
        testOrdering(expected, foo2, foo0, foo1);
        testOrdering(expected, foo2, foo1, foo0);
    }

    private static void testOrdering(final List<Module> expected, final Module...modules) {
        final SimpleSchemaContext context = SimpleSchemaContext.forModules(ImmutableSet.copyOf(modules));
        assertArrayEquals(expected.toArray(), context.getModules().toArray());
        assertArrayEquals(expected.toArray(), context.findModules(expected.get(0).getName()).toArray());
        assertArrayEquals(expected.toArray(), context.findModules(expected.get(0).getNamespace()).toArray());
    }

    private static Module mockModule(final String name, final Optional<Revision> revision) {
        final QNameModule mod = QNameModule.create(URI.create("name"), revision);
        final Module ret = mock(Module.class, mod.toString());
        doReturn(name).when(ret).getName();
        doReturn(mod.getNamespace()).when(ret).getNamespace();
        doReturn(mod.getRevision()).when(ret).getRevision();
        doReturn(mod).when(ret).getQNameModule();
        return ret;
    }
}
