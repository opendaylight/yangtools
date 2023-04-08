/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug5200Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug5200");
        final var root = QName.create("foo", "2016-05-05", "root");

        var myLeafNode = context.findDataTreeChild(root, QName.create(root, "my-leaf")).orElseThrow();
        var myLeaf2Node = context.findDataTreeChild(root, QName.create(root, "my-leaf-2")).orElseThrow();

        final var myLeafType = assertInstanceOf(StringTypeDefinition.class,
            assertInstanceOf(LeafSchemaNode.class, myLeafNode).getType());
        final var myLeaf2Type = assertInstanceOf(Int32TypeDefinition.class,
            assertInstanceOf(LeafSchemaNode.class, myLeaf2Node).getType());

        final var lengthConstraint = myLeafType.getLengthConstraint().orElseThrow();
        final var patternConstraints = myLeafType.getPatternConstraints();

        assertEquals(1, lengthConstraint.getAllowedRanges().asRanges().size());
        assertEquals(1, patternConstraints.size());

        assertEquals(Optional.of("lenght constraint error-app-tag"), lengthConstraint.getErrorAppTag());
        assertEquals(Optional.of("lenght constraint error-app-message"), lengthConstraint.getErrorMessage());

        final var patternConstraint = patternConstraints.iterator().next();
        assertEquals(Optional.of("pattern constraint error-app-tag"), patternConstraint.getErrorAppTag());
        assertEquals(Optional.of("pattern constraint error-app-message"), patternConstraint.getErrorMessage());

        var rangeConstraint = myLeaf2Type.getRangeConstraint().orElseThrow();
        assertEquals(1, rangeConstraint.getAllowedRanges().asRanges().size());

        assertEquals(Optional.of("range constraint error-app-tag"), rangeConstraint.getErrorAppTag());
        assertEquals(Optional.of("range constraint error-app-message"), rangeConstraint.getErrorMessage());
    }
}
