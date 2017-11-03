/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug5200Test {
    private static final String NS = "foo";
    private static final String REV = "2016-05-05";

    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5200");
        assertNotNull(context);

        QName root = QName.create(NS, REV, "root");
        QName myLeaf = QName.create(NS, REV, "my-leaf");
        QName myLeaf2 = QName.create(NS, REV, "my-leaf-2");

        SchemaNode myLeafNode = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, root, myLeaf));
        SchemaNode myLeaf2Node = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, root, myLeaf2));

        assertTrue(myLeafNode instanceof LeafSchemaNode);
        assertTrue(myLeaf2Node instanceof LeafSchemaNode);

        TypeDefinition<?> myLeafType = ((LeafSchemaNode) myLeafNode).getType();
        TypeDefinition<?> myLeaf2Type = ((LeafSchemaNode) myLeaf2Node).getType();

        assertTrue(myLeafType instanceof StringTypeDefinition);
        assertTrue(myLeaf2Type instanceof IntegerTypeDefinition);

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

        RangeConstraint<?> rangeConstraint = ((IntegerTypeDefinition) myLeaf2Type).getRangeConstraint().get();
        assertEquals(1, rangeConstraint.getAllowedRanges().asRanges().size());

        assertEquals(Optional.of("range constraint error-app-tag"), rangeConstraint.getErrorAppTag());
        assertEquals(Optional.of("range constraint error-app-message"), rangeConstraint.getErrorMessage());
    }
}
