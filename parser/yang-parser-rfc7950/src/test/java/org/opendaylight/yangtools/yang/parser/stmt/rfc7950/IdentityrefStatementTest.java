/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class IdentityrefStatementTest extends AbstractYangTest {

    @Test
    void testIdentityrefWithMultipleBaseIdentities() {
        final var context = assertEffectiveModel("/rfc7950/identityref-stmt/foo.yang");

        final var foo = context.findModule("foo", Revision.of("2017-01-11")).orElseThrow();
        final var identities = foo.getIdentities();
        assertEquals(3, identities.size());

        final var idrefLeaf = assertInstanceOf(LeafSchemaNode.class,
            foo.getDataChildByName(QName.create(foo.getQNameModule(), "idref-leaf")));
        final var idrefType = assertInstanceOf(IdentityrefTypeDefinition.class, idrefLeaf.getType());
        final var referencedIdentities = idrefType.getIdentities();
        assertEquals(Set.copyOf(identities), referencedIdentities);
        assertEquals("id-a", idrefType.getIdentities().iterator().next().getQName().getLocalName());
    }

    @Test
    void testInvalidYang10() {
        assertThat(assertInvalidSubstatementException("/rfc7950/identityref-stmt/foo10.yang").getMessage())
            .startsWith("statement type allows at most 1 base substatement: 3 present [at ");
    }
}
