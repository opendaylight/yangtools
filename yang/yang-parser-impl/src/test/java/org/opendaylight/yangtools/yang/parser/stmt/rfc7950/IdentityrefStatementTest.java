/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class IdentityrefStatementTest {

    @Test
    public void testIdentityrefWithMultipleBaseIdentities() throws ReactorException, FileNotFoundException,
            URISyntaxException, ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/identityref-stmt/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-01-11");

        final Module foo = schemaContext.findModuleByName("foo", revision);
        assertNotNull(foo);

        final Set<IdentitySchemaNode> identities = foo.getIdentities();
        assertEquals(3, identities.size());

        final LeafSchemaNode idrefLeaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "idref-leaf"));
        assertNotNull(idrefLeaf);

        final IdentityrefTypeDefinition idrefType = (IdentityrefTypeDefinition) idrefLeaf.getType();
        final Set<IdentitySchemaNode> referencedIdentities = idrefType.getIdentities();
        assertEquals(3, referencedIdentities.size());
        assertEquals(identities, referencedIdentities);
        assertEquals("id-a", idrefType.getIdentity().getQName().getLocalName());
    }

    @Test
    public void testInvalidYang10() throws ReactorException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/identityref-stmt/foo10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Maximal count of BASE for TYPE is 1, detected 3."));
        }
    }
}
