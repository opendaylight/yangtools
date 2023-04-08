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
import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

public class Bug5200Test extends AbstractYangTest {
    @Test
    public void test() {
        final var context = assertEffectiveModelDir("/bugs/bug5200");
        final var root = QName.create("foo", "2016-05-05", "root");

        var myLeafNode = context.findDataTreeChild(root, QName.create(root, "my-leaf")).orElseThrow();
        var myLeaf2Node = context.findDataTreeChild(root, QName.create(root, "my-leaf-2")).orElseThrow();

        assertThat(myLeafNode, instanceOf(LeafSchemaNode.class));
        assertThat(myLeaf2Node, instanceOf(LeafSchemaNode.class));

        var myLeafType = ((LeafSchemaNode) myLeafNode).getType();
        var myLeaf2Type = ((LeafSchemaNode) myLeaf2Node).getType();

        assertThat(myLeafType, instanceOf(StringTypeDefinition.class));
        assertThat(myLeaf2Type, instanceOf(Int32TypeDefinition.class));

        final var lengthConstraint = ((StringTypeDefinition) myLeafType).getLengthConstraint().orElseThrow();
        final var patternConstraints = ((StringTypeDefinition) myLeafType).getPatternConstraints();

        assertEquals(1, lengthConstraint.getAllowedRanges().asRanges().size());
        assertEquals(1, patternConstraints.size());

        assertEquals(Optional.of("lenght constraint error-app-tag"), lengthConstraint.getErrorAppTag());
        assertEquals(Optional.of("lenght constraint error-app-message"), lengthConstraint.getErrorMessage());

        final var patternConstraint = patternConstraints.iterator().next();
        assertEquals(Optional.of("pattern constraint error-app-tag"), patternConstraint.getErrorAppTag());
        assertEquals(Optional.of("pattern constraint error-app-message"), patternConstraint.getErrorMessage());

        var rangeConstraint = ((Int32TypeDefinition) myLeaf2Type).getRangeConstraint().orElseThrow();
        assertEquals(1, rangeConstraint.getAllowedRanges().asRanges().size());

        assertEquals(Optional.of("range constraint error-app-tag"), rangeConstraint.getErrorAppTag());
        assertEquals(Optional.of("range constraint error-app-message"), rangeConstraint.getErrorMessage());
    }
}
