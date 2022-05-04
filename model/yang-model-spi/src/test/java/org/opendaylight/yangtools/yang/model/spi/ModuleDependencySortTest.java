/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.Submodule;

@RunWith(MockitoJUnitRunner.class)
public class ModuleDependencySortTest {
    public static final QNameModule FOO_MODULE = QNameModule.create(XMLNamespace.of("foo"));
    public static final QNameModule BAR_MODULE = QNameModule.create(XMLNamespace.of("bar"));

    @Mock
    public Module fooNoRev;

    @Mock
    public ModuleImport fooNoRevImport;

    @Mock
    public Module bar;

    @Mock
    public Submodule barSubmodule;

    @Before
    public void before() {
        doReturn("foo").when(fooNoRev).getName();
        doReturn(FOO_MODULE.getNamespace()).when(fooNoRev).getNamespace();
        doReturn(FOO_MODULE.getRevision()).when(fooNoRev).getRevision();
        doReturn(Set.of()).when(fooNoRev).getImports();
        doReturn(Set.of()).when(fooNoRev).getSubmodules();

        doReturn(Unqualified.of("foo")).when(fooNoRevImport).getModuleName();
        doReturn(Optional.empty()).when(fooNoRevImport).getRevision();

        doReturn("bar").when(bar).getName();
        doReturn(BAR_MODULE.getNamespace()).when(bar).getNamespace();
        doReturn(BAR_MODULE.getRevision()).when(bar).getRevision();
        doReturn(YangVersion.VERSION_1).when(bar).getYangVersion();
        doReturn(Set.of()).when(bar).getImports();
        doReturn(Set.of(barSubmodule)).when(bar).getSubmodules();

        doReturn(Set.of(fooNoRevImport)).when(barSubmodule).getImports();
    }

    @Test
    public void testSimpleModules() {
        assertSortedTo(List.of(fooNoRev), fooNoRev);
    }

    @Test
    public void testSubmodules() {
        assertSortedTo(List.of(fooNoRev, bar), bar, fooNoRev);
        assertSortedTo(List.of(fooNoRev, bar), fooNoRev, bar);
    }

    private static void assertSortedTo(final List<Module> expected, final Module... modules) {
        assertEquals(expected, ModuleDependencySort.sort(modules));
    }
}
