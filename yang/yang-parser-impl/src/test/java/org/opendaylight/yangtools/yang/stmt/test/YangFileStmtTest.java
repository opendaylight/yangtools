package org.opendaylight.yangtools.yang.stmt.test;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

import static org.junit.Assert.assertNotNull;

public class YangFileStmtTest {
    private static final TestYangFileStatementSource YANGFILE = new TestYangFileStatementSource("/semantic-statement-parser/test.yang");

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSource(YANGFILE);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }
}
