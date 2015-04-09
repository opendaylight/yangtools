package org.opendaylight.yangtools.yang.stmt.test;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IncludeRevisionsTest {

    private static final TestYangFileStatementSource EQUAL_ROOT = new TestYangFileStatementSource("/revisions/equal-root.yang");
    private static final TestYangFileStatementSource EQUAL_REV = new TestYangFileStatementSource("/revisions/equal-rev.yang");
    private static final TestYangFileStatementSource UNEQUAL_ROOT = new TestYangFileStatementSource("/revisions/unequal-root.yang");
    private static final TestYangFileStatementSource UNEQUAL_REV = new TestYangFileStatementSource("/revisions/unequal-rev.yang");
    private static final TestYangFileStatementSource SUBMOD_ONLY_ROOT = new TestYangFileStatementSource("/revisions/submod-only-root.yang");
    private static final TestYangFileStatementSource SUBMOD_ONLY_REV = new TestYangFileStatementSource("/revisions/submod-only-rev.yang");
    private static final TestYangFileStatementSource MOD_ONLY_ROOT = new TestYangFileStatementSource("/revisions/mod-only-root.yang");
    private static final TestYangFileStatementSource MOD_ONLY_REV = new TestYangFileStatementSource("/revisions/mod-only-rev.yang");
    private static final TestYangFileStatementSource MOD_ONLY_1970_ROOT = new TestYangFileStatementSource("/revisions/mod-1970-root.yang");
    private static final TestYangFileStatementSource MOD_ONLY_1970_REV = new TestYangFileStatementSource("/revisions/mod-1970-rev.yang");
    private static final TestYangFileStatementSource NOWHERE_ROOT = new TestYangFileStatementSource("/revisions/nowhere-root.yang");
    private static final TestYangFileStatementSource NOWHERE_REV = new TestYangFileStatementSource("/revisions/nowhere-rev.yang");


    @Test
    public void revsEqualTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, EQUAL_REV, EQUAL_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revsUnequalTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, UNEQUAL_REV, UNEQUAL_ROOT);

        try {
            reactor.build();
            fail("reactor.process should fail due to unequal revisions in include and submodule");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SourceLinkage, e.getPhase());
        }
    }

    @Test
    public void revIncludeOnly() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, SUBMOD_ONLY_REV, SUBMOD_ONLY_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revInModuleOnly() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, MOD_ONLY_REV, MOD_ONLY_ROOT);

        try {
            reactor.build();
            fail("reactor.process should fail due to missing revision in included submodule");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SourceLinkage, e.getPhase());
        }
    }


    @Test
    public void rev1970InModuleOnlyTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, MOD_ONLY_1970_REV, MOD_ONLY_1970_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revNowhereTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, NOWHERE_REV, NOWHERE_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    private void addSources(CrossSourceStatementReactor.BuildAction reactor, StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
