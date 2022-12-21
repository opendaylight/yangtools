/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class Bug6869Test extends AbstractYangTest {
    private static final QName ROOT = QName.create("foo", "root");
    private static final QName GRP_LEAF = QName.create("foo", "grp-leaf");

    @Test
    void identityNoFeaureTest() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6869/foo.yang", Set.of());
        assertNotNull(schemaContext);

        final var identities = getIdentities(schemaContext);
        assertEquals(0, identities.size());

        final LeafSchemaNode grpLeaf = assertInstanceOf(LeafSchemaNode.class,
            schemaContext.findDataTreeChild(ROOT, GRP_LEAF).orElseThrow());
        assertFalse(grpLeaf.isMandatory());
    }

    @Test
    void identityAllFeauresTest() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6869/foo.yang",
            createFeaturesSet("identity-feature", "mandatory-leaf", "tls", "ssh", "two", "three"));
        assertNotNull(schemaContext);

        final var identities = getIdentities(schemaContext);
        assertEquals(1, identities.size());

        final LeafSchemaNode grpLeaf = assertInstanceOf(LeafSchemaNode.class,
            schemaContext.findDataTreeChild(ROOT, GRP_LEAF).orElseThrow());
        assertTrue(grpLeaf.isMandatory());
    }

    private static Collection<? extends IdentitySchemaNode> getIdentities(final EffectiveModelContext schemaContext) {
        final var modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module module = modules.iterator().next();
        return module.getIdentities();
    }

    private static Set<QName> createFeaturesSet(final String... featureNames) {
        return Arrays.stream(featureNames)
            .map(featureName -> QName.create("foo", featureName))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Test
    void invalidYang10Test() {
        assertInvalidSubstatementException(startsWith("IF_FEATURE is not valid for IDENTITY"),
            "/rfc7950/bug6869/invalid10.yang");
    }
}
