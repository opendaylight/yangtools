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
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class IdentityStmtTest {

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

    @Test
    void selfReferencingIdentityTest() throws ReactorException {
        assertThrows(SomeModifiersUnresolvedException.class, () -> {
            RFC7950Reactors.defaultReactor().newBuild().addSource(ILLEGAL_IDENTITY_MODULE).buildEffective();
        });
    }

    @Test
    void selfReferencingIdentityWithPrefixTest() throws ReactorException {
        assertThrows(SomeModifiersUnresolvedException.class, () -> {
            RFC7950Reactors.defaultReactor().newBuild().addSource(ILLEGAL_IDENTITY_MODULE2).buildEffective();
        });
    }

    @Test
    void importedIdentityTest() throws ReactorException {
        SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(LEGAL_IDENTITY_MODULE, LEGAL_IDENTITY_MODULE2)
            .buildEffective();
        assertNotNull(result);
    }

    @Test
    void selfReferencingIdentityThroughChaining() throws ReactorException {
        assertThrows(SomeModifiersUnresolvedException.class, () -> {
            SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(ILLEGAL_IDENTITY_MODULE3)
                .buildEffective();
            assertNotNull(result);
        });
    }

    @Test
    void chainedIdentityTest() throws ReactorException {
        SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(LEGAL_IDENTITY_MODULE3)
            .buildEffective();
        assertNotNull(result);

        Module testModule = result.findModules("legal-chained-identity-test").iterator().next();
        assertNotNull(testModule);

        var identities = testModule.getIdentities();
        assertEquals(4, identities.size());

        var identitiesIterator = identities.iterator();
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
    void duplicateIdentityTest() throws ReactorException {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(DUPLICATE_IDENTITY_MODULE);
        final var cause = assertThrows(SomeModifiersUnresolvedException.class, reactor::buildEffective).getCause();
        assertInstanceOf(SourceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Duplicate identity definition "));
    }
}
