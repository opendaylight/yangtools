/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;

/**
 * Test suite for increasing of test coverage of LeafListSchemaNodeBuilder implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public class LeafListSchemaNodeBuilderTest extends AbstractBuilderTest {

    @Test
    public void testLeafListSchemaNodeBuilderWithBaseLeafListSchemaNode() {
        final String baseLeafListLocalName = "base-leaf-list";
        final QName baseLeafListQName = QName.create(module.getNamespace(), module.getRevision(), baseLeafListLocalName);
        final SchemaPath baseLeafListPath = SchemaPath.create(true, baseLeafListQName);
        final LeafListSchemaNodeBuilder baseLeafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, baseLeafListQName, baseLeafListPath);

        final UnknownSchemaNodeBuilder unknownNodeBuilder = provideUnknownNodeBuilder();

        baseLeafListBuilder.addUnknownNodeBuilder(unknownNodeBuilder);
        baseLeafListBuilder.setType(Uint16.getInstance());
        final LeafListSchemaNode leafList = baseLeafListBuilder.build();

        assertNotNull(leafList);
        assertFalse(leafList.getUnknownSchemaNodes().isEmpty());
        assertEquals(leafList.getUnknownSchemaNodes().size(), 1);

        final String leafListLocalName = "extended-leaf-list";
        final QName leafListQName = QName.create(module.getNamespace(), module.getRevision(), leafListLocalName);
        final SchemaPath leafListPath = SchemaPath.create(true, leafListQName);
        final LeafListSchemaNodeBuilder leafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            15, leafListQName, leafListPath, leafList);

        final LeafListSchemaNode extendedLeafList = leafListBuilder.build();

        assertNotNull(extendedLeafList);
        assertFalse(extendedLeafList.getUnknownSchemaNodes().isEmpty());
        assertEquals(extendedLeafList.getUnknownSchemaNodes().size(), 1);

        assertNotEquals(leafList, extendedLeafList);

        assertTrue(extendedLeafList instanceof DerivableSchemaNode);
        assertTrue(((DerivableSchemaNode) extendedLeafList).getOriginal().isPresent());
        assertEquals(leafList, ((DerivableSchemaNode) extendedLeafList).getOriginal().get());
    }

    @Test
    public void testEquals() {
        final String baseLeafListLocalName = "leaf-list1";
        final QName baseLeafListQName = QName.create(module.getNamespace(), module.getRevision(), baseLeafListLocalName);
        final SchemaPath baseLeafListPath = SchemaPath.create(true, baseLeafListQName);
        final LeafListSchemaNodeBuilder baseLeafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, baseLeafListQName, baseLeafListPath);

        baseLeafListBuilder.setType(Uint16.getInstance());
        final LeafListSchemaNode leafList = baseLeafListBuilder.build();

        final String leafListLocalName = "leaf-list2";
        final QName leafListQName2 = QName.create(module.getNamespace(), module.getRevision(), leafListLocalName);
        final SchemaPath leafListPath2 = SchemaPath.create(true, leafListQName2);
        final LeafListSchemaNodeBuilder leafListBuilder2 = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, leafListQName2, leafListPath2);

        leafListBuilder2.setType(Uint16.getInstance());
        final LeafListSchemaNode leafList2 = leafListBuilder2.build();

        assertNotEquals(baseLeafListBuilder, null);
        assertNotEquals(baseLeafListBuilder, leafListBuilder2);

        assertNotEquals(leafList, null);
        assertNotEquals(leafList, leafList2);

        final QName containerQName = QName.create(module.getNamespace(), module.getRevision(), "parent-container");
        final String leafListLocalName3 = "leaf-list2";
        final QName leafListQName3 = QName.create(module.getNamespace(), module.getRevision(), leafListLocalName3);
        final SchemaPath leafListPath3 = SchemaPath.create(true, containerQName, leafListQName3);
        final LeafListSchemaNodeBuilder leafListBuilder3 = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, leafListQName3, leafListPath3);

        baseLeafListBuilder.setType(Uint16.getInstance());
        final LeafListSchemaNode leafList3 = baseLeafListBuilder.build();

        assertNotEquals(leafListBuilder2, leafListBuilder3);
        assertNotEquals(leafList3, leafList2);
    }

    @Test
    public void testLeafListSchemaNodeImplProperties() {
        final String descString = "my leaf list description";
        final String leafListLocalName = "base-leaf-list";
        final QName leafListQName = QName.create(module.getNamespace(), module.getRevision(), leafListLocalName);
        final SchemaPath leafListPath = SchemaPath.create(true, leafListQName);
        final LeafListSchemaNodeBuilder leafListBuilder = new LeafListSchemaNodeBuilder(module.getModuleName(),
            10, leafListQName, leafListPath);

        leafListBuilder.setType(Uint16.getInstance());
        leafListBuilder.setDescription(descString);
        final LeafListSchemaNode leafList = leafListBuilder.build();

        assertEquals(leafList.getQName(), leafListQName);
        assertEquals(leafList.getDescription(), descString);
        assertEquals(leafList.getStatus(), Status.CURRENT);
        assertEquals(leafList.getPath(), leafListPath);
        assertEquals(leafList.getType(), Uint16.getInstance());
        assertNull(leafList.getReference());

        assertEquals("LeafListSchemaNodeImpl[(urn:opendaylight.rpc:def:test-model?revision=2014-01-06)base-leaf-list]",
            leafList.toString());
    }
}
