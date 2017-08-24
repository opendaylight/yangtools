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

import com.google.common.collect.Range;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
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

        Map<Range<Integer>, ConstraintMetaDefinition> lengthConstraints =
                ((StringTypeDefinition) myLeafType).getLengthConstraints().asMapOfRanges();
        List<PatternConstraint> patternConstraints = ((StringTypeDefinition) myLeafType).getPatternConstraints();

        assertEquals(1, lengthConstraints.size());
        assertEquals(1, patternConstraints.size());

        ConstraintMetaDefinition lenghtConstraint = lengthConstraints.values().iterator().next();
        assertEquals("lenght constraint error-app-tag", lenghtConstraint.getErrorAppTag());
        assertEquals("lenght constraint error-app-message", lenghtConstraint.getErrorMessage());

        PatternConstraint patternConstraint = patternConstraints.iterator().next();
        assertEquals("pattern constraint error-app-tag", patternConstraint.getErrorAppTag());
        assertEquals("pattern constraint error-app-message", patternConstraint.getErrorMessage());

        List<RangeConstraint> rangeConstraints = ((IntegerTypeDefinition) myLeaf2Type).getRangeConstraints();
        assertEquals(1, rangeConstraints.size());

        RangeConstraint rangeConstraint = rangeConstraints.iterator().next();
        assertEquals("range constraint error-app-tag", rangeConstraint.getErrorAppTag());
        assertEquals("range constraint error-app-message", rangeConstraint.getErrorMessage());
    }
}
