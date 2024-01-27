/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EffectiveModulesAndSubmodulesTest {
    private static final Logger LOG = LoggerFactory.getLogger(EffectiveModulesAndSubmodulesTest.class);
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

    private static final QNameModule ROOT = QNameModule.of("root-module");

    @Test
    void modulesAndSubmodulesSimpleReferencesTest() throws ReactorException {
        final var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(ROOT_MODULE, IMPORTED_MODULE, SUBMODULE_1, SUBMODULE_2, SUBMODULE_TO_SUBMODULE_1)
            .buildEffective();

        assertNotNull(result);

        final var modules = result.getModules();
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
            printReferences(module, false, "");
            printChilds(module.getChildNodes(), "      ");
        }

        assertNotNull(root);
        assertNotNull(imported);

        final var rootChildNodes = root.getChildNodes();
        final var importedChildNodes = imported.getChildNodes();

        assertNotNull(rootChildNodes);
        assertNotNull(importedChildNodes);

        assertEquals(3, rootChildNodes.size());
        assertEquals(1, importedChildNodes.size());

        final var rootSubmodules = root.getSubmodules();
        final var importedSubmodules = imported.getSubmodules();

        assertNotNull(rootSubmodules);
        assertNotNull(importedSubmodules);

        assertEquals(2, rootSubmodules.size());
        assertEquals(0, importedSubmodules.size());

        Submodule sub1 = null;
        Submodule sub2 = null;
        for (var rootSubmodule : rootSubmodules) {
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

        final var sub1ChildNodes = sub1.getChildNodes();
        final var sub2ChildNodes = sub2.getChildNodes();

        assertNotNull(sub1ChildNodes);
        assertNotNull(sub2ChildNodes);

        assertEquals(1, sub1ChildNodes.size());
        assertEquals(1, sub2ChildNodes.size());

        final var sub1Submodules = sub1.getSubmodules();
        final var sub2Submodules = sub2.getSubmodules();

        assertNotNull(sub1Submodules);
        assertNotNull(sub2Submodules);

        assertEquals(1, sub1Submodules.size());
        assertEquals(0, sub2Submodules.size());

        Submodule sub1Submodule = null;
        for (var submodule : sub1Submodules) {
            switch (submodule.getName()) {
                case "submodule-to-submodule-1":
                    sub1Submodule = submodule;
                    break;
                default:
            }
        }

        assertNotNull(sub1Submodule);

        assertEquals(ROOT, sub1Submodule.getQNameModule());

        final var sub1SubmoduleChildNodes = sub1Submodule.getChildNodes();
        assertNotNull(sub1SubmoduleChildNodes);
        assertEquals(1, sub1SubmoduleChildNodes.size());

        final var sub1SubmoduleSubmodules = sub1Submodule.getSubmodules();
        assertNotNull(sub1SubmoduleSubmodules);
        assertEquals(0, sub1SubmoduleSubmodules.size());

        findModulesSubTest(result, root, imported);

        getDataChildByNameSubTest(result, root);
    }

    private static void getDataChildByNameSubTest(final EffectiveModelContext result, final Module root) {
        final var containerInRoot = result.getDataChildByName(
            QName.create(root.getQNameModule(), "container-in-root-module"));
        assertEquals(Optional.of("desc"), containerInRoot.getDescription());
    }

    private static void findModulesSubTest(final EffectiveModelContext result, final Module root,
            final Module imported) {
        final var foundRoot = result.findModule("root-module").orElseThrow();
        final var foundRoots = result.findModules(XMLNamespace.of("root-module"));
        final var foundRoot3 = result.findModule(XMLNamespace.of("root-module")).orElseThrow();

        assertNotNull(foundRoot);
        assertNotNull(foundRoots);
        assertEquals(1, foundRoots.size());
        final var foundRoot2 = foundRoots.iterator().next();

        assertNotNull(foundRoot2);
        assertNotNull(foundRoot3);

        assertEquals(root, foundRoot);
        assertEquals(root, foundRoot2);
        assertEquals(root, foundRoot3);

        final var foundImported = result.findModule("imported-module").orElseThrow();
        final var foundImporteds = result.findModules(XMLNamespace.of("imported-module"));
        final var foundImported3 = result.findModule(XMLNamespace.of("imported-module")).orElseThrow();

        assertNotNull(foundImported);
        assertNotNull(foundImporteds);
        assertEquals(1, foundImporteds.size());
        final var foundImported2 = foundImporteds.iterator().next();

        assertNotNull(foundImported2);
        assertNotNull(foundImported3);

        assertEquals(imported, foundImported);
        assertEquals(imported, foundImported2);
        assertEquals(imported, foundImported3);

        assertNotEquals(root, imported);
    }

    private static void printReferences(final ModuleLike module, final boolean isSubmodule, final String indent) {
        LOG.debug("{}{} {}", indent, isSubmodule ? "Submodule" : "Module", module.getName());
        for (var submodule : module.getSubmodules()) {
            printReferences(submodule, true, indent + "      ");
            printChilds(submodule.getChildNodes(), indent + "            ");
        }
    }

    private static void printChilds(final Collection<? extends DataSchemaNode> childNodes, final String indent) {
        for (var child : childNodes) {
            LOG.debug("{}{} {}", indent, "Child", child.getQName().getLocalName());
            if (child instanceof DataNodeContainer container) {
                printChilds(container.getChildNodes(), indent + "      ");
            }
        }
    }
}
