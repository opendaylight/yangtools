package org.opendaylight.yangtools.yang.stmt.test;

import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.*;

import java.util.Arrays;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

class UsesTestStatementSource implements StatementStreamSource {

    private static final String NS_PREFIX = "urn:org:opendaylight:yangtools:test:";

    private final String name;
    private StatementWriter writer;
    private List<String> groupingChain;
    private UsesResolutionTest.TYPE type;
    private StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };

    public UsesTestStatementSource(String name, String... groupingChain) {
        this.name = name;
        this.groupingChain = Arrays.asList(groupingChain);
        this.type = UsesResolutionTest.TYPE.DEFAULT;
    }

    /**
     * @param name2
     * @param cyclic
     */
    public UsesTestStatementSource(String name, UsesResolutionTest.TYPE type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void writeFull(StatementWriter writer,
            QNameToStatementDefinition stmtDef, PrefixToModule prefixes)
            throws SourceException {
        this.writer = writer;
        header();
        extensions();
        body();
        end();

    }

    @Override
    public void writeLinkage(StatementWriter writer,
            QNameToStatementDefinition stmtDef) throws SourceException {
        this.writer = writer;
        header().end();
    }

    @Override
    public void writeLinkageAndStatementDefinitions(StatementWriter writer,
            QNameToStatementDefinition stmtDef, PrefixToModule prefixes)
            throws SourceException {
        this.writer = writer;
        header();
        extensions();
        end();

    }

    protected void extensions() throws SourceException {

    }

    protected void body() throws SourceException {

        switch (type) {
        case CYCLIC:
            writeCyclic();
            break;
        case NON_CYCLIC:
            writeNonCyclic();
            break;
        case MISSING:
            writeMissing();
            break;
        case ROOT:
            writeRoot();
            break;
        case INCORRECT_ROOT:
            writeIncorrectRoot();
            break;
        case IMPORT:
            writeImport();
            break;

        default:
            writeDefault();

        }

    }


    private void writeIncorrectRoot() throws SourceException {
        stmt(Container).arg("root-container-with-uses");
        {
            stmt(Uses).arg("imp-grp").end();
            stmt(Uses).arg("local-grp").end();
        }
        end();

        stmt(Grouping).arg("local-grp");
        {
            writeGroupingBody("local-grp");
        }
        end();
    }

    private void writeRoot() throws SourceException {
        stmt(Container).arg("root-container-with-uses");
        {
            stmt(Uses).arg("imp:imp-grp").end();
            stmt(Uses).arg("local-grp").end();
        }
        end();

        stmt(Grouping).arg("local-grp");
        {
            writeGroupingBody("local-grp");
        }
        end();
    }

    private void writeImport() throws SourceException {
        stmt(Grouping).arg("imp-grp");
        {
            writeGroupingBody("imp-grp");
        }
        end();
    }

    private void writeNonCyclic() throws SourceException {
        stmt(Container).arg("container-with-uses");
        stmt(Uses).arg("grp1").end();
        end();

        stmt(Grouping).arg("grp1");
        stmt(Uses).arg("grp2").end();
        end();

        stmt(Grouping).arg("grp2");
        stmt(Uses).arg("grp3").end();
        end();

        stmt(Grouping).arg("grp3");
        writeGroupingBody("grp3");
        end();
    }

    private void writeMissing() throws SourceException {
        stmt(Container).arg("container-with-uses");
        stmt(Uses).arg("grp1").end();
        stmt(Uses).arg("missing-grp").end();
        end();

        stmt(Grouping).arg("grp1");
        writeGroupingBody("grp1");
        end();
    }

    private void writeCyclic() throws SourceException {
        stmt(Container).arg("container-with-uses");
        stmt(Uses).arg("grp1").end();
        end();

        stmt(Grouping).arg("grp1");
        stmt(Uses).arg("grp2").end();
        end();

        stmt(Grouping).arg("grp2");
        stmt(Uses).arg("grp3").end();
        end();

        stmt(Grouping).arg("grp3");
        stmt(Uses).arg("grp1").end();
        end();
    }

    private void writeDefault() throws SourceException {
        stmt(Container).arg("container-with-uses");
        for (String grpName : groupingChain) {
            stmt(Uses).arg(grpName).end();
        }
        end();

        for (String grpName : groupingChain) {
            stmt(Grouping).arg(grpName);
            writeGroupingBody(grpName);
            stmt(Uses).arg("inner-" + grpName).end();
            end();
        }

        for (String grpName : groupingChain) {
            stmt(Grouping).arg("inner-" + grpName);
            writeGroupingBody("inner-" + grpName);
            end();
        }
    }

    private void writeGroupingBody(String groupingName) throws SourceException {
        stmt(Container).arg(groupingName + "-container");
        stmt(Container).arg(groupingName + "-container-2");
        stmt(Container).arg(groupingName + "-container-3");
        end();
        end();
        end();

        stmt(Container).arg(groupingName + "-container-b");
        stmt(Container).arg(groupingName + "-container-2-b");
        stmt(Container).arg(groupingName + "-container-3-b");
        end();
        end();
        end();
    }

    UsesTestStatementSource header() throws SourceException {
        switch (type) {
        case ROOT:
            writeRootHeader();
            break;
        default:
            writeModuleHeader();

        }
        return this;
    }

    private void writeRootHeader() throws SourceException {
        stmt(Module).arg(name);
        {
            stmt(Namespace).arg(getNamespace()).end();
            stmt(Prefix).arg(name).end();
            stmt(Revision).arg("2000-01-01").end();

            stmt(Import).arg("import-module");
            stmt(Prefix).arg("imp").end();
            stmt(RevisionDate).arg("2000-01-01").end();
            end();
        }
    }

    /**
     *
     */
    private void writeModuleHeader() throws SourceException {

        stmt(Module).arg(name);
        {
            stmt(Namespace).arg(getNamespace()).end();
            stmt(Prefix).arg(name).end();
            stmt(Revision).arg("2000-01-01").end();
            // for(String inc : includes) {
            // stmt(Include).arg(inc);
            // stmt(RevisionDate).arg("2000-01-01").end();
            // end();
            // }
        }

    }

    private String getNamespace() {
        return NS_PREFIX + name;
    }

    protected UsesTestStatementSource arg(String arg) throws SourceException {
        writer.argumentValue(arg, REF);
        return this;
    }

    protected UsesTestStatementSource stmt(Rfc6020Mapping stmt)
            throws SourceException {
        writer.startStatement(stmt.getStatementName(), REF);
        return this;
    }

    protected UsesTestStatementSource end() throws SourceException {
        writer.endStatement(REF);
        return this;
    }

}
