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

public class IncludeResolutionTest {

    private static final Logger log = Logger.getLogger(IncludeResolutionTest.class.getName());

    private static final IncludeTestStatementSource ROOT = new IncludeTestStatementSource(false,"root-module",null, "submodule-1");
    private static final IncludeTestStatementSource SUBMODULE1 = new IncludeTestStatementSource(true,"submodule-1","root-module", "submodule-2");
    private static final IncludeTestStatementSource SUBMODULE2 = new IncludeTestStatementSource(true,"submodule-2", "root-module");
    private static final IncludeTestStatementSource ERROR_MODULE = new IncludeTestStatementSource(false,"error-module", null, "foo");
    private static final IncludeTestStatementSource ERROR_SUBMODULE = new IncludeTestStatementSource(true,"error-submodule", "root-module", "foo");

    private static final IncludeTestStatementSource MISSING_PARENT_MODULE = new IncludeTestStatementSource(true,"missing-parent", "foo");
    @Test
    public void includeTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,ROOT,SUBMODULE1,SUBMODULE2);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingIncludedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,ERROR_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail doe to misssing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE,e.getPhase());
            log.info(e.getMessage());
        }

    }

    @Test
    public void missingIncludedSourceTest2() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,ERROR_SUBMODULE);
        try {
            reactor.build();
            fail("reactor.process should fail doe to misssing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE,e.getPhase());
            log.info(e.getMessage());
        }

    }

    //@Test
    public void missingIncludedSourceTest3() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,MISSING_PARENT_MODULE);
        try {
            EffectiveModelContext build = reactor.build();
            //fail("reactor.process should fail doe to misssing belongsTo source");
        } catch (ReactorException e) {
            //assertTrue(e instanceof SomeModifiersUnresolvedException);
            //assertEquals(ModelProcessingPhase.SourceLinkage,e.getPhase());
            throw(e);
        }

    }

    private static void addSources(final BuildAction reactor, final IncludeTestStatementSource... sources) {
        for(IncludeTestStatementSource source : sources) {
            reactor.addSource(source);
        }
    }

}
