/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class IdentityStmtTest {

    private static final YangStatementSourceImpl ILLEGAL_IDENTITY_MODULE = new YangStatementSourceImpl
            ("/identity/identitytest.yang", false);
    private static final YangStatementSourceImpl ILLEGAL_IDENTITY_MODULE2 = new YangStatementSourceImpl
            ("/identity/prefixidentitytest.yang", false);
    private static final YangStatementSourceImpl LEGAL_IDENTITY_MODULE = new YangStatementSourceImpl
            ("/identity/import/dummy.yang", false);
    private static final YangStatementSourceImpl LEGAL_IDENTITY_MODULE2 = new YangStatementSourceImpl
            ("/identity/import/prefiximportidentitytest.yang", false);
    private static final YangStatementSourceImpl ILLEGAL_IDENTITY_MODULE3 = new YangStatementSourceImpl
            ("/identity/illegal-chained-identity-test.yang", false);
    private static final YangStatementSourceImpl LEGAL_IDENTITY_MODULE3 = new YangStatementSourceImpl
            ("/identity/legal-chained-identity-test.yang", false);
    private static final YangStatementSourceImpl DUPLICATE_IDENTITY_MODULE = new YangStatementSourceImpl
            ("/identity/duplicate-identity-test.yang", false);

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ILLEGAL_IDENTITY_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityWithPrefixTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ILLEGAL_IDENTITY_MODULE2);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void importedIdentityTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, LEGAL_IDENTITY_MODULE, LEGAL_IDENTITY_MODULE2);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void selfReferencingIdentityThroughChaining() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ILLEGAL_IDENTITY_MODULE3);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void chainedIdentityTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, LEGAL_IDENTITY_MODULE3);

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
        StmtTestUtils.addSources(reactor, DUPLICATE_IDENTITY_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("duplicate-identity-test", null);
        Set<IdentitySchemaNode> identities = testModule.getIdentities();
        assertEquals(1, identities.size());
    }
}
