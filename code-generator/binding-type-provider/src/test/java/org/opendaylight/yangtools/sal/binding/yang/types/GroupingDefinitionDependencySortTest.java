/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

/**
 * Test suite for testing of GroupingDefinitionDependencySort.
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class GroupingDefinitionDependencySortTest {

    private SchemaContext schemaContext;

    @Before
    public void setUp() throws Exception {
        YangParserImpl parser = new YangParserImpl();
        InputStream stream = GroupingDefinitionDependencySortTest.class.getResourceAsStream("/grouping-definitions.yang");
        List<InputStream> inputStreams = Collections.singletonList(stream);
        final Set<Module> modules = parser.parseYangModelsFromStreams(inputStreams);
        schemaContext = parser.resolveSchemaContext(modules);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortWithNullGroupingDefinitions() throws Exception {
        final GroupingDefinitionDependencySort groupDependency = new GroupingDefinitionDependencySort();
        groupDependency.sort(null);
    }

    @Test
    public void testSort() throws Exception {
        final Set<GroupingDefinition> groupings = schemaContext.getGroupings();

        final GroupingDefinitionDependencySort groupDependency = new GroupingDefinitionDependencySort();
        final List<GroupingDefinition> result = groupDependency.sort(groupings);

        assertNotNull(result);
        assertTrue(!result.isEmpty());

        GroupingDefinition simpleGroup = null;
        GroupingDefinition innerGroup = null;
        GroupingDefinition parentWithInnerGroup = null;
        GroupingDefinition parentOfNestedGroups = null;
        GroupingDefinition complexGroup = null;

        for (final GroupingDefinition grouping : result) {
            if (grouping.getQName().getLocalName().equals("simple-group")) {
                simpleGroup = grouping;
            } else if (grouping.getQName().getLocalName().equals("inner-group")) {
                innerGroup = grouping;
            } else if (grouping.getQName().getLocalName().equals("parent-with-inner")) {
                parentWithInnerGroup = grouping;
            } else if (grouping.getQName().getLocalName().equals("parent-of-nested-groups")) {
                parentOfNestedGroups = grouping;
            } else if (grouping.getQName().getLocalName().equals("complex-grouping")) {
                complexGroup = grouping;
            }
        }

        assertNotNull(simpleGroup);
        assertNotNull(innerGroup);
        assertNotNull(parentWithInnerGroup);
        assertNotNull(parentOfNestedGroups);
        assertNotNull(complexGroup);
    }
}