/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug5396Test {
    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5396");
        assertNotNull(context);

        QName root = QName.create("foo", "1970-01-01", "root");
        QName myLeaf2 = QName.create("foo", "1970-01-01", "my-leaf2");

        SchemaPath schemaPath = SchemaPath.create(true, root, myLeaf2);
        SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, schemaPath);
        assertTrue(findDataSchemaNode instanceof LeafSchemaNode);

        LeafSchemaNode leaf2 = (LeafSchemaNode) findDataSchemaNode;
        TypeDefinition<?> type = leaf2.getType();
        assertTrue(type instanceof UnionTypeDefinition);

        UnionTypeDefinition union = (UnionTypeDefinition) type;
        List<TypeDefinition<?>> types = union.getTypes();

        assertEquals(4, types.size());

        TypeDefinition<?> type0 = types.get(0);
        TypeDefinition<?> type1 = types.get(1);
        TypeDefinition<?> type2 = types.get(2);
        TypeDefinition<?> type3 = types.get(3);

        assertFalse(type0.equals(type1));
        assertFalse(type0.equals(type2));
        assertFalse(type0.equals(type3));

        assertTrue(type0 instanceof StringTypeDefinition);
        assertTrue(type1 instanceof StringTypeDefinition);
        assertTrue(type2 instanceof StringTypeDefinition);
        assertTrue(type3 instanceof StringTypeDefinition);

        StringTypeDefinition stringType0 = (StringTypeDefinition) type0;
        StringTypeDefinition stringType1 = (StringTypeDefinition) type1;
        StringTypeDefinition stringType2 = (StringTypeDefinition) type2;
        StringTypeDefinition stringType3 = (StringTypeDefinition) type3;

        List<PatternConstraint> patternConstraints0 = stringType0.getPatternConstraints();
        List<PatternConstraint> patternConstraints1 = stringType1.getPatternConstraints();
        List<PatternConstraint> patternConstraints2 = stringType2.getPatternConstraints();
        List<PatternConstraint> patternConstraints3 = stringType3.getPatternConstraints();

        assertEquals(1, patternConstraints0.size());
        assertEquals(1, patternConstraints1.size());
        assertEquals(1, patternConstraints2.size());
        assertEquals(1, patternConstraints3.size());

        PatternConstraint patternConstraint0 = patternConstraints0.get(0);
        PatternConstraint patternConstraint1 = patternConstraints1.get(0);
        PatternConstraint patternConstraint2 = patternConstraints2.get(0);
        PatternConstraint patternConstraint3 = patternConstraints3.get(0);

        assertEquals("^dp[0-9]+o[0-9]+(d[0-9]+)?$", patternConstraint0.getRegularExpression());
        assertEquals("^dp[0-9]+s[0-9]+(f[0-9]+)?(d[0-9]+)?$", patternConstraint1.getRegularExpression());
        assertEquals("^dp[0-9]+(P[0-9]+)?p[0-9]{1,3}s[0-9]{1,3}(f[0-9]+)?(d[0-9]+)?$",
                patternConstraint2.getRegularExpression());
        assertEquals("^dp[0-9]+p[0-9]+p[0-9]+$", patternConstraint3.getRegularExpression());
    }
}
