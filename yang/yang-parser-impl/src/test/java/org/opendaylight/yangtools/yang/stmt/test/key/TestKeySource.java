package org.opendaylight.yangtools.yang.stmt.test.key;

import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Key;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Module;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Namespace;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Prefix;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

public class TestKeySource implements StatementStreamSource {

    private static final String NS_PREFIX = "urn:org:opendaylight:yangtools:test:";

    private final String name;
    private final String key;
    private StatementWriter writer;
    private StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };

    public TestKeySource(String name, String key) {
        this.name = name;
        this.key = key;
    }

    @Override
    public void writeFull(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes)
            throws SourceException {

        this.writer = writer;

        header();
        extensions();
        body();
        end();
    }

    @Override
    public void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef) throws SourceException {
        this.writer = writer;
        header().end();
    }

    @Override
    public void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef,
            PrefixToModule prefixes) throws SourceException {
        this.writer = writer;
        header();
        extensions();
        end();
    }

    protected void extensions() throws SourceException {

    }

    protected void body() throws SourceException {

        stmt(Rfc6020Mapping.List).arg("lst");
        {
            stmt(Key).arg(key).end();
        }
        end();
    }

    TestKeySource header() throws SourceException {

        stmt(Module).arg(name);
        {
            stmt(Namespace).arg(getNamespace()).end();
            stmt(Prefix).arg(name).end();
        }

        return this;
    }

    private String getNamespace() {
        return NS_PREFIX + name;
    }

    protected TestKeySource arg(String arg) throws SourceException {
        writer.argumentValue(arg, REF);
        return this;
    }

    protected TestKeySource stmt(Rfc6020Mapping stmt) throws SourceException {
        writer.startStatement(stmt.getStatementName(), REF);
        return this;
    }

    protected TestKeySource end() throws SourceException {
        writer.endStatement(REF);
        return this;
    }
}
