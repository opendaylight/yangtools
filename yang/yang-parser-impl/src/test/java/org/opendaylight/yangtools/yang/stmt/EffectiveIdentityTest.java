/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.junit.Test;

public class EffectiveIdentityTest {

    private static final YangStatementSourceImpl IDENTITY_TEST = new YangStatementSourceImpl(
            "/stmt-test/identity/identity-test.yang", false);

    private static final YangStatementSourceImpl CYCLIC_IDENTITY_TEST = new YangStatementSourceImpl(
            "/stmt-test/identity/cyclic-identity-test.yang", false);

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void cyclicefineTest() throws SourceException, ReactorException,
            URISyntaxException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        StmtTestUtils.addSources(reactor, CYCLIC_IDENTITY_TEST);
        try {
            EffectiveSchemaContext result = reactor.buildEffective();
        } catch (SomeModifiersUnresolvedException e) {
            StmtTestUtils.log(e, "      ");
            throw e;
        }
    }

    @Test
    public void identityTest() throws SourceException, ReactorException,
            URISyntaxException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        StmtTestUtils.addSources(reactor, IDENTITY_TEST);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module module = result.findModuleByName("identity-test",
                SimpleDateFormatUtil.DEFAULT_DATE_REV);

        assertNotNull(module);

        Set<IdentitySchemaNode> identities = module.getIdentities();

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

        Set<IdentitySchemaNode> rootDerivedIdentities = root
                .getDerivedIdentities();
        assertEquals(2, rootDerivedIdentities.size());

        assertTrue(rootDerivedIdentities.contains(child1));
        assertTrue(rootDerivedIdentities.contains(child2));
        assertFalse(rootDerivedIdentities.contains(child12));
        assertFalse(child1.equals(child2));

        assertTrue(root == child1.getBaseIdentities().iterator().next());
        assertTrue(root == child2.getBaseIdentities().iterator().next());

        assertTrue(child2.getDerivedIdentities().isEmpty());

        Set<IdentitySchemaNode> child1DerivedIdentities = child1
                .getDerivedIdentities();
        assertEquals(1, child1DerivedIdentities.size());
        assertTrue(child1DerivedIdentities.contains(child12));
        assertFalse(child1DerivedIdentities.contains(child1));

        assertTrue(child1 == child12.getBaseIdentities().iterator().next());
        assertTrue(child12 == child1DerivedIdentities.iterator().next());
    }

}
