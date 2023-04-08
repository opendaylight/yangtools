/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

public class IdentityrefStatementTest extends AbstractYangTest {

    @Test
    public void testIdentityrefWithMultipleBaseIdentities() {
        final var context = assertEffectiveModel("/rfc7950/identityref-stmt/foo.yang");

        final var foo = context.findModule("foo", Revision.of("2017-01-11")).orElseThrow();
        final var identities = foo.getIdentities();
        assertEquals(3, identities.size());

        final var idrefLeaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "idref-leaf"));
        final var idrefType = (IdentityrefTypeDefinition) idrefLeaf.getType();
        final var referencedIdentities = idrefType.getIdentities();
        assertEquals(3, referencedIdentities.size());
        assertThat(referencedIdentities, containsInAnyOrder(identities.toArray()));
        assertEquals("id-a", idrefType.getIdentities().iterator().next().getQName().getLocalName());
    }

    @Test
    public void testInvalidYang10() {
        assertInvalidSubstatementException(startsWith("Maximal count of BASE for TYPE is 1, detected 3."),
                "/rfc7950/identityref-stmt/foo10.yang");
    }
}
