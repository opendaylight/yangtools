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
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

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

    private static final QNameModule ROOT = QNameModule.create(URI.create("root-module"));

    @Test
    public void modulesAndSubmodulesSimpleReferencesTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(ROOT_MODULE, IMPORTED_MODULE, SUBMODULE_1, SUBMODULE_2, SUBMODULE_TO_SUBMODULE_1)
                .buildEffective();

        assertNotNull(result);

        final Collection<? extends Module> modules = result.getModules();
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

        final Collection<? extends DataSchemaNode> rootChildNodes = root.getChildNodes();
        final Collection<? extends DataSchemaNode> importedChildNodes = imported.getChildNodes();

        assertNotNull(rootChildNodes);
        assertNotNull(importedChildNodes);

        assertEquals(3, rootChildNodes.size());
        assertEquals(1, importedChildNodes.size());

        final Collection<? extends Submodule> rootSubmodules = root.getSubmodules();
        final Collection<? extends Submodule> importedSubmodules = imported.getSubmodules();

        assertNotNull(rootSubmodules);
        assertNotNull(importedSubmodules);

        assertEquals(2, rootSubmodules.size());
        assertEquals(0, importedSubmodules.size());

        Submodule sub1 = null;
        Submodule sub2 = null;
        for (final Submodule rootSubmodule : rootSubmodules) {
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

        assertEquals(ROOT, sub1.getQNameModule());
        assertEquals(ROOT, sub2.getQNameModule());

        final Collection<? extends DataSchemaNode> sub1ChildNodes = sub1.getChildNodes();
        final Collection<? extends DataSchemaNode> sub2ChildNodes = sub2.getChildNodes();

        assertNotNull(sub1ChildNodes);
        assertNotNull(sub2ChildNodes);

        assertEquals(1, sub1ChildNodes.size());
        assertEquals(1, sub2ChildNodes.size());

        final Collection<? extends Submodule> sub1Submodules = sub1.getSubmodules();
        final Collection<? extends Submodule> sub2Submodules = sub2.getSubmodules();

        assertNotNull(sub1Submodules);
        assertNotNull(sub2Submodules);

        assertEquals(1, sub1Submodules.size());
        assertEquals(0, sub2Submodules.size());

        Submodule sub1Submodule = null;
        for (final Submodule submodule : sub1Submodules) {
            switch (submodule.getName()) {
                case "submodule-to-submodule-1":
                    sub1Submodule = submodule;
                    break;
                default:
            }
        }

        assertNotNull(sub1Submodule);

        assertEquals(ROOT, sub1Submodule.getQNameModule());

        final Collection<? extends DataSchemaNode> sub1SubmoduleChildNodes = sub1Submodule.getChildNodes();
        assertNotNull(sub1SubmoduleChildNodes);
        assertEquals(1, sub1SubmoduleChildNodes.size());

        final Collection<? extends Submodule> sub1SubmoduleSubmodules = sub1Submodule.getSubmodules();
        assertNotNull(sub1SubmoduleSubmodules);
        assertEquals(0, sub1SubmoduleSubmodules.size());

        findModulesSubTest(result, root, imported);

        getDataChildByNameSubTest(result, root);
    }

    private static void getDataChildByNameSubTest(final SchemaContext result, final Module root) {
        final DataSchemaNode containerInRoot = result.getDataChildByName(QName
                .create(root.getQNameModule(), "container-in-root-module"));
        assertNotNull(containerInRoot);
        assertEquals(Optional.of("desc"), containerInRoot.getDescription());
    }

    private static void findModulesSubTest(final SchemaContext result, final Module root, final Module imported) {
        final Module foundRoot = result.findModule("root-module").get();
        final Collection<? extends Module> foundRoots = result.findModules(URI.create("root-module"));
        final Module foundRoot3 = result.findModule(URI.create("root-module")).get();

        assertNotNull(foundRoot);
        assertNotNull(foundRoots);
        assertEquals(1, foundRoots.size());
        final Module foundRoot2 = foundRoots.iterator().next();

        assertNotNull(foundRoot2);
        assertNotNull(foundRoot3);

        assertEquals(root, foundRoot);
        assertEquals(root, foundRoot2);
        assertEquals(root, foundRoot3);

        final Module foundImported = result.findModule("imported-module").get();
        final Collection<? extends Module> foundImporteds = result.findModules(URI.create("imported-module"));
        final Module foundImported3 = result.findModule(URI.create("imported-module")).get();

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
