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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class IdentityStmtTest {

    private static final StatementStreamSource ILLEGAL_IDENTITY_MODULE =
            sourceForResource("/identity/identitytest.yang");

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
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ILLEGAL_IDENTITY_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityWithPrefixTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ILLEGAL_IDENTITY_MODULE2);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void importedIdentityTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(LEGAL_IDENTITY_MODULE, LEGAL_IDENTITY_MODULE2);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityThroughChaining() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ILLEGAL_IDENTITY_MODULE3);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void chainedIdentityTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(LEGAL_IDENTITY_MODULE3);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("legal-chained-identity-test", null);
        assertNotNull(testModule);

        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(4, identities.size());

        Iterator<IdentitySchemaNode> identitiesIterator = identities.iterator();
        IdentitySchemaNode identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"), is
                ("third-identity"), is("fourth-identity")));

        identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"), is
                ("third-identity"), is("fourth-identity")));

        identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"), is
                ("third-identity"), is("fourth-identity")));

        identity = identitiesIterator.next();
        assertThat(identity.getQName().getLocalName(), anyOf(is("first-identity"), is("second-identity"), is
                ("third-identity"), is("fourth-identity")));
    }

    @Test
    public void duplicateIdentityTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(DUPLICATE_IDENTITY_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("duplicate-identity-test", null);
        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(1, identities.size());
    }
}
