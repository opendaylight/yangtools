/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6870Test extends AbstractYangTest {
    @Test
    void valid11Test() {
        final var context = assertEffectiveModel("/rfc7950/bug6870/foo.yang");
        assertModifier(context, ModifierKind.INVERT_MATCH, QName.create("foo", "root"), QName.create("foo", "my-leaf"));
        assertModifier(context, null, QName.create("foo", "root"), QName.create("foo", "my-leaf-2"));
    }

    private static void assertModifier(final EffectiveModelContext schemaContext,
            final ModifierKind expectedModifierKind, final QName... qnames) {
        final DataSchemaNode findNode = schemaContext.findDataTreeChild(qnames).orElseThrow();
        assertThat(findNode, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode myLeaf = (LeafSchemaNode) findNode;

        final TypeDefinition<? extends TypeDefinition<?>> type = myLeaf.getType();
        assertThat(type, instanceOf(StringTypeDefinition.class));
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        assertEquals(Optional.ofNullable(expectedModifierKind), patternConstraints.iterator().next().getModifier());
    }

    @Test
    void invalid11Test() {
        assertSourceException(startsWith("'Invert-match' is not valid argument of modifier statement"),
            "/rfc7950/bug6870/invalid11.yang");
    }

    @Test
    void invalid10Test() {
        assertSourceException(startsWith("modifier is not a YANG statement or use of extension"),
            "/rfc7950/bug6870/invalid10.yang");
    }
}
