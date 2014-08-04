/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;

public class DependencyResolverTest {

    @Test
    public void testModulesWithoutRevisionAndImport() throws Exception {
        final Map<SourceIdentifier, YangModelDependencyInfo> map = new HashMap<>();

        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/no-revision/imported.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/no-revision/imported@2012-12-12.yang")));
        addToMap(map, YangModelDependencyInfo.ModuleDependencyInfo.fromInputStream(getClass().getResourceAsStream("/no-revision/top@2012-10-10.yang")));

        final DependencyResolver resolved = DependencyResolver.create(map);

        Assert.assertEquals(0, resolved.getUnresolvedSources().size());
        Assert.assertEquals(0, resolved.getUnsatisfiedImports().size());
    }

    private void addToMap(final Map<SourceIdentifier, YangModelDependencyInfo> map, final YangModelDependencyInfo yangModelDependencyInfo) {
        map.put(getSourceId(yangModelDependencyInfo), yangModelDependencyInfo);
    }

    private static SourceIdentifier getSourceId(final YangModelDependencyInfo depInfo) {
        final String name = depInfo.getName();
        final String formattedRevision =
                depInfo.getFormattedRevision() == null ? YangModelDependencyInfo.NOT_PRESENT_FORMATTED_REVISION : depInfo.getFormattedRevision();
        return new SourceIdentifier(name, Optional.of(formattedRevision));
    }
}
