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
import org.opendaylight.yangtools.yang.stmt.test.TestStatementSource.ModuleEntry;

public class ImportRevisionsTest {

    private static final ModuleEntry modImported = new ModuleEntry("nature", "2006-05-05");
    private static final ModuleEntry modImportedRevDiff = new ModuleEntry("nature", "2011-12-12");
    private static final ModuleEntry modImportedRevMiss = new ModuleEntry("nature", null);
    private static final ModuleEntry modImported1970 = new ModuleEntry("nature", "1970-01-01");

    private static final ModuleEntry modImporter = new ModuleEntry("mammal", "2006-05-05");

    private static final TestStatementSource IMPORTED = new TestStatementSource(modImported);
    private static final TestStatementSource IMPORTED_NO_REV = new TestStatementSource(modImportedRevMiss);

    private static final TestStatementSource IMPORT = new TestStatementSource(modImporter, modImported);
    private static final TestStatementSource IMPORT_REV_DFLT = new TestStatementSource(modImporter, modImported1970);
    private static final TestStatementSource IMPORT_REV_DIFF = new TestStatementSource(modImporter, modImportedRevDiff);
    private static final TestStatementSource IMPORT_NO_REV = new TestStatementSource(modImporter, modImportedRevMiss);

    @Test
    public void revsEqualTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED, IMPORT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revsDiffTest() throws SourceException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED, IMPORT_REV_DIFF);

        try {
            reactor.build();
            fail("reactor.process should fail due to unequal revisions in imported modules");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void revInModuleOnly() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED, IMPORT_NO_REV);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revImportOnly() throws SourceException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED_NO_REV, IMPORT);

        try {
            reactor.build();
            fail("reactor.process should fail due to missing revision in imported module");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void revDefault1970InImport() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED_NO_REV, IMPORT_REV_DFLT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revNowhere() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED_NO_REV, IMPORT_NO_REV);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    private static void addSources(final BuildAction reactor, final TestStatementSource... sources) {
        for (TestStatementSource source : sources) {
            reactor.addSource(source);
        }
    }
}
