/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6870Test extends AbstractYangTest {
    @Test
    void valid11Test() {
        final var context = assertEffectiveModel("/rfc7950/bug6870/foo.yang");
        assertModifier(context, ModifierKind.INVERT_MATCH, QName.create("foo", "root"), QName.create("foo", "my-leaf"));
        assertModifier(context, null, QName.create("foo", "root"), QName.create("foo", "my-leaf-2"));
    }

    private static void assertModifier(final EffectiveModelContext modelContext,
            final ModifierKind expectedModifierKind, final QName... qnames) {
        final DataSchemaNode findNode = modelContext.findDataTreeChild(qnames).orElseThrow();
        final LeafSchemaNode myLeaf = assertInstanceOf(LeafSchemaNode.class, findNode);

        final var type = myLeaf.typeDefinition();
        final var patternConstraints = assertInstanceOf(StringTypeDefinition.class, type).getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        assertEquals(Optional.ofNullable(expectedModifierKind), patternConstraints.iterator().next().getModifier());
    }

    @Test
    void invalid11Test() {
        assertSourceExceptionMessage("/rfc7950/bug6870/invalid11.yang")
            .startsWith("'Invert-match' is not valid argument of modifier statement");
    }

    @Test
    void invalid10Test() {
        assertSourceExceptionMessage("/rfc7950/bug6870/invalid10.yang")
            .startsWith("modifier is not a YANG statement or use of extension");
    }
}
