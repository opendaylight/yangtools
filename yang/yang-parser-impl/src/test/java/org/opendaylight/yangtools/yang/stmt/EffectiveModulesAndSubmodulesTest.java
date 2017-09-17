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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class EffectiveModulesAndSubmodulesTest {

    private static final StatementStreamSource ROOT_MODULE = sourceForResource(
            "/stmt-test/submodules/root-module.yang");
    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource(
            "/stmt-test/submodules/imported-module.yang");
    private static final StatementStreamSource SUBMODULE_1 = sourceForResource(
            "/stmt-test/submodules/submodule-1.yang");
    private static final StatementStreamSource SUBMODULE_2 = sourceForResource(
            "/stmt-test/submodules/submodule-2.yang");
    private static final StatementStreamSource SUBMODULE_TO_SUBMODULE_1 = sourceForResource(
            "/stmt-test/submodules/submodule-to-submodule-1.yang");

    @Test
    public void modulesAndSubmodulesSimpleReferencesTest()
            throws SourceException, ReactorException, URISyntaxException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        reactor.addSources(ROOT_MODULE, IMPORTED_MODULE,
                SUBMODULE_1, SUBMODULE_2, SUBMODULE_TO_SUBMODULE_1);
        final SchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        final Set<Module> modules = result.getModules();
        assertNotNull(modules);
        assertEquals(2, modules.size());

        Module root = null;
        Module imported = null;
        for (final Module module : modules) {
            switch (module.getName()) {
                case "root-module":
                    root = module;
                    break;
                case "imported-module":
                    imported = module;
                    break;
                default:
            }
            StmtTestUtils.printReferences(module, false, "");
            StmtTestUtils.printChilds(module.getChildNodes(), "      ");
        }

        assertNotNull(root);
        assertNotNull(imported);

        final Collection<DataSchemaNode> rootChildNodes = root.getChildNodes();
        final Collection<DataSchemaNode> importedChildNodes = imported
                .getChildNodes();

        assertNotNull(rootChildNodes);
        assertNotNull(importedChildNodes);

        assertEquals(3, rootChildNodes.size());
        assertEquals(1, importedChildNodes.size());

        final Set<Module> rootSubmodules = root.getSubmodules();
        final Set<Module> importedSubmodules = imported.getSubmodules();

        assertNotNull(rootSubmodules);
        assertNotNull(importedSubmodules);

        assertEquals(2, rootSubmodules.size());
        assertEquals(0, importedSubmodules.size());

        Module sub1 = null;
        Module sub2 = null;
        for (final Module rootSubmodule : rootSubmodules) {
            switch (rootSubmodule.getName()) {
                case "submodule-1":
                    sub1 = rootSubmodule;
                    break;
                case "submodule-2":
                    sub2 = rootSubmodule;
                    break;
                default:
            }
        }

        assertNotNull(sub1);
        assertNotNull(sub2);

        assertEquals(QNameModule.create(new URI("root-module"),
                SimpleDateFormatUtil.DEFAULT_DATE_REV), sub1.getQNameModule());
        assertEquals(QNameModule.create(new URI("root-module"),
                SimpleDateFormatUtil.DEFAULT_DATE_REV), sub2.getQNameModule());

        final Collection<DataSchemaNode> sub1ChildNodes = sub1.getChildNodes();
        final Collection<DataSchemaNode> sub2ChildNodes = sub2.getChildNodes();

        assertNotNull(sub1ChildNodes);
        assertNotNull(sub2ChildNodes);

        assertEquals(1, sub1ChildNodes.size());
        assertEquals(1, sub2ChildNodes.size());

        final Set<Module> sub1Submodules = sub1.getSubmodules();
        final Set<Module> sub2Submodules = sub2.getSubmodules();

        assertNotNull(sub1Submodules);
        assertNotNull(sub2Submodules);

        assertEquals(1, sub1Submodules.size());
        assertEquals(0, sub2Submodules.size());

        Module sub1Submodule = null;
        for (final Module submodule : sub1Submodules) {
            switch (submodule.getName()) {
                case "submodule-to-submodule-1":
                    sub1Submodule = submodule;
                    break;
                default:
            }
        }

        assertNotNull(sub1Submodule);

        assertEquals(QNameModule.create(new URI("root-module"),
                SimpleDateFormatUtil.DEFAULT_DATE_REV),
                sub1Submodule.getQNameModule());

        final Collection<DataSchemaNode> sub1SubmoduleChildNodes = sub1Submodule.getChildNodes();
        assertNotNull(sub1SubmoduleChildNodes);
        assertEquals(1, sub1SubmoduleChildNodes.size());

        final Set<Module> sub1SubmoduleSubmodules = sub1Submodule.getSubmodules();
        assertNotNull(sub1SubmoduleSubmodules);
        assertEquals(0, sub1SubmoduleSubmodules.size());

        findModulesSubTest(result, root, imported);

        getDataChildByNameSubTest(result, root);
    }

    private static void getDataChildByNameSubTest(final SchemaContext result, final Module root) {
        final DataSchemaNode containerInRoot = result.getDataChildByName(QName
                .create(root.getQNameModule(), "container-in-root-module"));
        assertNotNull(containerInRoot);
        assertEquals("desc", containerInRoot.getDescription());
    }

    private static void findModulesSubTest(final SchemaContext result, final Module root, final Module imported)
            throws URISyntaxException {
        final Module foundRoot = result.findModuleByName("root-module",
                SimpleDateFormatUtil.DEFAULT_DATE_REV);
        final Set<Module> foundRoots = result.findModuleByNamespace(new URI(
                "root-module"));
        final Module foundRoot3 = result.findModuleByNamespaceAndRevision(new URI(
                "root-module"), SimpleDateFormatUtil.DEFAULT_DATE_REV);

        assertNotNull(foundRoot);
        assertNotNull(foundRoots);
        assertEquals(1, foundRoots.size());
        final Module foundRoot2 = foundRoots.iterator().next();

        assertNotNull(foundRoot2);
        assertNotNull(foundRoot3);

        assertEquals(root, foundRoot);
        assertEquals(root, foundRoot2);
        assertEquals(root, foundRoot3);

        final Module foundImported = result.findModuleByName("imported-module",
                SimpleDateFormatUtil.DEFAULT_DATE_REV);
        final Set<Module> foundImporteds = result.findModuleByNamespace(new URI(
                "imported-module"));
        final Module foundImported3 = result.findModuleByNamespaceAndRevision(
                new URI("imported-module"),
                SimpleDateFormatUtil.DEFAULT_DATE_REV);

        assertNotNull(foundImported);
        assertNotNull(foundImporteds);
        assertEquals(1, foundImporteds.size());
        final Module foundImported2 = foundImporteds.iterator().next();

        assertNotNull(foundImported2);
        assertNotNull(foundImported3);

        assertEquals(imported, foundImported);
        assertEquals(imported, foundImported2);
        assertEquals(imported, foundImported3);

        assertFalse(root.equals(imported));
    }
}
