/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class OrderingTest {

    @Test
    public void testOrderingTypedef() throws URISyntaxException,
            SourceException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                "/model").toURI());
        Module bar = TestUtils.findModule(modules, "bar");
        Set<TypeDefinition<?>> typedefs = bar.getTypeDefinitions();
        String[] expectedOrder = new String[] { "int32-ext1", "int32-ext2",
                "string-ext1", "string-ext2", "string-ext3", "string-ext4",
                "invalid-string-pattern", "multiple-pattern-string",
                "my-decimal-type", "my-union", "my-union-ext", "nested-union2" };
        String[] actualOrder = new String[typedefs.size()];

        int i = 0;
        for (TypeDefinition<?> type : typedefs) {
            actualOrder[i] = type.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingChildNodes() throws URISyntaxException,
            SourceException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                "/model").toURI());
        Module foo = TestUtils.findModule(modules, "foo");
        AugmentationSchema augment1 = null;
        for (AugmentationSchema as : foo.getAugmentations()) {
            if (as.getChildNodes().size() == 5) {
                augment1 = as;
                break;
            }
        }
        assertNotNull(augment1);

        String[] expectedOrder = new String[] { "ds0ChannelNumber",
                "interface-id", "my-type", "schemas", "odl" };
        String[] actualOrder = new String[expectedOrder.length];

        int i = 0;
        for (DataSchemaNode augmentChild : augment1.getChildNodes()) {
            actualOrder[i] = augmentChild.getQName().getLocalName();
            i++;
        }

        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingNestedChildNodes1() throws URISyntaxException,
            SourceException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                "/model").toURI());
        Module foo = TestUtils.findModule(modules, "foo");

        Collection<DataSchemaNode> childNodes = foo.getChildNodes();
        String[] expectedOrder = new String[] { "int32-leaf", "string-leaf",
                "invalid-pattern-string-leaf",
                "invalid-direct-string-pattern-def-leaf",
                "multiple-pattern-string-leaf",
                "multiple-pattern-direct-string-def-leaf", "length-leaf",
                "decimal-leaf", "decimal-leaf2", "ext", "union-leaf",
                "custom-union-leaf", "transfer", "datas", "mycont", "data",
                "how", "address", "port", "addresses", "peer", "id", "foo-id",
                "sub-ext", "sub-transfer", "sub-datas" };
        String[] actualOrder = new String[childNodes.size()];

        int i = 0;
        for (DataSchemaNode child : childNodes) {
            actualOrder[i] = child.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingNestedChildNodes2() throws URISyntaxException,
            SourceException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource(
                "/model").toURI());
        Module baz = TestUtils.findModule(modules, "baz");
        Set<GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        GroupingDefinition target = groupings.iterator().next();

        Collection<DataSchemaNode> childNodes = target.getChildNodes();
        String[] expectedOrder = new String[] { "data", "how", "address",
                "port", "addresses" };
        String[] actualOrder = new String[childNodes.size()];

        int i = 0;
        for (DataSchemaNode child : childNodes) {
            actualOrder[i] = child.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingNestedChildNodes3() throws Exception {
        Module baz = TestUtils.loadModule(getClass().getResourceAsStream(
                "/ordering/foo.yang"));
        ContainerSchemaNode x = (ContainerSchemaNode) baz
                .getDataChildByName("x");
        Collection<DataSchemaNode> childNodes = x.getChildNodes();

        String[] expectedOrder = new String[] { "x15", "x10", "x5", "x1", "a5",
                "a1", "x2", "b5", "b1", "x3", "ax15", "ax5" };
        String[] actualOrder = new String[childNodes.size()];

        int i = 0;
        for (DataSchemaNode child : childNodes) {
            actualOrder[i] = child.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

}
