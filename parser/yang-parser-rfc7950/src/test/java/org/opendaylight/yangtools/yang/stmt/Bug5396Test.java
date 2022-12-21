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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

class Bug5396Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug5396");

        QName root = QName.create("foo", "root");
        QName myLeaf2 = QName.create("foo", "my-leaf2");

        SchemaNode findDataSchemaNode = context.findDataTreeChild(root, myLeaf2).get();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));

        LeafSchemaNode leaf2 = (LeafSchemaNode) findDataSchemaNode;
        TypeDefinition<?> type = leaf2.getType();
        assertThat(type, instanceOf(UnionTypeDefinition.class));

        UnionTypeDefinition union = (UnionTypeDefinition) type;
        List<TypeDefinition<?>> types = union.getTypes();

        assertEquals(4, types.size());

        TypeDefinition<?> type0 = types.get(0);
        TypeDefinition<?> type1 = types.get(1);
        TypeDefinition<?> type2 = types.get(2);
        TypeDefinition<?> type3 = types.get(3);

        assertNotEquals(type0, type1);
        assertNotEquals(type0, type2);
        assertNotEquals(type0, type3);

        assertThat(type0, instanceOf(StringTypeDefinition.class));
        assertThat(type1, instanceOf(StringTypeDefinition.class));
        assertThat(type2, instanceOf(StringTypeDefinition.class));
        assertThat(type3, instanceOf(StringTypeDefinition.class));

        StringTypeDefinition stringType0 = (StringTypeDefinition) type0;
        StringTypeDefinition stringType1 = (StringTypeDefinition) type1;
        StringTypeDefinition stringType2 = (StringTypeDefinition) type2;
        StringTypeDefinition stringType3 = (StringTypeDefinition) type3;

        final List<PatternConstraint> patternConstraints0 = stringType0.getPatternConstraints();
        final List<PatternConstraint> patternConstraints1 = stringType1.getPatternConstraints();
        final List<PatternConstraint> patternConstraints2 = stringType2.getPatternConstraints();
        final List<PatternConstraint> patternConstraints3 = stringType3.getPatternConstraints();

        assertEquals(1, patternConstraints0.size());
        assertEquals(1, patternConstraints1.size());
        assertEquals(1, patternConstraints2.size());
        assertEquals(1, patternConstraints3.size());

        final PatternConstraint patternConstraint0 = patternConstraints0.get(0);
        final PatternConstraint patternConstraint1 = patternConstraints1.get(0);
        final PatternConstraint patternConstraint2 = patternConstraints2.get(0);
        final PatternConstraint patternConstraint3 = patternConstraints3.get(0);

        assertEquals("^(?:dp[0-9]+o[0-9]+(d[0-9]+)?)$", patternConstraint0.getJavaPatternString());
        assertEquals("^(?:dp[0-9]+s[0-9]+(f[0-9]+)?(d[0-9]+)?)$", patternConstraint1.getJavaPatternString());
        assertEquals("^(?:dp[0-9]+(P[0-9]+)?p[0-9]{1,3}s[0-9]{1,3}(f[0-9]+)?(d[0-9]+)?)$",
            patternConstraint2.getJavaPatternString());
        assertEquals("^(?:dp[0-9]+p[0-9]+p[0-9]+)$", patternConstraint3.getJavaPatternString());
    }
}
