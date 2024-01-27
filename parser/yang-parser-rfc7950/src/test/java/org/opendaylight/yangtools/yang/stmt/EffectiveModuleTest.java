/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

class EffectiveModuleTest {
    private static final QNameModule ROOT_MODULE_QNAME = QNameModule.of("root-ns");
    private static final QName CONT = QName.create(ROOT_MODULE_QNAME, "cont");
    private static final QName FEATURE1 = QName.create(ROOT_MODULE_QNAME, "feature1");
    private static final Revision REVISION = Revision.of("2000-01-01");

    @Test
    void effectiveBuildTest() throws Exception {
        final var rootModule = TestUtils.parseYangSource(
            "/semantic-statement-parser/effective-module/root.yang",
            "/semantic-statement-parser/effective-module/imported.yang",
            "/semantic-statement-parser/effective-module/submod.yang")
            .findModules("root").iterator().next();
        assertNotNull(rootModule);

        assertEquals("root-pref", rootModule.getPrefix());
        assertEquals(YangVersion.VERSION_1, rootModule.getYangVersion());
        assertEquals(Optional.of("cisco"), rootModule.getOrganization());
        assertEquals(Optional.of("cisco email"), rootModule.getContact());

        final var contSchemaNode = (ContainerSchemaNode) rootModule.getDataChildByName(CONT);
        assertNotNull(contSchemaNode);

        final var augmentations = rootModule.getAugmentations();
        assertEquals(1, augmentations.size());
        assertEquals(Absolute.of(CONT), augmentations.iterator().next().getTargetPath());

        final var imports = rootModule.getImports();
        assertEquals(1, imports.size());
        final var importStmt = imports.iterator().next();
        assertNotNull(importStmt);
        assertEquals(Unqualified.of("imported"), importStmt.getModuleName());
        assertEquals(Optional.of(REVISION), importStmt.getRevision());
        assertEquals("imp-pref", importStmt.getPrefix());

        final var submodules = rootModule.getSubmodules();
        assertEquals(1, submodules.size());
        assertEquals("submod", submodules.iterator().next().getName());

        final var notifications = rootModule.getNotifications();
        assertEquals(1, notifications.size());
        assertEquals("notif1", notifications.iterator().next().getQName().getLocalName());

        final var rpcs = rootModule.getRpcs();
        assertEquals(1, rpcs.size());
        assertEquals("rpc1", rpcs.iterator().next().getQName().getLocalName());

        final var deviations = rootModule.getDeviations();
        assertEquals(1, deviations.size());
        final var deviationStmt = deviations.iterator().next();
        assertNotNull(deviationStmt);
        final var importedContQName = QName.create(QNameModule.of(XMLNamespace.of("imported"), REVISION), "cont");
        assertEquals(Absolute.of(importedContQName), deviationStmt.getTargetPath());
        assertEquals(DeviateKind.ADD, deviationStmt.getDeviates().iterator().next().getDeviateType());
        assertEquals(Optional.of("deviate reference"), deviationStmt.getReference());

        final var identities = rootModule.getIdentities();
        assertEquals(1, identities.size());
        assertEquals("identity1", identities.iterator().next().getQName().getLocalName());

        final var features = rootModule.getFeatures();
        assertEquals(1, features.size());
        final var featureStmt = features.iterator().next();
        assertNotNull(featureStmt);
        assertEquals(FEATURE1, featureStmt.getQName());
        assertEquals(Optional.of("feature1 description"), featureStmt.getDescription());
        assertEquals(Optional.of("feature1 reference"), featureStmt.getReference());
        assertEquals(Status.CURRENT, featureStmt.getStatus());

        final var extensionSchemaNodes = rootModule.getExtensionSchemaNodes();
        assertEquals(1, extensionSchemaNodes.size());
        assertEquals("ext1", extensionSchemaNodes.iterator().next().getQName().getLocalName());
    }
}
