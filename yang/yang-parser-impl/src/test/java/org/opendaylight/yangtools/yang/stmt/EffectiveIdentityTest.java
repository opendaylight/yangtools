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
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Iterables;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class EffectiveIdentityTest {

    private static final StatementStreamSource IDENTITY_TEST = sourceForResource(
            "/stmt-test/identity/identity-test.yang");

    private static final StatementStreamSource CYCLIC_IDENTITY_TEST = sourceForResource(
            "/stmt-test/identity/cyclic-identity-test.yang");

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void cyclicefineTest() throws SourceException, ReactorException,
            URISyntaxException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSources(CYCLIC_IDENTITY_TEST);
        try {
            reactor.buildEffective();
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
        reactor.addSources(IDENTITY_TEST);
        SchemaContext result = reactor.buildEffective();

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

        assertTrue(root == Iterables.getOnlyElement(child1.getBaseIdentities()));
        assertTrue(root == Iterables.getOnlyElement(child2.getBaseIdentities()));

        assertTrue(child2.getDerivedIdentities().isEmpty());

        Set<IdentitySchemaNode> child1DerivedIdentities = child1
                .getDerivedIdentities();
        assertEquals(1, child1DerivedIdentities.size());
        assertTrue(child1DerivedIdentities.contains(child12));
        assertFalse(child1DerivedIdentities.contains(child1));

        assertTrue(child1 == Iterables.getOnlyElement(child12.getBaseIdentities()));
        assertTrue(child12 == child1DerivedIdentities.iterator().next());
    }
}
