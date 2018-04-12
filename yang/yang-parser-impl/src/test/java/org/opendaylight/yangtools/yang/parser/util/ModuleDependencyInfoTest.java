/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;

@RunWith(MockitoJUnitRunner.class)
public class ModuleDependencyInfoTest {
    private static final QNameModule TEST = QNameModule.create(URI.create("foo"), null);

    @Mock
    private Module fooNoRev;

    @Before
    public void before() {
        doReturn(ImmutableSet.of()).when(fooNoRev).getImports();
        doReturn("foo").when(fooNoRev).getName();
        doReturn(TEST).when(fooNoRev).getQNameModule();
        doReturn(TEST.getNamespace()).when(fooNoRev).getNamespace();
        doReturn(TEST.getRevision()).when(fooNoRev).getRevision();
    }

    @Test
    public void testSimpleModules() {
        assertSortedTo(of(fooNoRev), fooNoRev);
    }

    private static void assertSortedTo(final List<Module> expected, final Module... modules) {
        assertEquals(expected, ModuleDependencySort.sort(modules));
    }
}
