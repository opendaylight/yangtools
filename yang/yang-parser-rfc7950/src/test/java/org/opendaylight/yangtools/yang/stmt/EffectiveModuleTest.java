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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class EffectiveModuleTest {

    private static final StatementStreamSource ROOT_MODULE = sourceForResource(
            "/semantic-statement-parser/effective-module/root.yang");
    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource(
            "/semantic-statement-parser/effective-module/imported.yang");
    private static final StatementStreamSource SUBMODULE = sourceForResource(
            "/semantic-statement-parser/effective-module/submod.yang");

    private static final QNameModule ROOT_MODULE_QNAME = QNameModule.create(URI.create("root-ns"));

    private static final QName CONT = QName.create(ROOT_MODULE_QNAME, "cont");
    private static final QName FEATURE1 = QName.create(ROOT_MODULE_QNAME, "feature1");

    private static final SchemaPath CONT_SCHEMA_PATH = SchemaPath.create(true, CONT);
    private static final SchemaPath FEATURE1_SCHEMA_PATH = SchemaPath.create(true, FEATURE1);

    private static final Revision REVISION = Revision.of("2000-01-01");

    @Test
    public void effectiveBuildTest() throws SourceException, ReactorException {
        SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(ROOT_MODULE, IMPORTED_MODULE, SUBMODULE)
                .buildEffective();

        assertNotNull(result);

        Module rootModule = result.findModules("root").iterator().next();
        assertNotNull(rootModule);

        assertEquals("root-pref", rootModule.getPrefix());
        assertEquals(YangVersion.VERSION_1, rootModule.getYangVersion());
        assertEquals(Optional.of("cisco"), rootModule.getOrganization());
        assertEquals(Optional.of("cisco email"), rootModule.getContact());

        final ContainerSchemaNode contSchemaNode = (ContainerSchemaNode) rootModule.getDataChildByName(CONT);
        assertNotNull(contSchemaNode);

        final Set<AugmentationSchemaNode> augmentations = rootModule.getAugmentations();
        assertEquals(1, augmentations.size());
        assertEquals(CONT_SCHEMA_PATH, augmentations.iterator().next().getTargetPath());

        final Set<String> imports = rootModule.getBoundPrefixes();
        assertEquals(1, imports.size());
//        final ModuleImport importStmt = imports.iterator().next();
//        assertNotNull(importStmt);
//        assertEquals("imported", importStmt.getModuleName());
//        assertEquals(Optional.of(REVISION), importStmt.getRevision());
//        assertEquals("imp-pref", importStmt.getPrefix());

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
        final QNameModule importedModuleQName = QNameModule.create(URI.create("imported"), REVISION);
        final QName importedContQName = QName.create(importedModuleQName, "cont");
        final SchemaPath importedContSchemaPath = SchemaPath.create(true, importedContQName);
        assertEquals(importedContSchemaPath, deviationStmt.getTargetPath());
        assertEquals(DeviateKind.ADD, deviationStmt.getDeviates().iterator().next().getDeviateType());
        assertEquals(Optional.of("deviate reference"), deviationStmt.getReference());

        final Set<IdentitySchemaNode> identities = rootModule.getIdentities();
        assertEquals(1, identities.size());
        assertEquals("identity1", identities.iterator().next().getQName().getLocalName());

        final Set<FeatureDefinition> features = rootModule.getFeatures();
        assertEquals(1, features.size());
        final FeatureDefinition featureStmt = features.iterator().next();
        assertNotNull(featureStmt);
        assertEquals(FEATURE1, featureStmt.getQName());
        assertEquals(FEATURE1_SCHEMA_PATH, featureStmt.getPath());
        assertEquals(Optional.of("feature1 description"), featureStmt.getDescription());
        assertEquals(Optional.of("feature1 reference"), featureStmt.getReference());
        assertEquals(Status.CURRENT, featureStmt.getStatus());

        final List<ExtensionDefinition> extensionSchemaNodes = rootModule.getExtensionSchemaNodes();
        assertEquals(1, extensionSchemaNodes.size());
        assertEquals("ext1", extensionSchemaNodes.iterator().next().getQName().getLocalName());
    }
}
