package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class IncludeRevisionsTest {

    private static final YangStatementSourceImpl EQUAL_ROOT = new YangStatementSourceImpl("/revisions/equal-root.yang",
            false);
    private static final YangStatementSourceImpl EQUAL_REV = new YangStatementSourceImpl("/revisions/equal-rev.yang",
            false);
    private static final YangStatementSourceImpl UNEQUAL_ROOT = new YangStatementSourceImpl(
            "/revisions/unequal-root.yang", false);
    private static final YangStatementSourceImpl UNEQUAL_REV = new YangStatementSourceImpl(
            "/revisions/unequal-rev.yang", false);
    private static final YangStatementSourceImpl SUBMOD_ONLY_ROOT = new YangStatementSourceImpl(
            "/revisions/submod-only-root.yang", false);
    private static final YangStatementSourceImpl SUBMOD_ONLY_REV = new YangStatementSourceImpl(
            "/revisions/submod-only-rev.yang", false);
    private static final YangStatementSourceImpl MOD_ONLY_ROOT = new YangStatementSourceImpl(
            "/revisions/mod-only-root.yang", false);
    private static final YangStatementSourceImpl MOD_ONLY_REV = new YangStatementSourceImpl(
            "/revisions/mod-only-rev.yang", false);
    private static final YangStatementSourceImpl MOD_ONLY_1970_ROOT = new YangStatementSourceImpl(
            "/revisions/mod-1970-root.yang", false);
    private static final YangStatementSourceImpl MOD_ONLY_1970_REV = new YangStatementSourceImpl(
            "/revisions/mod-1970-rev.yang", false);
    private static final YangStatementSourceImpl NOWHERE_ROOT = new YangStatementSourceImpl(
            "/revisions/nowhere-root.yang", false);
    private static final YangStatementSourceImpl NOWHERE_REV = new YangStatementSourceImpl(
            "/revisions/nowhere-rev.yang", false);

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
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
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
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
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

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
