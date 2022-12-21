/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug5200Test extends AbstractYangTest {
    private static final String NS = "foo";
    private static final String REV = "2016-05-05";

    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug5200");

        QName root = QName.create(NS, REV, "root");
        QName myLeaf = QName.create(NS, REV, "my-leaf");
        QName myLeaf2 = QName.create(NS, REV, "my-leaf-2");

        SchemaNode myLeafNode = context.findDataTreeChild(root, myLeaf).get();
        SchemaNode myLeaf2Node = context.findDataTreeChild(root, myLeaf2).get();

        assertThat(myLeafNode, instanceOf(LeafSchemaNode.class));
        assertThat(myLeaf2Node, instanceOf(LeafSchemaNode.class));

        TypeDefinition<?> myLeafType = ((LeafSchemaNode) myLeafNode).getType();
        TypeDefinition<?> myLeaf2Type = ((LeafSchemaNode) myLeaf2Node).getType();

        assertThat(myLeafType, instanceOf(StringTypeDefinition.class));
        assertThat(myLeaf2Type, instanceOf(Int32TypeDefinition.class));

        final LengthConstraint lengthConstraint =
            ((StringTypeDefinition) myLeafType).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) myLeafType).getPatternConstraints();

        assertEquals(1, lengthConstraint.getAllowedRanges().asRanges().size());
        assertEquals(1, patternConstraints.size());

        assertEquals(Optional.of("lenght constraint error-app-tag"), lengthConstraint.getErrorAppTag());
        assertEquals(Optional.of("lenght constraint error-app-message"), lengthConstraint.getErrorMessage());

        PatternConstraint patternConstraint = patternConstraints.iterator().next();
        assertEquals(Optional.of("pattern constraint error-app-tag"), patternConstraint.getErrorAppTag());
        assertEquals(Optional.of("pattern constraint error-app-message"), patternConstraint.getErrorMessage());

        RangeConstraint<?> rangeConstraint = ((Int32TypeDefinition) myLeaf2Type).getRangeConstraint().get();
        assertEquals(1, rangeConstraint.getAllowedRanges().asRanges().size());

        assertEquals(Optional.of("range constraint error-app-tag"), rangeConstraint.getErrorAppTag());
        assertEquals(Optional.of("range constraint error-app-message"), rangeConstraint.getErrorMessage());
    }
}
