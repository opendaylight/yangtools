/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.Submodule;

@ExtendWith(MockitoExtension.class)
class ModuleDependencySortTest {
    static final QNameModule FOO_MODULE = QNameModule.of("foo");
    static final QNameModule BAR_MODULE = QNameModule.of("bar");

    @Mock
    Module fooNoRev;

    @Mock
    ModuleImport fooNoRevImport;

    @Mock
    Module bar;

    @Mock
    Submodule barSubmodule;

    @BeforeEach
    void before() {
        doReturn("foo").when(fooNoRev).getName();
        doReturn(FOO_MODULE.namespace()).when(fooNoRev).getNamespace();
        doReturn(FOO_MODULE.findRevision()).when(fooNoRev).getRevision();
        doReturn(Set.of()).when(fooNoRev).getImports();
        doReturn(Set.of()).when(fooNoRev).getSubmodules();
    }

    @Test
    void testSimpleModules() {
        assertSortedTo(List.of(fooNoRev), fooNoRev);
    }

    @Test
    void testSubmodules() {
        doReturn(Unqualified.of("foo")).when(fooNoRevImport).getModuleName();
        doReturn(Optional.empty()).when(fooNoRevImport).getRevision();

        doReturn("bar").when(bar).getName();
        doReturn(BAR_MODULE.namespace()).when(bar).getNamespace();
        doReturn(BAR_MODULE.findRevision()).when(bar).getRevision();
        doReturn(YangVersion.VERSION_1).when(bar).getYangVersion();
        doReturn(Set.of()).when(bar).getImports();
        doReturn(Set.of(barSubmodule)).when(bar).getSubmodules();

        doReturn(Set.of(fooNoRevImport)).when(barSubmodule).getImports();

        assertSortedTo(List.of(fooNoRev, bar), bar, fooNoRev);
        assertSortedTo(List.of(fooNoRev, bar), fooNoRev, bar);
    }

    private static void assertSortedTo(final List<Module> expected, final Module... modules) {
        assertEquals(expected, ModuleDependencySort.sort(modules));
    }
}
