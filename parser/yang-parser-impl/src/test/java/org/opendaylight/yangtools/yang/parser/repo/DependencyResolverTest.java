/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo.ModuleDependencyInfo;

@Deprecated
public class DependencyResolverTest {
    @Test
    public void testModulesWithoutRevisionAndImport() throws Exception {
        final var map = new HashMap<SourceIdentifier, YangModelDependencyInfo>();
        addToMap(map, "/no-revision/imported.yang");
        addToMap(map, "/no-revision/imported@2012-12-12.yang");
        addToMap(map, "/no-revision/top@2012-10-10.yang");

        final var resolved = RevisionDependencyResolver.create(map);
        assertEquals(0, resolved.getUnresolvedSources().size());
        assertEquals(0, resolved.getUnsatisfiedImports().size());
    }

    @Test
    public void testSubmoduleNoModule() throws Exception {
        final var map = new HashMap<SourceIdentifier, YangModelDependencyInfo>();
        // Subfoo does not have parent in reactor
        addToMap(map, "/model/subfoo.yang");
        addToMap(map, "/model/bar.yang");
        addToMap(map, "/model/baz.yang");

        final var resolved = RevisionDependencyResolver.create(map);
        assertEquals(2, resolved.getResolvedSources().size());
        assertEquals(1, resolved.getUnresolvedSources().size());
        assertEquals(0, resolved.getUnsatisfiedImports().size());
    }

    @Test
    public void testSubmodule() throws Exception {
        final var map = new HashMap<SourceIdentifier, YangModelDependencyInfo>();
        addToMap(map, "/model/subfoo.yang");
        addToMap(map, "/model/foo.yang");
        addToMap(map, "/model/bar.yang");
        addToMap(map, "/model/baz.yang");

        final var resolved = RevisionDependencyResolver.create(map);
        assertEquals(0, resolved.getUnresolvedSources().size());
        assertEquals(0, resolved.getUnsatisfiedImports().size());
        assertEquals(4, resolved.getResolvedSources().size());
    }

    private static void addToMap(final Map<SourceIdentifier, YangModelDependencyInfo> map, final String yangFileName)
            throws Exception {
        final var info = ModuleDependencyInfo.forYangText(YangTextSource.forResource(DependencyResolverTest.class,
            yangFileName));
        map.put(new SourceIdentifier(info.getName(), info.getFormattedRevision()), info);
    }
}
