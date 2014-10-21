/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;

public class LeafListSchemaNodeBuilderTest {

    private LeafListSchemaNodeBuilder baseSchemaNodeBuilder;
    private LeafListSchemaNodeBuilder leafListSchemaNodeBuilder;
    private LeafListSchemaNodeBuilder leafListSchemaNodeBuilder2;
    private LeafListSchemaNodeBuilder leafListSchemaNodeBuilder3;
    private LeafListSchemaNodeBuilder leafListSchemaNodeBuilder4;
    private LeafListSchemaNodeBuilder leafListSchemaNodeBuilder5;

    @Before
    public void init() {
        baseSchemaNodeBuilder = new LeafListSchemaNodeBuilder("module-test", 10, QName.create("List1"), SchemaPath.ROOT);
        baseSchemaNodeBuilder.setType(StringType.getInstance());
        baseSchemaNodeBuilder.setParent(null);

        leafListSchemaNodeBuilder = new LeafListSchemaNodeBuilder("module-test", 111, QName.create("test"), SchemaPath.ROOT, baseSchemaNodeBuilder.build());

        leafListSchemaNodeBuilder2 = leafListSchemaNodeBuilder;
        leafListSchemaNodeBuilder3 = new LeafListSchemaNodeBuilder("module-test", 10, QName.create("List1"), SchemaPath.create(false, QName.create("root"), QName.create("Cont1")));
        leafListSchemaNodeBuilder4 = new LeafListSchemaNodeBuilder("module-test", 10, QName.create("List1"), SchemaPath.create(false, QName.create("root"), QName.create("Cont1")));

        leafListSchemaNodeBuilder5 = new LeafListSchemaNodeBuilder("module-test", 10, QName.create("List1"), SchemaPath.create(false, QName.create("root"), QName.create("Cont1")));
        leafListSchemaNodeBuilder5.setParent(baseSchemaNodeBuilder);
    }

    @Test
    public void testEquals() {
        assertTrue(leafListSchemaNodeBuilder.equals(leafListSchemaNodeBuilder2));
        assertFalse(leafListSchemaNodeBuilder.equals(null));
        assertFalse(leafListSchemaNodeBuilder.equals("test"));
        assertFalse(leafListSchemaNodeBuilder.equals(leafListSchemaNodeBuilder3));
        assertTrue(leafListSchemaNodeBuilder4.equals(leafListSchemaNodeBuilder3));
        assertFalse(baseSchemaNodeBuilder.equals(leafListSchemaNodeBuilder4));
        assertFalse(leafListSchemaNodeBuilder5.equals(leafListSchemaNodeBuilder4));
    }
}
