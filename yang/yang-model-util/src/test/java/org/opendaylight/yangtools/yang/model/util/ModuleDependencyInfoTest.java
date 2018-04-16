/*
l * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

@RunWith(MockitoJUnitRunner.class)
public class ModuleDependencyInfoTest {
    private static final QNameModule FOO_MODULE = QNameModule.create(URI.create("foo"));
    private static final QNameModule BAR_MODULE = QNameModule.create(URI.create("bar"));

    @Mock
    private Module fooNoRev;

    @Mock
    private ModuleImport fooNoRevImport;

    @Mock
    private Module bar;

    @Mock
    private Module barSubmodule;

    @Before
    public void before() {
        doReturn("foo").when(fooNoRev).getName();
        doReturn(FOO_MODULE).when(fooNoRev).getQNameModule();
        doReturn(FOO_MODULE.getNamespace()).when(fooNoRev).getNamespace();
        doReturn(FOO_MODULE.getRevision()).when(fooNoRev).getRevision();
        doReturn(YangVersion.VERSION_1).when(fooNoRev).getYangVersion();
        doReturn(ImmutableSet.of()).when(fooNoRev).getImports();
        doReturn(ImmutableSet.of()).when(fooNoRev).getSubmodules();

        doReturn("foo").when(fooNoRevImport).getModuleName();
        doReturn(Optional.empty()).when(fooNoRevImport).getRevision();

        doReturn("bar").when(bar).getName();
        doReturn(BAR_MODULE).when(bar).getQNameModule();
        doReturn(BAR_MODULE.getNamespace()).when(bar).getNamespace();
        doReturn(BAR_MODULE.getRevision()).when(bar).getRevision();
        doReturn(YangVersion.VERSION_1).when(bar).getYangVersion();
        doReturn(ImmutableSet.of()).when(bar).getImports();
        doReturn(ImmutableSet.of(barSubmodule)).when(bar).getSubmodules();

        doReturn("bar-submodule").when(barSubmodule).getName();
        doReturn(BAR_MODULE).when(barSubmodule).getQNameModule();
        doReturn(BAR_MODULE.getNamespace()).when(barSubmodule).getNamespace();
        doReturn(BAR_MODULE.getRevision()).when(barSubmodule).getRevision();
        doReturn(ImmutableSet.of(fooNoRevImport)).when(barSubmodule).getImports();
        doReturn(ImmutableSet.of()).when(barSubmodule).getSubmodules();
    }

    @Test
    public void testSimpleModules() {
        assertSortedTo(of(fooNoRev), fooNoRev);
    }

    @Test
    public void testSubmodules() {
        assertSortedTo(of(fooNoRev, bar), bar, fooNoRev);
        assertSortedTo(of(fooNoRev, bar), fooNoRev, bar);
    }

    private static void assertSortedTo(final List<Module> expected, final Module... modules) {
        assertEquals(expected, ModuleDependencySort.sort(modules));
    }
}
