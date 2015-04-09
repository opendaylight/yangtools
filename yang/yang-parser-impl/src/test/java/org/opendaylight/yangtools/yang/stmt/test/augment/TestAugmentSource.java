package org.opendaylight.yangtools.yang.stmt.test.augment;

import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Augment;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Import;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Module;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Namespace;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Prefix;

import java.util.Arrays;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

public class TestAugmentSource implements StatementStreamSource {

    private static final String NS_PREFIX = "urn:org:opendaylight:yangtools:test:";

    private final String name;
    private final String augment;
    private final java.util.List<String> imports;
    private StatementWriter writer;
    private StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };

    public TestAugmentSource(String name, String augment, String... imports) {
        this.name = name;
        this.augment = augment;
        this.imports = Arrays.asList(imports);
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

        stmt(Augment).arg(augment);
        end();
    }

    TestAugmentSource header() throws SourceException {

        stmt(Module).arg(name);
        {
            stmt(Namespace).arg(getNamespace()).end();
            stmt(Prefix).arg(name).end();
        }

        for (String impEntry : imports) {

            stmt(Import).arg(impEntry);
            {
                stmt(Prefix).arg(impEntry).end();
            }
            end();
        }

        return this;
    }

    private String getNamespace() {
        return NS_PREFIX + name;
    }

    protected TestAugmentSource arg(String arg) throws SourceException {
        writer.argumentValue(arg, REF);
        return this;
    }

    protected TestAugmentSource stmt(Rfc6020Mapping stmt) throws SourceException {
        writer.startStatement(stmt.getStatementName(), REF);
        return this;
    }

    protected TestAugmentSource end() throws SourceException {
        writer.endStatement(REF);
        return this;
    }
}
