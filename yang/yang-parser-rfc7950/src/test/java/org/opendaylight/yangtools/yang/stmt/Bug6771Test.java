/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug6771Test {
    private static final String NS = "http://www.example.com/typedef-bug";
    private static final QName ROOT = QName.create(NS, "root");
    private static final QName CONT_B = QName.create(NS, "container-b");
    private static final QName LEAF_CONT_B = QName.create(NS, "leaf-container-b");
    private static final QName INNER_CONTAINER = QName.create(NS, "inner-container");

    @Test
    public void augmentTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6771/augment");
        assertNotNull(context);

        verifyLeafType(SchemaContextUtil
                .findDataSchemaNode(context, SchemaPath.create(true, ROOT, CONT_B, LEAF_CONT_B)));
        verifyLeafType(SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, CONT_B, INNER_CONTAINER, LEAF_CONT_B)));
    }

    @Test
    public void choiceCaseTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6771/choice-case");
        assertNotNull(context);

        final QName myChoice = QName.create(NS, "my-choice");
        final QName caseOne = QName.create(NS, "one");
        final QName caseTwo = QName.create(NS, "two");
        final QName caseThree = QName.create(NS, "three");
        final QName containerOne = QName.create(NS, "container-one");
        final QName containerTwo = QName.create(NS, "container-two");
        final QName containerThree = QName.create(NS, "container-three");

        verifyLeafType(SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, myChoice, caseOne, containerOne, LEAF_CONT_B)));
        verifyLeafType(SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, myChoice, caseTwo, containerTwo, LEAF_CONT_B)));
        verifyLeafType(SchemaContextUtil.findDataSchemaNode(context,
                SchemaPath.create(true, ROOT, myChoice, caseThree, containerThree, INNER_CONTAINER, LEAF_CONT_B)));
    }

    @Test
    public void groupingTest() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6771/grouping");
        assertNotNull(context);
        verifyLeafType(SchemaContextUtil
                .findDataSchemaNode(context, SchemaPath.create(true, ROOT, CONT_B, LEAF_CONT_B)));
    }

    private static void verifyLeafType(final SchemaNode schemaNode) {
        assertTrue(schemaNode instanceof LeafSchemaNode);
        assertTrue(((LeafSchemaNode) schemaNode).getType() instanceof Uint32TypeDefinition);
    }
}
