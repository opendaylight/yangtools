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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

class IdentityStmtTest extends AbstractYangTest {
    @Test
    void selfReferencingIdentityTest() {
        assertInferenceException(
            startsWith("Unable to resolve identity (urn:test.identitytest?revision=2014-09-17)test and base identity "
                + "(urn:test.identitytest?revision=2014-09-17)test [at "),
            "/identity/identitytest.yang");
    }

    @Test
    void selfReferencingIdentityWithPrefixTest() {
        assertInferenceException(
            startsWith("Unable to resolve identity (urn:test.prefixidentitytest?revision=2014-09-24)prefixtest and "
                + "base identity (urn:test.prefixidentitytest?revision=2014-09-24)prefixtest [at "),
            "/identity/prefixidentitytest.yang");
    }

    @Test
    void importedIdentityTest() {
        assertEffectiveModel("/identity/import/dummy.yang", "/identity/import/prefiximportidentitytest.yang");
    }

    @Test
    void selfReferencingIdentityThroughChaining() {
        assertInferenceException(
            startsWith("Yang model processing phase STATEMENT_DEFINITION failed [at "),
            "/identity/illegal-chained-identity-test.yang");
    }

    @Test
    void chainedIdentityTest() {
        final var result = assertEffectiveModel("/identity/legal-chained-identity-test.yang");

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
    void duplicateIdentityTest() {
        assertSourceException(startsWith("Duplicate identity definition "), "/identity/duplicate-identity-test.yang");
    }
}
