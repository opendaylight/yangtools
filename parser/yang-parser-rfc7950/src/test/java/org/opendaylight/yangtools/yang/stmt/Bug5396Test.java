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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class Bug5396Test extends AbstractYangTest {
    @Test
    public void test() {
        final var context = assertEffectiveModelDir("/bugs/bug5396");

        QName root = QName.create("foo", "root");
        QName myLeaf2 = QName.create("foo", "my-leaf2");

        var findDataSchemaNode = context.findDataTreeChild(root, myLeaf2).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));

        var leaf2 = (LeafSchemaNode) findDataSchemaNode;
        var type = leaf2.getType();
        assertThat(type, instanceOf(UnionTypeDefinition.class));

        var union = (UnionTypeDefinition) type;
        var types = union.getTypes();

        assertEquals(4, types.size());

        var type0 = types.get(0);
        var type1 = types.get(1);
        var type2 = types.get(2);
        var type3 = types.get(3);

        assertNotEquals(type0, type1);
        assertNotEquals(type0, type2);
        assertNotEquals(type0, type3);

        assertThat(type0, instanceOf(StringTypeDefinition.class));
        assertThat(type1, instanceOf(StringTypeDefinition.class));
        assertThat(type2, instanceOf(StringTypeDefinition.class));
        assertThat(type3, instanceOf(StringTypeDefinition.class));

        var stringType0 = (StringTypeDefinition) type0;
        var stringType1 = (StringTypeDefinition) type1;
        var stringType2 = (StringTypeDefinition) type2;
        var stringType3 = (StringTypeDefinition) type3;

        final var patternConstraints0 = stringType0.getPatternConstraints();
        final var patternConstraints1 = stringType1.getPatternConstraints();
        final var patternConstraints2 = stringType2.getPatternConstraints();
        final var patternConstraints3 = stringType3.getPatternConstraints();

        assertEquals(1, patternConstraints0.size());
        assertEquals(1, patternConstraints1.size());
        assertEquals(1, patternConstraints2.size());
        assertEquals(1, patternConstraints3.size());

        final var patternConstraint0 = patternConstraints0.get(0);
        final var patternConstraint1 = patternConstraints1.get(0);
        final var patternConstraint2 = patternConstraints2.get(0);
        final var patternConstraint3 = patternConstraints3.get(0);

        assertEquals("^(?:dp[0-9]+o[0-9]+(d[0-9]+)?)$", patternConstraint0.getJavaPatternString());
        assertEquals("^(?:dp[0-9]+s[0-9]+(f[0-9]+)?(d[0-9]+)?)$", patternConstraint1.getJavaPatternString());
        assertEquals("^(?:dp[0-9]+(P[0-9]+)?p[0-9]{1,3}s[0-9]{1,3}(f[0-9]+)?(d[0-9]+)?)$",
                patternConstraint2.getJavaPatternString());
        assertEquals("^(?:dp[0-9]+p[0-9]+p[0-9]+)$", patternConstraint3.getJavaPatternString());
    }
}
