package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertNotNull;

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
            "/stmt-test/submodules/root-module.yang");
    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl(
            "/stmt-test/submodules/imported-module.yang");
    private static final YangStatementSourceImpl SUBMODULE_1 = new YangStatementSourceImpl(
            "/stmt-test/submodules/submodule-1.yang");
    private static final YangStatementSourceImpl SUBMODULE_2 = new YangStatementSourceImpl(
            "/stmt-test/submodules/submodule-2.yang");
    private static final YangStatementSourceImpl SUBMODULE_TO_SUBMODULE_1 = new YangStatementSourceImpl(
            "/stmt-test/submodules/submodule-to-submodule-1.yang");

    @Test
    public void modulesAndSubmodulesSimpleReferencesTest()
            throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, IMPORTED_MODULE,
                SUBMODULE_1, SUBMODULE_2, SUBMODULE_TO_SUBMODULE_1);
        EffectiveSchemaContext result = reactor.buildEffective();

        Set<Module> modules = result.getModules();
        for (Module module : modules) {
            StmtTestUtils.printReferences(module, false, "");
            StmtTestUtils.printChilds(module.getChildNodes(), "      ");
        }

        assertNotNull(result);
    }

}
