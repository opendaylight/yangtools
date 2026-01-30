/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

class EffectiveIdentityTest {
    @Test
    void cyclicDefineTest() {
        final var cause = assertThrows(SomeModifiersUnresolvedException.class, () ->
            RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/stmt-test/identity/cyclic-identity-test.yang"))
                .buildEffective())
            .getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage()).startsWith("Yang model processing phase STATEMENT_DEFINITION failed [at ");

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

        assertThat(causes.get(0).getMessage())
            .startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-1 and base identity "
                + "(cyclic.identity.test)child-identity-2 [at ");
        assertThat(causes.get(1).getMessage())
            .startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-2 and base identity "
                + "(cyclic.identity.test)child-identity-3 [at ");
        assertThat(causes.get(2).getMessage())
            .startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-3 and base identity "
                + "(cyclic.identity.test)child-identity-4 [at ");
        assertThat(causes.get(3).getMessage())
            .startsWith("Unable to resolve identity (cyclic.identity.test)child-identity-4 and base identity "
                + "(cyclic.identity.test)child-identity-1 [at ");
    }

    @Test
    void identityTest() throws Exception {
        final var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/stmt-test/identity/identity-test.yang"))
            .buildEffective();
        assertNotNull(result);

        final var module = result.findModule("identity-test").orElseThrow();
        var identities = module.getIdentities();

        assertNotNull(identities);
        assertEquals(4, identities.size());

        IdentitySchemaNode root = null;
        IdentitySchemaNode child1 = null;
        IdentitySchemaNode child2 = null;
        IdentitySchemaNode child12 = null;
        for (var identitySchemaNode : identities) {
            switch (identitySchemaNode.getQName().getLocalName()) {
                case "root-identity" ->
                    root = identitySchemaNode;
                case "child-identity-1" ->
                    child1 = identitySchemaNode;
                case "child-identity-2" ->
                    child2 = identitySchemaNode;
                case "child-identity-1-2" ->
                    child12 = identitySchemaNode;
                default -> {
                    // No-op
                }
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
