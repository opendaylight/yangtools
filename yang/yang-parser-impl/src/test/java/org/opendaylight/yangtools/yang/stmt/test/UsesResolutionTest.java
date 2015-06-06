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
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class UsesResolutionTest {

    //private static final Logger log = Logger.getLogger(IncludeResolutionTest.class.getName());

    public static enum TYPE {
        CYCLIC,
        MISSING,
        NON_CYCLIC,
        ROOT,
        IMPORT,
        INCORRECT_ROOT,
        DEFAULT
    }

    private static final UsesTestStatementSource GRP_TST = new UsesTestStatementSource(
            "my-module", "grouping-1", "grouping-2");

    private static final UsesTestStatementSource CYCLIC = new UsesTestStatementSource("my-module",TYPE.CYCLIC);

    private static final UsesTestStatementSource MISSING = new UsesTestStatementSource("my-module",TYPE.MISSING);

    private static final UsesTestStatementSource NON_CYCLIC = new UsesTestStatementSource("my-module",TYPE.NON_CYCLIC);

    private static final UsesTestStatementSource ROOT = new UsesTestStatementSource("root-module",TYPE.ROOT);
    private static final UsesTestStatementSource IMPORT = new UsesTestStatementSource("import-module",TYPE.IMPORT);
    private static final UsesTestStatementSource INCORRECT_ROOT = new UsesTestStatementSource("import-module",TYPE.INCORRECT_ROOT);

    @Test
    public void usesImportTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, ROOT,IMPORT);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void usesTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, GRP_TST);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void usesAlmostCyclicTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, NON_CYCLIC);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void usesCyclicTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, CYCLIC);
        try {
            EffectiveModelContext result = reactor.build();
            fail("reactor.process should fail doe to cyclic uses-grouping statements");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.FULL_DECLARATION, e.getPhase());
            log(e,"");
        }
    }

    @Test
    public void usesMissingTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, MISSING);
        try {
            EffectiveModelContext result = reactor.build();
            fail("reactor.process should fail doe to misssing grouping");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.FULL_DECLARATION, e.getPhase());
            log(e,"");
        }
    }

    @Test
    public void usesBadImportTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INCORRECT_ROOT, IMPORT);
        try {
            EffectiveModelContext result = reactor.build();
            fail("reactor.process should fail doe to misssing grouping");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.FULL_DECLARATION, e.getPhase());
            log(e,"");
        }
    }

    private void log(final Throwable e, final String indent) {
        System.out.println(indent + e.getMessage());

        Throwable[] suppressed = e.getSuppressed();
        for (Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }

    }

    private static void addSources(final BuildAction reactor,
            final UsesTestStatementSource... sources) {
        for (UsesTestStatementSource source : sources) {
            reactor.addSource(source);
        }
    }
}
