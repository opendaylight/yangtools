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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class EffectiveSchemaContextTest {

    @Test
    public void testEffectiveSchemaContext() throws ReactorException, ParseException, URISyntaxException, IOException,
            YangSyntaxErrorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        final YangStatementStreamSource source1 = YangStatementStreamSource.create(YangTextSchemaSource.forResource(
            "/effective-schema-context-test/foo.yang"));
        final YangStatementStreamSource source2 = YangStatementStreamSource.create(YangTextSchemaSource.forResource(
            "/effective-schema-context-test/bar.yang"));
        final YangStatementStreamSource source3 = YangStatementStreamSource.create(YangTextSchemaSource.forResource(
            "/effective-schema-context-test/baz.yang"));

        reactor.addSources(source1, source2, source3);
        final SchemaContext schemaContext = reactor.buildEffective();
        assertNotNull(schemaContext);

        final Set<DataSchemaNode> dataDefinitions = schemaContext.getDataDefinitions();
        assertEquals(3, dataDefinitions.size());

        final Collection<DataSchemaNode> childNodes = schemaContext.getChildNodes();
        assertEquals(3, childNodes.size());

        final Set<NotificationDefinition> notifications = schemaContext.getNotifications();
        assertEquals(3, notifications.size());

        final Set<RpcDefinition> rpcs = schemaContext.getOperations();
        assertEquals(3, rpcs.size());

        final Set<ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(3, extensions.size());

        final List<UnknownSchemaNode> unknownSchemaNodes = schemaContext.getUnknownSchemaNodes();
        assertEquals(3, unknownSchemaNodes.size());

        assertNull(schemaContext.getDataChildByName(QName.create("foo-namespace", "2016-09-21", "foo-cont")));

        assertNull(schemaContext.findModuleByName("foo", SimpleDateFormatUtil.getRevisionFormat().parse("2016-08-21")));
        assertNull(schemaContext.findModuleByNamespaceAndRevision(
                null, SimpleDateFormatUtil.getRevisionFormat().parse("2016-09-21")));
        assertNull(schemaContext.findModuleByNamespaceAndRevision(
                URI.create("foo-namespace"), SimpleDateFormatUtil.getRevisionFormat().parse("2016-08-21")));

        assertFalse(schemaContext.isAugmenting());
        assertFalse(schemaContext.isAddedByUses());
        assertFalse(schemaContext.isConfiguration());
        assertFalse(schemaContext.isPresenceContainer());
        assertNull(schemaContext.getConstraints());
        assertNull(schemaContext.getDescription());
        assertNull(schemaContext.getReference());
        assertEquals(SchemaContext.NAME, schemaContext.getQName());
        assertEquals(SchemaPath.ROOT, schemaContext.getPath());
        assertEquals(Status.CURRENT, schemaContext.getStatus());
        assertNotNull(schemaContext.getUses());
        assertTrue(schemaContext.getUses().isEmpty());
        assertNotNull(schemaContext.getAvailableAugmentations());
        assertTrue(schemaContext.getAvailableAugmentations().isEmpty());

        Module fooModule = schemaContext.findModuleByName(
                "foo", SimpleDateFormatUtil.getRevisionFormat().parse("2016-09-21"));
        assertFalse(schemaContext.getModuleSource(fooModule).isPresent());

        assertEquals(3, schemaContext.getAllModuleIdentifiers().size());
        assertEquals(3, ((EffectiveSchemaContext) schemaContext).getRootDeclaredStatements().size());
        assertEquals(3,((EffectiveSchemaContext) schemaContext).getRootEffectiveStatements().size());

        final Set<Module> modules = schemaContext.getModules();
        final SchemaContext copiedSchemaContext = EffectiveSchemaContext.resolveSchemaContext(modules);
        assertNotNull(copiedSchemaContext);
        assertEquals(modules, copiedSchemaContext.getModules());
    }
}
