/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;

public class Bug6771Test {
    private static final QNameModule NS = QNameModule.create(XMLNamespace.of("http://www.example.com/typedef-bug"));
    private static final QName ROOT = QName.create(NS, "root");
    private static final QName CONT_B = QName.create(NS, "container-b");
    private static final QName LEAF_CONT_B = QName.create(NS, "leaf-container-b");
    private static final QName INNER_CONTAINER = QName.create(NS, "inner-container");

    @Test
    public void augmentTest() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/bug6771/augment.yang")
            .getModuleStatement(NS);

        verifyLeafType(module, ROOT, CONT_B, LEAF_CONT_B);
        verifyLeafType(module, ROOT, CONT_B, INNER_CONTAINER, LEAF_CONT_B);
    }

    @Test
    public void choiceCaseTest() throws Exception {
        final ModuleEffectiveStatement module = StmtTestUtils.parseYangSource("/bugs/bug6771/choice-case.yang")
            .getModuleStatement(NS);

        final QName myChoice = QName.create(NS, "my-choice");
        final QName caseOne = QName.create(NS, "one");
        final QName caseTwo = QName.create(NS, "two");
        final QName caseThree = QName.create(NS, "three");
        final QName containerOne = QName.create(NS, "container-one");
        final QName containerTwo = QName.create(NS, "container-two");
        final QName containerThree = QName.create(NS, "container-three");

        verifyLeafType(module, ROOT, myChoice, caseOne, containerOne, LEAF_CONT_B);
        verifyLeafType(module, ROOT, myChoice, caseTwo, containerTwo, LEAF_CONT_B);
        verifyLeafType(module, ROOT, myChoice, caseThree, containerThree, INNER_CONTAINER, LEAF_CONT_B);
    }

    @Test
    public void groupingTest() throws Exception {
        verifyLeafType(StmtTestUtils.parseYangSource("/bugs/bug6771/grouping.yang").getModuleStatement(NS),
            ROOT, CONT_B, LEAF_CONT_B);
    }

    private static void verifyLeafType(final ModuleEffectiveStatement module, final QName... qnames) {
        final SchemaTreeEffectiveStatement<?> stmt = module.findSchemaTreeNode(qnames).orElse(null);
        assertThat(stmt, instanceOf(LeafSchemaNode.class));
        assertThat(((LeafSchemaNode) stmt).getType(), instanceOf(Uint32TypeDefinition.class));
    }
}
