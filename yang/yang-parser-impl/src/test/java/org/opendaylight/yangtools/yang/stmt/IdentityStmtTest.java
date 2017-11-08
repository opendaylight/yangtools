/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class IdentityStmtTest {

    private static final StatementStreamSource ILLEGAL_IDENTITY_MODULE = sourceForResource(
        "/identity/identitytest.yang");
    private static final StatementStreamSource ILLEGAL_IDENTITY_MODULE2 = sourceForResource(
        "/identity/prefixidentitytest.yang");
    private static final StatementStreamSource LEGAL_IDENTITY_MODULE = sourceForResource(
        "/identity/import/dummy.yang");
    private static final StatementStreamSource LEGAL_IDENTITY_MODULE2 = sourceForResource(
        "/identity/import/prefiximportidentitytest.yang");
    private static final StatementStreamSource ILLEGAL_IDENTITY_MODULE3 = sourceForResource(
        "/identity/illegal-chained-identity-test.yang");
    private static final StatementStreamSource LEGAL_IDENTITY_MODULE3 = sourceForResource(
        "/identity/legal-chained-identity-test.yang");
    private static final StatementStreamSource DUPLICATE_IDENTITY_MODULE = sourceForResource(
        "/identity/duplicate-identity-test.yang");

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityTest() throws ReactorException {
        DefaultReactors.defaultReactor().newBuild().addSource(ILLEGAL_IDENTITY_MODULE).buildEffective();
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityWithPrefixTest() throws ReactorException {
        DefaultReactors.defaultReactor().newBuild().addSource(ILLEGAL_IDENTITY_MODULE2).buildEffective();
    }

    @Test
    public void importedIdentityTest() throws ReactorException {
        SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(LEGAL_IDENTITY_MODULE, LEGAL_IDENTITY_MODULE2)
                .buildEffective();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityThroughChaining() throws ReactorException {
        SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSource(ILLEGAL_IDENTITY_MODULE3)
                .buildEffective();
        assertNotNull(result);
    }

    @Test
    public void chainedIdentityTest() throws ReactorException {
        SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSource(LEGAL_IDENTITY_MODULE3)
                .buildEffective();
        assertNotNull(result);

        Module testModule = result.findModules("legal-chained-identity-test").iterator().next();
        assertNotNull(testModule);

        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(4, identities.size());

        Iterator<IdentitySchemaNode> identitiesIterator = identities.iterator();
        IdentitySchemaNode identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"),
            is("third-identity"), is("fourth-identity")));

        identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"),
            is("third-identity"), is("fourth-identity")));

        identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"),
            is("third-identity"), is("fourth-identity")));

        identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"),
            is("third-identity"), is("fourth-identity")));
    }

    @Test
    public void duplicateIdentityTest() throws ReactorException {
        SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSource(DUPLICATE_IDENTITY_MODULE)
                .buildEffective();
        assertNotNull(result);

        Module testModule = result.findModules("duplicate-identity-test").iterator().next();
        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(1, identities.size());
    }
}
