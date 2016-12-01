/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class EffectiveModuleTest {

    private static final YangStatementSourceImpl ROOT_MODULE = new YangStatementSourceImpl(
            "/semantic-statement-parser/effective-module/root.yang", false);
    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl(
            "/semantic-statement-parser/effective-module/imported.yang", false);
    private static final YangStatementSourceImpl SUBMODULE = new YangStatementSourceImpl(
            "/semantic-statement-parser/effective-module/submod.yang", false);

    private static final QNameModule ROOT_MODULE_QNAME = QNameModule.create(URI.create("root-ns"),
            SimpleDateFormatUtil.DEFAULT_DATE_REV);

    private static final QName cont = QName.create(ROOT_MODULE_QNAME, "cont");
    private static final QName feature1 = QName.create(ROOT_MODULE_QNAME, "feature1");

    private static final SchemaPath contSchemaPath = SchemaPath.create(true, cont);
    private static final SchemaPath feature1SchemaPath = SchemaPath.create(true, feature1);

    private static Date revision;

    @BeforeClass
    public static void init() {
        try {
            revision = SimpleDateFormatUtil.getRevisionFormat().parse("2000-01-01");
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    public void effectiveBuildTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, ROOT_MODULE, IMPORTED_MODULE, SUBMODULE);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Module rootModule = result.findModuleByName("root", null);
        assertNotNull(rootModule);

        assertEquals("root-pref", rootModule.getPrefix());
        assertEquals(YangVersion.VERSION_1, rootModule.getYangVersion());
        assertEquals("cisco", rootModule.getOrganization());
        assertEquals("cisco email", rootModule.getContact());

        final ContainerSchemaNode contSchemaNode = (ContainerSchemaNode) rootModule.getDataChildByName(cont);
        assertNotNull(contSchemaNode);

        final Set<AugmentationSchema> augmentations = rootModule.getAugmentations();
        assertEquals(1, augmentations.size());
        assertEquals(contSchemaPath, augmentations.iterator().next().getTargetPath());

        final Set<ModuleImport> imports = rootModule.getImports();
        assertEquals(1, imports.size());
        final ModuleImport importStmt = imports.iterator().next();
        assertNotNull(importStmt);
        assertEquals("imported", importStmt.getModuleName());
        assertEquals(revision, importStmt.getRevision());
        assertEquals("imp-pref", importStmt.getPrefix());

        final Set<Module> submodules = rootModule.getSubmodules();
        assertEquals(1, submodules.size());
        assertEquals("submod", submodules.iterator().next().getName());

        final Set<NotificationDefinition> notifications = rootModule.getNotifications();
        assertEquals(1, notifications.size());
        assertEquals("notif1", notifications.iterator().next().getQName().getLocalName());

        final Set<RpcDefinition> rpcs = rootModule.getRpcs();
        assertEquals(1, rpcs.size());
        assertEquals("rpc1", rpcs.iterator().next().getQName().getLocalName());

        final Set<Deviation> deviations = rootModule.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation deviationStmt = deviations.iterator().next();
        assertNotNull(deviationStmt);
        assertEquals(contSchemaPath, deviationStmt.getTargetPath());
        assertEquals(DeviateKind.ADD, deviationStmt.getDeviates().iterator().next().getDeviateType());
        assertEquals("deviate reference", deviationStmt.getReference());

        final Set<IdentitySchemaNode> identities = rootModule.getIdentities();
        assertEquals(1, identities.size());
        assertEquals("identity1", identities.iterator().next().getQName().getLocalName());

        final Set<FeatureDefinition> features = rootModule.getFeatures();
        assertEquals(1, features.size());
        final FeatureDefinition featureStmt = features.iterator().next();
        assertNotNull(featureStmt);
        assertEquals(feature1, featureStmt.getQName());
        assertEquals(feature1SchemaPath, featureStmt.getPath());
        assertEquals("feature1 description", featureStmt.getDescription());
        assertEquals("feature1 reference", featureStmt.getReference());
        assertEquals(Status.CURRENT, featureStmt.getStatus());

        final List<ExtensionDefinition> extensionSchemaNodes = rootModule.getExtensionSchemaNodes();
        assertEquals(1, extensionSchemaNodes.size());
        assertEquals("ext1", extensionSchemaNodes.iterator().next().getQName().getLocalName());
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }
}
