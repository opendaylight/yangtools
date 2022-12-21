/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class EffectiveIdentityTest {
    private static final StatementStreamSource IDENTITY_TEST = sourceForResource(
        "/stmt-test/identity/identity-test.yang");
    private static final StatementStreamSource CYCLIC_IDENTITY_TEST = sourceForResource(
        "/stmt-test/identity/cyclic-identity-test.yang");

    @Test
    void cyclicDefineTest() {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(CYCLIC_IDENTITY_TEST);
        final var cause = assertThrows(SomeModifiersUnresolvedException.class, reactor::buildEffective).getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Yang model processing phase STATEMENT_DEFINITION failed [at "));

        // This is a bit complicated, as the order of exceptions may differ
        final var causes = new ArrayList<Throwable>();
        final var cause1 = cause.getCause();
        if (cause1 != null) {
            causes.add(cause1);
        }
        causes.addAll(Arrays.asList(cause.getSuppressed()));
        causes.sort(Comparator.comparing(Throwable::getMessage));
        assertEquals(4, causes.size());
        causes.forEach(throwable -> assertInstanceOf(InferenceException.class, throwable));

        assertThat(causes.get(0).getMessage(),
            startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-1 and base identity "
                + "(cyclic.identity.test)child-identity-2 [at "));
        assertThat(causes.get(1).getMessage(),
            startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-2 and base identity "
                + "(cyclic.identity.test)child-identity-3 [at "));
        assertThat(causes.get(2).getMessage(),
            startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-3 and base identity "
                + "(cyclic.identity.test)child-identity-4 [at "));
        assertThat(causes.get(3).getMessage(),
            startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-4 and base identity "
                + "(cyclic.identity.test)child-identity-1 [at "));
    }

    @Test
    void identityTest() throws ReactorException {
        SchemaContext result = RFC7950Reactors.defaultReactor().newBuild().addSources(IDENTITY_TEST).buildEffective();
        assertNotNull(result);

        Module module = result.findModule("identity-test").orElseThrow();
        var identities = module.getIdentities();

        assertNotNull(identities);
        assertEquals(4, identities.size());

        IdentitySchemaNode root = null;
        IdentitySchemaNode child1 = null;
        IdentitySchemaNode child2 = null;
        IdentitySchemaNode child12 = null;
        for (IdentitySchemaNode identitySchemaNode : identities) {
            switch (identitySchemaNode.getQName().getLocalName()) {
                case "root-identity":
                    root = identitySchemaNode;
                    break;
                case "child-identity-1":
                    child1 = identitySchemaNode;
                    break;
                case "child-identity-2":
                    child2 = identitySchemaNode;
                    break;
                case "child-identity-1-2":
                    child12 = identitySchemaNode;
                    break;
                default:
                    break;
            }
        }

        assertNotNull(root);
        assertNotNull(child1);
        assertNotNull(child2);
        assertNotNull(child12);

        assertTrue(root.getBaseIdentities().isEmpty());

        var rootDerivedIdentities = result.getDerivedIdentities(root);
        assertEquals(2, rootDerivedIdentities.size());

        assertTrue(rootDerivedIdentities.contains(child1));
        assertTrue(rootDerivedIdentities.contains(child2));
        assertFalse(rootDerivedIdentities.contains(child12));
        assertNotEquals(child1, child2);

        assertSame(root, Iterables.getOnlyElement(child1.getBaseIdentities()));
        assertSame(root, Iterables.getOnlyElement(child2.getBaseIdentities()));

        assertEquals(0, result.getDerivedIdentities(child2).size());

        var child1DerivedIdentities = result.getDerivedIdentities(child1);
        assertEquals(1, child1DerivedIdentities.size());
        assertTrue(child1DerivedIdentities.contains(child12));
        assertFalse(child1DerivedIdentities.contains(child1));

        assertSame(child1, Iterables.getOnlyElement(child12.getBaseIdentities()));
        assertSame(child12, child1DerivedIdentities.iterator().next());
    }
}
