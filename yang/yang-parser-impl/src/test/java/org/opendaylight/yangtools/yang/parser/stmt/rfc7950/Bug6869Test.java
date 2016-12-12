/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6869Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void identityNoFeaureTest() throws ReactorException, SourceException, FileNotFoundException,
            URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6869/foo.yang",
                createIfFeaturesPredicate("no-feature"));
        assertNotNull(schemaContext);

        final Set<IdentitySchemaNode> identities = getIdentities(schemaContext);
        assertEquals(0, identities.size());

        final SchemaNode findNode = findNode(schemaContext, ImmutableList.of("root", "grp-leaf"));
        assertTrue(findNode instanceof LeafSchemaNode);
        final LeafSchemaNode grpLeaf = (LeafSchemaNode) findNode;
        assertFalse(grpLeaf.getConstraints().isMandatory());
    }

    @Test
    public void identityAllFeauresTest() throws ReactorException, SourceException, FileNotFoundException,
            URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6869/foo.yang",
                createIfFeaturesPredicate("identity-feature", "mandatory-leaf", "tls", "ssh", "two", "three"));
        assertNotNull(schemaContext);

        final Set<IdentitySchemaNode> identities = getIdentities(schemaContext);
        assertEquals(1, identities.size());

        final SchemaNode findNode = findNode(schemaContext, ImmutableList.of("root", "grp-leaf"));
        assertTrue(findNode instanceof LeafSchemaNode);
        final LeafSchemaNode grpLeaf = (LeafSchemaNode) findNode;
        assertTrue(grpLeaf.getConstraints().isMandatory());
    }

    private static Set<IdentitySchemaNode> getIdentities(final SchemaContext schemaContext) {
        final Set<Module> modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module module = modules.iterator().next();
        return module.getIdentities();
    }

    private static Predicate<QName> createIfFeaturesPredicate(final String... featureNames) {
        final Predicate<QName> ifFeaturesPredicate = qName -> {
            final Set<QName> supportedFeatures = new HashSet<>();
            for (final String featureName : featureNames) {
                supportedFeatures.add(QName.create(FOO_NS, FOO_REV, featureName));
            }
            return supportedFeatures.contains(qName);
        };
        return ifFeaturesPredicate;
    }

    private static SchemaNode findNode(final SchemaContext context, final Iterable<String> localNamesPath) {
        final Iterable<QName> qNames = Iterables.transform(localNamesPath,
                localName -> QName.create(FOO_NS, FOO_REV, localName));
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(qNames, true));
    }

    @Test
    public void invalidYang10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6869/invalid10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("IF_FEATURE is not valid for IDENTITY"));
        }
    }
}