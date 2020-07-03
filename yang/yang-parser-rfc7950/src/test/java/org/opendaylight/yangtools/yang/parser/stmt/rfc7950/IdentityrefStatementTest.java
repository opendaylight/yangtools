/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class IdentityrefStatementTest {

    @Test
    public void testIdentityrefWithMultipleBaseIdentities() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/identityref-stmt/foo.yang");
        assertNotNull(schemaContext);

        final Module foo = schemaContext.findModule("foo", Revision.of("2017-01-11")).get();
        final Collection<? extends IdentitySchemaNode> identities = foo.getIdentities();
        assertEquals(3, identities.size());

        final LeafSchemaNode idrefLeaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "idref-leaf"));
        assertNotNull(idrefLeaf);

        final IdentityrefTypeDefinition idrefType = (IdentityrefTypeDefinition) idrefLeaf.getType();
        final Set<? extends IdentitySchemaNode> referencedIdentities = idrefType.getIdentities();
        assertEquals(3, referencedIdentities.size());
        assertThat(referencedIdentities, containsInAnyOrder(identities.toArray()));
        assertEquals("id-a", idrefType.getIdentities().iterator().next().getQName().getLocalName());
    }

    @Test
    public void testInvalidYang10() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/identityref-stmt/foo10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Maximal count of BASE for TYPE is 1, detected 3."));
        }
    }
}
