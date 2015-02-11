package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class ImportResolutionTest {

    private static final TestStatementSource ROOT_WITHOUT_IMPORT = new TestStatementSource("nature");
    private static final TestStatementSource IMPORT_ROOT = new TestStatementSource("mammal","nature");
    private static final TestStatementSource IMPORT_DERIVED = new TestStatementSource("human", "mammal");
    private static final TestStatementSource IMPORT_SELF = new TestStatementSource("egocentric", "egocentric");
    private static final TestStatementSource CICLE_YIN = new TestStatementSource("cycle-yin", "cycle-yang");
    private static final TestStatementSource CICLE_YANG = new TestStatementSource("cycle-yang", "cycle-yin");


    @Test
    public void inImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,ROOT_WITHOUT_IMPORT,IMPORT_ROOT,IMPORT_DERIVED);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void inInverseOfImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,IMPORT_DERIVED,IMPORT_ROOT,ROOT_WITHOUT_IMPORT);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingImportedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,IMPORT_DERIVED,ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail doe to misssing imported source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SourceLinkage,e.getPhase());
        }

    }

    @Test
    public void circularImportsTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,CICLE_YIN,CICLE_YANG);
        try {
            reactor.build();
            fail("reactor.process should fail doe to circular import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SourceLinkage,e.getPhase());
        }
    }

    @Test
    public void selfImportTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,IMPORT_SELF,IMPORT_ROOT,ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail doe to self import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SourceLinkage,e.getPhase());
        }
    }


    private void addSources(BuildAction reactor, TestStatementSource... sources) {
        for(TestStatementSource source : sources) {
            reactor.addSource(source);
        }
    }

}
