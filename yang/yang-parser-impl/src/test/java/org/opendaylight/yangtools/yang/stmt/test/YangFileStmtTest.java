package org.opendaylight.yangtools.yang.stmt.test;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

import static org.junit.Assert.assertNotNull;

public class YangFileStmtTest {
        private static final TestYangFileStatementSource YANGFILE = new TestYangFileStatementSource("/semantic-statement-parser/test.yang");
        private static final TestYangFileStatementSource IMPORTEDYANGFILE = new TestYangFileStatementSource("/semantic-statement-parser/importedtest.yang");
        private static final TestYangFileStatementSource SIMPLENODES = new TestYangFileStatementSource("/semantic-statement-parser/simple-nodes-semantic.yang");
        private static final TestYangFileStatementSource FOO = new TestYangFileStatementSource("/semantic-statement-parser/foo.yang");
        private static final TestYangFileStatementSource FILE1 = new TestYangFileStatementSource("/model/bar.yang");
        private static final TestYangFileStatementSource FILE2 = new TestYangFileStatementSource("/model/baz.yang");
        private static final TestYangFileStatementSource FILE3 = new TestYangFileStatementSource("/model/foo.yang");
        private static final TestYangFileStatementSource FILE4 = new TestYangFileStatementSource("/model/subfoo.yang");
        private static final TestYangFileStatementSource EXTFILE = new TestYangFileStatementSource("/semantic-statement-parser/ext-typedef.yang");
        private static final TestYangFileStatementSource EXTUSE = new TestYangFileStatementSource("/semantic-statement-parser/ext-use.yang");

        @Test
        public void readAndParseYangFileTest() throws SourceException, ReactorException {
                CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
                addSources(reactor, YANGFILE, SIMPLENODES, IMPORTEDYANGFILE, FOO);
                addSources(reactor, FILE1, FILE2, FILE3, FILE4);
                addSources(reactor, EXTFILE, EXTUSE);
                EffectiveModelContext result = reactor.build();
                assertNotNull(result);
        }

        private void addSources(CrossSourceStatementReactor.BuildAction reactor, StatementStreamSource... sources) {
                for (StatementStreamSource source : sources) {
                        reactor.addSource(source);
                }
        }
}