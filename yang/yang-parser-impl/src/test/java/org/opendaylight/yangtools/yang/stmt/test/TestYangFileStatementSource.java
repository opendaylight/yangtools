package org.opendaylight.yangtools.yang.stmt.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.impl.YangStatementParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

public class TestYangFileStatementSource implements StatementStreamSource {

        private YangStatementParserListenerImpl yangStatementModelParser;
        private YangStatementParser.StatementContext statementContext;
        private ParseTreeWalker walker;

        public TestYangFileStatementSource(final String fileName) {
                try {
                        statementContext = parseYangSource(loadFile(fileName));
                        walker = new ParseTreeWalker();
                        yangStatementModelParser = new YangStatementParserListenerImpl(REF);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private final StatementSourceReference REF = new StatementSourceReference() {

                @Override
                public StatementSource getStatementSource() {
                        return StatementSource.DECLARATION;
                }
        };

        @Override
        public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) throws SourceException {
                yangStatementModelParser.setAttributes(writer, stmtDef);
                walker.walk(yangStatementModelParser, statementContext);
        }

        @Override
        public void writeLinkageAndStatementDefinitions(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) throws SourceException {
                yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
                walker.walk(yangStatementModelParser, statementContext);
        }

        @Override
        public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) throws SourceException {
                yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
                walker.walk(yangStatementModelParser, statementContext);
        }

        private FileInputStream loadFile(final String fileName) throws Exception {
                return new FileInputStream(new File(getClass().getResource(fileName).toURI()));
        }

        private static YangStatementParser.StatementContext parseYangSource(final InputStream stream) throws IOException {
                final YangStatementLexer lexer = new YangStatementLexer(new ANTLRInputStream(stream));
                final CommonTokenStream tokens = new CommonTokenStream(lexer);
                final YangStatementParser parser = new YangStatementParser(tokens);
                //TODO: no error listener yet
                //parser.removeErrorListeners();
                //final YangErrorListener errorListener = new YangErrorListener();
                //parser.addErrorListener(errorListener);
                final YangStatementParser.StatementContext result = parser.statement();
                //errorListener.validate();
                return result;
        }
}