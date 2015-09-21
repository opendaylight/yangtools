/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.it.yang.runtime.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleContext;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class MultipleRevisionsSupportTest {

    public static final YangModuleInfo TOPOLOGY_OLD_MODULE = org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev130712.$YangModuleInfoImpl
            .getInstance();
    public static final YangModuleInfo TOPOLOGY_NEW_MODULE = org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.$YangModuleInfoImpl
            .getInstance();

    public static final Set<YangModuleInfo> DEPENDENCIES = ImmutableSet.<YangModuleInfo> builder() //
            .addAll(TOPOLOGY_OLD_MODULE.getImportedModules()) //
            .addAll(TOPOLOGY_NEW_MODULE.getImportedModules()).build();

    @Test
    public void dependenciesOlderNewer() throws Exception {
        List<InputStream> streams = ImmutableList.<InputStream> builder()//
                .addAll(toInputStreams(DEPENDENCIES)) //
                .add(TOPOLOGY_OLD_MODULE.getModuleSourceStream()) //
                .add(TOPOLOGY_NEW_MODULE.getModuleSourceStream()) //
                .build();
        SchemaContext schemaContext = contextVerified(streams);
        verifySchemaDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
        verifyBindingDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
    }

    @Test
    public void dependenciesNewerOlder() throws Exception {
        List<InputStream> streams = ImmutableList.<InputStream> builder()//
                .addAll(toInputStreams(DEPENDENCIES)) //
                .add(TOPOLOGY_NEW_MODULE.getModuleSourceStream()) //
                .add(TOPOLOGY_OLD_MODULE.getModuleSourceStream()) //
                .build();
        SchemaContext schemaContext = contextVerified(streams);
        verifySchemaDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
        verifyBindingDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
    }

    @Test
    public void newerOlderDependencies() throws Exception {
        List<InputStream> streams = ImmutableList.<InputStream> builder()//
                .add(TOPOLOGY_NEW_MODULE.getModuleSourceStream()) //
                .add(TOPOLOGY_OLD_MODULE.getModuleSourceStream()) //
                .addAll(toInputStreams(DEPENDENCIES)) //
                .build();
        SchemaContext schemaContext = contextVerified(streams);
        verifySchemaDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
        verifyBindingDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
    }

    @Test
    public void newerDependenciesOlder() throws Exception {
        List<InputStream> streams = ImmutableList.<InputStream> builder()//
                .add(TOPOLOGY_NEW_MODULE.getModuleSourceStream()) //
                .addAll(toInputStreams(DEPENDENCIES)) //
                .add(TOPOLOGY_OLD_MODULE.getModuleSourceStream()) //
                .build();
        SchemaContext schemaContext = contextVerified(streams);
        verifySchemaDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
        verifyBindingDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
    }

    @Test
    public void OlderNewerDependencies() throws Exception {
        List<InputStream> streams = ImmutableList.<InputStream> builder()//
                .add(TOPOLOGY_OLD_MODULE.getModuleSourceStream()) //
                .add(TOPOLOGY_NEW_MODULE.getModuleSourceStream()) //
                .addAll(toInputStreams(DEPENDENCIES)) //
                .build();
        SchemaContext schemaContext = contextVerified(streams);
        verifySchemaDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
        verifyBindingDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
    }

    @Test
    public void olderDependenciesNewer() throws Exception {
        List<InputStream> streams = ImmutableList.<InputStream> builder()//
                .add(TOPOLOGY_OLD_MODULE.getModuleSourceStream()) //
                .add(TOPOLOGY_NEW_MODULE.getModuleSourceStream()) //
                .addAll(toInputStreams(DEPENDENCIES)) //
                .build();
        SchemaContext schemaContext = contextVerified(streams);
        verifySchemaDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
        verifyBindingDifference(schemaContext, TOPOLOGY_OLD_MODULE, TOPOLOGY_NEW_MODULE);
    }

    private SchemaContext contextVerified(final List<InputStream> streams) throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        return reactor.buildEffective(streams);
    }

    private void verifyBindingDifference(final SchemaContext schemaContext, final YangModuleInfo oldModule, final YangModuleInfo newModule) {
        generatedTypesVerified(schemaContext, oldModule, newModule);
    }

    private Map<Module, ModuleContext> generatedTypesVerified(final SchemaContext schemaContext, final YangModuleInfo oldModule,
            final YangModuleInfo newModule) {
        BindingGeneratorImpl generator = new BindingGeneratorImpl();
        generator.generateTypes(schemaContext);
        return generator.getModuleContexts();
    }

    private void verifySchemaDifference(final SchemaContext context, final YangModuleInfo topologyOldModule,
            final YangModuleInfo topologyNewModule) {
        Module oldModel = context.findModuleByNamespaceAndRevision(//
                URI.create(TOPOLOGY_OLD_MODULE.getNamespace()), QName.parseRevision(TOPOLOGY_OLD_MODULE.getRevision()));

        Module newModel = context.findModuleByNamespaceAndRevision(//
                URI.create(TOPOLOGY_NEW_MODULE.getNamespace()), QName.parseRevision(TOPOLOGY_NEW_MODULE.getRevision()));

        SchemaNode oldNode = findSchemaNode(oldModel, "network-topology", "topology", "link");
        SchemaNode newNode = findSchemaNode(newModel, "network-topology", "topology", "link");

        assertNotNull(oldNode);
        assertNotNull(newNode);

        assertDeepRevision(TOPOLOGY_OLD_MODULE.getRevision(), oldNode);
        assertDeepRevision(TOPOLOGY_NEW_MODULE.getRevision(), newNode);
    }

    private static void assertDeepRevision(final String revision, final SchemaNode node) {
        assertEquals("Wrong revision: " + node.getPath(), revision, node.getQName().getFormattedRevision());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                assertDeepRevision(revision, child);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (DataSchemaNode child : ((ChoiceSchemaNode) node).getCases()) {
                assertDeepRevision(revision, child);
            }
        }
    }

    private static final SchemaNode findSchemaNode(final DataNodeContainer container, final String... pathArgs) {
        DataNodeContainer previous = container;

        SchemaNode result = (container instanceof SchemaNode) ? (SchemaNode) container : null;
        for (String arg : pathArgs) {
            if (previous == null) {
                return null;
            }
            for (DataSchemaNode child : previous.getChildNodes()) {
                if (child.getQName().getLocalName().equals(arg)) {
                    if (child instanceof DataNodeContainer) {
                        previous = (DataNodeContainer) child;
                    } else {
                        previous = null;
                    }
                    result = child;
                    break;
                }
            }
        }
        return result;
    }

    private static final Iterable<? extends InputStream> toInputStreams(final Set<YangModuleInfo> moduleInfos)
            throws Exception {
        Builder<InputStream> streams = ImmutableList.<InputStream> builder();
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            streams.add(yangModuleInfo.getModuleSourceStream());
        }
        return streams.build();
    }

}
