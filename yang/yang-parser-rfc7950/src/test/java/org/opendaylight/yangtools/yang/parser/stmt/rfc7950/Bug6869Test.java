/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6869Test {
    private static final QName ROOT = QName.create("foo", "root");
    private static final QName GRP_LEAF = QName.create("foo", "grp-leaf");

    @Test
    public void identityNoFeaureTest() throws Exception {
        final EffectiveModelContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6869/foo.yang",
                ImmutableSet.of());
        assertNotNull(schemaContext);

        final Collection<? extends IdentitySchemaNode> identities = getIdentities(schemaContext);
        assertEquals(0, identities.size());

        final DataSchemaNode findNode = schemaContext.findDataTreeChild(ROOT, GRP_LEAF).orElse(null);
        assertThat(findNode, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode grpLeaf = (LeafSchemaNode) findNode;
        assertFalse(grpLeaf.isMandatory());
    }

    @Test
    public void identityAllFeauresTest() throws Exception {
        final EffectiveModelContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6869/foo.yang",
                createFeaturesSet("identity-feature", "mandatory-leaf", "tls", "ssh", "two", "three"));
        assertNotNull(schemaContext);

        final Collection<? extends IdentitySchemaNode> identities = getIdentities(schemaContext);
        assertEquals(1, identities.size());

        final DataSchemaNode findNode = schemaContext.findDataTreeChild(ROOT, GRP_LEAF).orElse(null);
        assertThat(findNode, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode grpLeaf = (LeafSchemaNode) findNode;
        assertTrue(grpLeaf.isMandatory());
    }

    private static Collection<? extends IdentitySchemaNode> getIdentities(final EffectiveModelContext schemaContext) {
        final Collection<? extends Module> modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module module = modules.iterator().next();
        return module.getIdentities();
    }

    private static Set<QName> createFeaturesSet(final String... featureNames) {
        final Set<QName> supportedFeatures = new HashSet<>();
        for (final String featureName : featureNames) {
            supportedFeatures.add(QName.create("foo", featureName));
        }

        return ImmutableSet.copyOf(supportedFeatures);
    }

    @Test
    public void invalidYang10Test() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/rfc7950/bug6869/invalid10.yang"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("IF_FEATURE is not valid for IDENTITY"));
    }
}