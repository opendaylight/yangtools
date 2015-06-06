/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import com.google.common.collect.Sets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort.ModuleNodeImpl;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Edge;

public class ModuleDependencySortTest {
    private final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final ModuleBuilder a = mockModuleBuilder("a", null);
    private final ModuleBuilder b = mockModuleBuilder("b", null);
    private final ModuleBuilder c = mockModuleBuilder("c", null);
    private final ModuleBuilder d = mockModuleBuilder("d", null);

    @Test
    public void testValid() throws Exception {

        mockDependency(a, b);
        mockDependency(b, c);
        mockDependency(b, d);

        ModuleBuilder[] builders = new ModuleBuilder[] { d, b, c, a };

        List<ModuleBuilder> l = ModuleDependencySort.sort(builders);

        assertDependencyGraph(ModuleDependencySort.createModuleGraph(ModuleOrModuleBuilder.fromAll(
                Collections.<Module>emptySet(), Arrays.asList(builders))));

        Matcher<String> cOrD = anyOf(equalTo(c.getName()), equalTo(d.getName()));

        assertThat(l.get(0).getName(), cOrD);
        assertThat(l.get(1).getName(), cOrD);
        assertEquals(b.getName(), l.get(2).getName());
        assertEquals(a.getName(), l.get(3).getName());
    }

    @Test
    public void testValidModule() throws Exception {

        Date rev = new Date();
        Module a = mockModule("a", rev);
        Module b = mockModule("b", rev);
        Module c = mockModule("c", rev);

        mockDependency(a, b);
        mockDependency(b, c);
        mockDependency(a, c);

        Module[] builders = new Module[] { a, b, c };

        List<Module> l = ModuleDependencySort.sort(builders);

        assertEquals(c.getName(), l.get(0).getName());
        assertEquals(b.getName(), l.get(1).getName());
        assertEquals(a.getName(), l.get(2).getName());
    }

    @Test(expected = YangValidationException.class)
    public void testModuleTwice() throws Exception {
        ModuleBuilder a2 = mockModuleBuilder("a", null);

        ModuleBuilder[] builders = new ModuleBuilder[] { a, a2 };
        try {
            ModuleDependencySort.sort(builders);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("Module:a with revision:default declared twice"));
            throw e;
        }
    }

    @Test(expected = YangValidationException.class)
    public void testImportNotExistingModule() throws Exception {
        mockDependency(a, b);

        ModuleBuilder[] builders = new ModuleBuilder[] { a };
        try {
            ModuleDependencySort.sort(builders);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("Not existing module imported:b:default by:a:default"));
            throw e;
        }
    }

    @Test
    public void testImportTwice() throws Exception {
        mockDependency(a, b);
        mockDependency(c, b);

        ModuleBuilder[] builders = new ModuleBuilder[] { a, b, c };
        ModuleDependencySort.sort(builders);
    }

    @Test
    public void testModuleTwiceWithDifferentRevs() throws Exception {
        ModuleBuilder a2 = mockModuleBuilder("a", new Date());

        ModuleBuilder[] builders = new ModuleBuilder[] { a, a2 };
        ModuleDependencySort.sort(builders);
    }

    @Test(expected = YangValidationException.class)
    public void testModuleTwice2() throws Exception {
        Date rev = new Date();
        ModuleBuilder a2 = mockModuleBuilder("a", rev);
        ModuleBuilder a3 = mockModuleBuilder("a", rev);

        ModuleBuilder[] builders = new ModuleBuilder[] { a, a2, a3 };
        try {
            ModuleDependencySort.sort(builders);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("Module:a with revision:" + SIMPLE_DATE_FORMAT.format(rev)
                    + " declared twice"));
            throw e;
        }
    }

    private static void assertDependencyGraph(final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph) {
        for (Entry<String, Map<Date, ModuleNodeImpl>> node : moduleGraph.entrySet()) {
            String name = node.getKey();

            // Expects only one module revision

            Set<Edge> inEdges = node.getValue().values().iterator().next().getInEdges();
            Set<Edge> outEdges = node.getValue().values().iterator().next().getOutEdges();

            if (name.equals("a")) {
                assertEdgeCount(inEdges, 0, outEdges, 1);
            } else if (name.equals("b")) {
                assertEdgeCount(inEdges, 1, outEdges, 2);
            } else {
                assertEdgeCount(inEdges, 1, outEdges, 0);
            }
        }
    }

    private static void assertEdgeCount(final Set<Edge> inEdges, final int i, final Set<Edge> outEdges, final int j) {
        assertEquals(i, inEdges.size());
        assertEquals(j, outEdges.size());
    }

    private static void mockDependency(final ModuleBuilder a, final ModuleBuilder b) {
        ModuleImport imprt = mock(ModuleImport.class);
        doReturn(b.getName()).when(imprt).getModuleName();
        doReturn(b.getName()).when(imprt).getPrefix();
        doReturn(b.getRevision()).when(imprt).getRevision();
        a.getImports().put(b.getName(), imprt);
    }

    private static void mockDependency(final Module a, final Module b) {
        ModuleImport imprt = mock(ModuleImport.class);
        doReturn(b.getName()).when(imprt).getModuleName();
        doReturn(b.getRevision()).when(imprt).getRevision();
        a.getImports().add(imprt);
    }

    private static ModuleBuilder mockModuleBuilder(final String name, final Date rev) {
        ModuleBuilder a = mock(ModuleBuilder.class);
        doReturn(name).when(a).getName();
        Map<String, ModuleImport> map = new HashMap<>();
        doReturn(map).when(a).getImports();
        if (rev != null) {
            doReturn(rev).when(a).getRevision();
        }
        return a;
    }

    private static Module mockModule(final String name, final Date rev) {
        Module a = mock(Module.class);
        doReturn(name).when(a).getName();
        Set<ModuleImport> set = Sets.newHashSet();
        doReturn(set).when(a).getImports();
        if (rev != null) {
            doReturn(rev).when(a).getRevision();
        }
        return a;
    }
}
