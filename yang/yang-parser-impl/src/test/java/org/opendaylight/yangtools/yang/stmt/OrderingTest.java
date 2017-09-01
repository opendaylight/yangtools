/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class OrderingTest {

    private SchemaContext context;
    private Module foo;
    private Module bar;
    private Module baz;

    @Before
    public void setup() throws ReactorException, IOException, YangSyntaxErrorException, URISyntaxException {
        context = TestUtils.loadModules(getClass().getResource("/model").toURI());
        foo = TestUtils.findModule(context, "foo").get();
        bar = TestUtils.findModule(context, "bar").get();
        baz = TestUtils.findModule(context, "baz").get();
    }

    @Test
    public void testOrderingTypedef() throws Exception {
        final Set<TypeDefinition<?>> typedefs = bar.getTypeDefinitions();
        final String[] expectedOrder = new String[] { "int32-ext1", "int32-ext2",
                "string-ext1", "string-ext2", "string-ext3", "string-ext4",
                "invalid-string-pattern", "multiple-pattern-string",
                "my-decimal-type", "my-union", "my-union-ext", "nested-union2" };
        final String[] actualOrder = new String[typedefs.size()];

        int i = 0;
        for (final TypeDefinition<?> type : typedefs) {
            actualOrder[i] = type.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingChildNodes() throws Exception {
        AugmentationSchema augment1 = null;
        for (final AugmentationSchema as : foo.getAugmentations()) {
            if (as.getChildNodes().size() == 5) {
                augment1 = as;
                break;
            }
        }
        assertNotNull(augment1);

        final String[] expectedOrder = new String[] { "ds0ChannelNumber",
                "interface-id", "my-type", "schemas", "odl" };
        final String[] actualOrder = new String[expectedOrder.length];

        int i = 0;
        for (final DataSchemaNode augmentChild : augment1.getChildNodes()) {
            actualOrder[i] = augmentChild.getQName().getLocalName();
            i++;
        }

        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingNestedChildNodes1() throws Exception {
        final Collection<DataSchemaNode> childNodes = foo.getChildNodes();
        final String[] expectedOrder = new String[] { "int32-leaf", "string-leaf",
                "invalid-pattern-string-leaf",
                "invalid-direct-string-pattern-def-leaf",
                "multiple-pattern-string-leaf",
                "multiple-pattern-direct-string-def-leaf", "length-leaf",
                "decimal-leaf", "decimal-leaf2", "ext", "union-leaf",
                "custom-union-leaf", "transfer", "datas", "mycont", "data",
                "how", "address", "port", "addresses", "peer", "id", "foo-id",
                "sub-ext", "sub-transfer", "sub-datas" };
        final String[] actualOrder = new String[childNodes.size()];

        int i = 0;
        for (final DataSchemaNode child : childNodes) {
            actualOrder[i] = child.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingNestedChildNodes2() throws Exception {
        final Set<GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition target = groupings.iterator().next();

        final Collection<DataSchemaNode> childNodes = target.getChildNodes();
        final String[] expectedOrder = new String[] { "data", "how", "address",
                "port", "addresses" };
        final String[] actualOrder = new String[childNodes.size()];

        int i = 0;
        for (final DataSchemaNode child : childNodes) {
            actualOrder[i] = child.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }

    @Test
    public void testOrderingNestedChildNodes3() throws Exception {
        final Module baz = TestUtils.loadModuleResources(getClass(), "/ordering/foo.yang")
                .getModules().iterator().next();
        final ContainerSchemaNode x = (ContainerSchemaNode) baz
                .getDataChildByName(QName.create(baz.getQNameModule(), "x"));
        final Collection<DataSchemaNode> childNodes = x.getChildNodes();

        final String[] expectedOrder = new String[] { "x15", "x10", "x5", "x1", "a5",
                "a1", "x2", "b5", "b1", "x3", "ax15", "ax5" };
        final String[] actualOrder = new String[childNodes.size()];

        int i = 0;
        for (final DataSchemaNode child : childNodes) {
            actualOrder[i] = child.getQName().getLocalName();
            i++;
        }
        assertArrayEquals(expectedOrder, actualOrder);
    }
}
