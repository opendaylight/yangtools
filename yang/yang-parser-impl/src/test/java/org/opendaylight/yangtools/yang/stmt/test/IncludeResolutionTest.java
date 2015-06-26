package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class IncludeResolutionTest {

    private static final Logger log = Logger.getLogger(IncludeResolutionTest.class.getName());

    private static final YangStatementSourceImpl ROOT = new YangStatementSourceImpl(
            "/semantic-statement-parser/include-arg-parsing/root-module.yang", false);
    private static final YangStatementSourceImpl SUBMODULE1 = new YangStatementSourceImpl(
            "/semantic-statement-parser/include-arg-parsing/submodule-1.yang", false);
    private static final YangStatementSourceImpl SUBMODULE2 = new YangStatementSourceImpl(
            "/semantic-statement-parser/include-arg-parsing/submodule-2.yang", false);
    private static final YangStatementSourceImpl ERROR_MODULE = new YangStatementSourceImpl(
            "/semantic-statement-parser/include-arg-parsing/error-module.yang", false);
    private static final YangStatementSourceImpl ERROR_SUBMODULE = new YangStatementSourceImpl(
            "/semantic-statement-parser/include-arg-parsing/error-submodule.yang", false);

    private static final YangStatementSourceImpl MISSING_PARENT_MODULE = new YangStatementSourceImpl(
            "/semantic-statement-parser/include-arg-parsing/missing-parent.yang", false);

    @Test
    public void includeTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, ROOT, SUBMODULE1, SUBMODULE2);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingIncludedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, ERROR_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
            log.info(e.getMessage());
        }

    }

    @Test
    public void missingIncludedSourceTest2() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, ERROR_SUBMODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
            log.info(e.getMessage());
        }

    }

    @Test
    public void missingIncludedSourceTest3() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, MISSING_PARENT_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing belongsTo source");
        } catch (ReactorException e) {
            log.info(e.getMessage());
        }

    }

    private void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }
}
