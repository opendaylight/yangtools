/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingBuilderImpl;

public class GroupingDefinitionDependencySortTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void testSortMethod() {
        GroupingDefinitionDependencySort groupingDefinitionDependencySort = new GroupingDefinitionDependencySort();
        List<GroupingDefinition> unsortedGroupingDefs = new ArrayList<>();

        GroupingBuilderImpl groupingBuilderImpl = new GroupingBuilderImpl("test-module", 111, QName.create("leaf1"), SchemaPath.create(false, QName.create("Cont1"), QName.create("Cont2")));
        GroupingBuilderImpl groupingBuilderImpl2 = new GroupingBuilderImpl("test-module", 222, QName.create("leaf2"), SchemaPath.create(false, QName.create("Cont1")));
        GroupingBuilderImpl groupingBuilderImpl3 = new GroupingBuilderImpl("test-module2", 111, QName.create("leaf3"), SchemaPath.create(false, QName.create("Cont1"), QName.create("Cont2")));
        GroupingBuilderImpl groupingBuilderImpl4 = new GroupingBuilderImpl("test-module2", 222, QName.create("leaf4"), SchemaPath.create(false, QName.create("Cont1"), QName.create("Cont2"), QName.create("List1")));
        GroupingBuilderImpl groupingBuilderImpl5 = new GroupingBuilderImpl("test-module2", 333, QName.create("leaf5"), SchemaPath.create(false, QName.create("Cont1")));

        unsortedGroupingDefs.add(groupingBuilderImpl.build());
        unsortedGroupingDefs.add(groupingBuilderImpl.build());
        unsortedGroupingDefs.add(groupingBuilderImpl2.build());
        unsortedGroupingDefs.add(groupingBuilderImpl3.build());
        unsortedGroupingDefs.add(groupingBuilderImpl4.build());
        unsortedGroupingDefs.add(groupingBuilderImpl5.build());

        List<GroupingDefinition> sortedGroupingDefs = groupingDefinitionDependencySort.sort(unsortedGroupingDefs);
        assertNotNull(sortedGroupingDefs);

        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Set of Type Definitions cannot be NULL!");
        groupingDefinitionDependencySort.sort(null);
    }
}
