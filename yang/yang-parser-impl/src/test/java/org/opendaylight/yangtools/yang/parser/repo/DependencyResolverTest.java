/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;

@Deprecated
public class DependencyResolverTest {

    @Test
    public void testModulesWithoutRevisionAndImport() throws Exception {
        final Map<SourceIdentifier, YangModelDependencyInfo> map = new HashMap<>();

        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/no-revision/imported.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/no-revision/imported@2012-12-12.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/no-revision/top@2012-10-10.yang")));

        final DependencyResolver resolved = RevisionDependencyResolver.create(map);

        assertEquals(0, resolved.getUnresolvedSources().size());
        assertEquals(0, resolved.getUnsatisfiedImports().size());
    }

    @Test
    public void testSubmoduleNoModule() throws Exception {
        final Map<SourceIdentifier, YangModelDependencyInfo> map = new HashMap<>();

        // Subfoo does not have parent in reactor
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/subfoo.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/bar.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/baz.yang")));

        final DependencyResolver resolved = RevisionDependencyResolver.create(map);

        assertEquals(2, resolved.getResolvedSources().size());
        assertEquals(1, resolved.getUnresolvedSources().size());
        assertEquals(0, resolved.getUnsatisfiedImports().size());
    }

    @Test
    public void testSubmodule() throws Exception {
        final Map<SourceIdentifier, YangModelDependencyInfo> map = new HashMap<>();

        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/subfoo.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/foo.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/bar.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/model/baz.yang")));

        final DependencyResolver resolved = RevisionDependencyResolver.create(map);

        assertEquals(4, resolved.getResolvedSources().size());
        assertEquals(0, resolved.getUnresolvedSources().size());
        assertEquals(0, resolved.getUnsatisfiedImports().size());
    }

    private static void addToMap(final Map<SourceIdentifier, YangModelDependencyInfo> map, final YangModelDependencyInfo yangModelDependencyInfo) {
        map.put(getSourceId(yangModelDependencyInfo), yangModelDependencyInfo);
    }

    private static SourceIdentifier getSourceId(final YangModelDependencyInfo depInfo) {
        final String name = depInfo.getName();
        return RevisionSourceIdentifier.create(name, Optional.ofNullable(depInfo.getFormattedRevision()));
    }
}
