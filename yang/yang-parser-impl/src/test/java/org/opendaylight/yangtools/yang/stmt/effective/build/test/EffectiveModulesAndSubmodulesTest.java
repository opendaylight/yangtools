package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.opendaylight.yangtools.yang.common.QName;

import org.opendaylight.yangtools.yang.common.QNameModule;
import java.net.URISyntaxException;
import java.net.URI;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.stmt.test.StmtTestUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.junit.Test;

public class EffectiveModulesAndSubmodulesTest {

    private static final YangStatementSourceImpl ROOT_MODULE = new YangStatementSourceImpl(
            "/stmt-test/submodules/root-module.yang",false);
    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl(
            "/stmt-test/submodules/imported-module.yang",false);
    private static final YangStatementSourceImpl SUBMODULE_1 = new YangStatementSourceImpl(
            "/stmt-test/submodules/submodule-1.yang",false);
    private static final YangStatementSourceImpl SUBMODULE_2 = new YangStatementSourceImpl(
            "/stmt-test/submodules/submodule-2.yang",false);
    private static final YangStatementSourceImpl SUBMODULE_TO_SUBMODULE_1 = new YangStatementSourceImpl(
            "/stmt-test/submodules/submodule-to-submodule-1.yang",false);

    @Test
    public void modulesAndSubmodulesSimpleReferencesTest()
            throws SourceException, ReactorException, URISyntaxException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, IMPORTED_MODULE,
                SUBMODULE_1, SUBMODULE_2, SUBMODULE_TO_SUBMODULE_1);
        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Set<Module> modules = result.getModules();
        assertNotNull(modules);
        assertEquals(2, modules.size());

        Module root = null;
        Module imported = null;
        for (Module module : modules) {
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

        Collection<DataSchemaNode> rootChildNodes = root.getChildNodes();
        Collection<DataSchemaNode> importedChildNodes = imported
                .getChildNodes();

        assertNotNull(rootChildNodes);
        assertNotNull(importedChildNodes);

        assertEquals(3, rootChildNodes.size());
        assertEquals(1, importedChildNodes.size());

        Set<Module> rootSubmodules = root.getSubmodules();
        Set<Module> importedSubmodules = imported.getSubmodules();

        assertNotNull(rootSubmodules);
        assertNotNull(importedSubmodules);

        assertEquals(2, rootSubmodules.size());
        assertEquals(0, importedSubmodules.size());

        Module sub1 = null;
        Module sub2 = null;
        for (Module rootSubmodule : rootSubmodules) {
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

        Collection<DataSchemaNode> sub1ChildNodes = sub1.getChildNodes();
        Collection<DataSchemaNode> sub2ChildNodes = sub2.getChildNodes();

        assertNotNull(sub1ChildNodes);
        assertNotNull(sub2ChildNodes);

        assertEquals(2, sub1ChildNodes.size());
        assertEquals(1, sub2ChildNodes.size());

        Set<Module> sub1Submodules = sub1.getSubmodules();
        Set<Module> sub2Submodules = sub2.getSubmodules();

        assertNotNull(sub1Submodules);
        assertNotNull(sub2Submodules);

        assertEquals(1, sub1Submodules.size());
        assertEquals(0, sub2Submodules.size());

        Module sub1Submodule = null;
        for (Module submodule : sub1Submodules) {
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

        Collection<DataSchemaNode> sub1SubmoduleChildNodes = sub1Submodule
                .getChildNodes();
        assertNotNull(sub1SubmoduleChildNodes);
        assertEquals(1, sub1SubmoduleChildNodes.size());

        Set<Module> sub1SubmoduleSubmodules = sub1Submodule.getSubmodules();
        assertNotNull(sub1SubmoduleSubmodules);
        assertEquals(0, sub1SubmoduleSubmodules.size());

        findModulesSubTest(result, root, imported);

        getDataChildByNameSubTest(result, root);

    }

    private void getDataChildByNameSubTest(EffectiveSchemaContext result,
            Module root) {
        DataSchemaNode containerInRoot = result.getDataChildByName(QName
                .create(root.getQNameModule(), "container-in-root-module"));
        assertNotNull(containerInRoot);
        assertEquals("desc", containerInRoot.getDescription());
    }

    private void findModulesSubTest(EffectiveSchemaContext result, Module root,
            Module imported) throws URISyntaxException {
        Module foundRoot = result.findModuleByName("root-module",
                SimpleDateFormatUtil.DEFAULT_DATE_REV);
        Set<Module> foundRoots = result.findModuleByNamespace(new URI(
                "root-module"));
        Module foundRoot3 = result.findModuleByNamespaceAndRevision(new URI(
                "root-module"), SimpleDateFormatUtil.DEFAULT_DATE_REV);

        assertNotNull(foundRoot);
        assertNotNull(foundRoots);
        assertEquals(1, foundRoots.size());
        Module foundRoot2 = foundRoots.iterator().next();

        assertNotNull(foundRoot2);
        assertNotNull(foundRoot3);

        assertEquals(root, foundRoot);
        assertEquals(root, foundRoot2);
        assertEquals(root, foundRoot3);

        Module foundImported = result.findModuleByName("imported-module",
                SimpleDateFormatUtil.DEFAULT_DATE_REV);
        Set<Module> foundImporteds = result.findModuleByNamespace(new URI(
                "imported-module"));
        Module foundImported3 = result.findModuleByNamespaceAndRevision(
                new URI("imported-module"),
                SimpleDateFormatUtil.DEFAULT_DATE_REV);

        assertNotNull(foundImported);
        assertNotNull(foundImporteds);
        assertEquals(1, foundImporteds.size());
        Module foundImported2 = foundImporteds.iterator().next();

        assertNotNull(foundImported2);
        assertNotNull(foundImported3);

        assertEquals(imported, foundImported);
        assertEquals(imported, foundImported2);
        assertEquals(imported, foundImported3);

        assertFalse(root.equals(imported));
    }

}
