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
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug6771Test {
    private static final String NS = "http://www.example.com/typedef-bug";
    private static final String REV = "1970-01-01";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName CONT_B = QName.create(NS, REV, "container-b");
    private static final QName LEAF_CONT_B = QName.create(NS, REV, "leaf-container-b");
    private static final QName INNER_CONTAINER = QName.create(NS, REV, "inner-container");

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

        final QName myChoice = QName.create(NS, REV, "my-choice");
        final QName caseOne = QName.create(NS, REV, "one");
        final QName caseTwo = QName.create(NS, REV, "two");
        final QName caseThree = QName.create(NS, REV, "three");
        final QName containerOne = QName.create(NS, REV, "container-one");
        final QName containerTwo = QName.create(NS, REV, "container-two");
        final QName containerThree = QName.create(NS, REV, "container-three");

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
        assertTrue(((LeafSchemaNode) schemaNode).getType() instanceof UnsignedIntegerTypeDefinition);
    }
}
