package org.opendaylight.yangtools.yang.parser.impl;

import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParserBaseListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangStatementParserListenerImpl extends YangStatementParserBaseListener {
        private static final Logger LOG = LoggerFactory.getLogger(YangStatementParserListenerImpl.class);

        private StatementWriter writer;
        private StatementSourceReference ref;

        public StatementSourceReference getRef() {
                return ref;
        }

        public void setRef(StatementSourceReference ref) {
                this.ref = ref;
        }

        public StatementWriter getWriter() {
                return writer;
        }

        public void setWriter(StatementWriter writer) {
                this.writer = writer;
        }

        @Override
        public void enterStatement(YangStatementParser.StatementContext ctx) {
                for (int i = 0; i < ctx.getChildCount(); i++) {
                        ParseTree child = ctx.getChild(i);
                        if (child instanceof YangStatementParser.KeywordContext) {
                                try {
                                        writer.startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, ((YangStatementParser.KeywordContext) child).children.get(0).getText()), ref);
                                } catch (SourceException e) {
                                        e.printStackTrace();
                                }
                        } else if (child instanceof YangStatementParser.ArgumentContext) {
                                try {
                                        writer.argumentValue(Utils.trimQuotesFromString(((YangStatementParser.ArgumentContext) child).children.get(0).getText()), ref);
                                } catch (SourceException e) {
                                        e.printStackTrace();
                                }
                        }
                }
        }

        @Override
        public void exitStatement(YangStatementParser.StatementContext ctx) {
                try {
                        writer.endStatement(ref);
                } catch (SourceException e) {
                        e.printStackTrace();
                }

        }
}
