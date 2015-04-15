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




class IncludeTestStatementSource implements StatementStreamSource {

    private static final String NS_PREFIX = "urn:org:opendaylight:yangtools:test:";

    private final String name;
    private final List<String> includes;
    private StatementWriter writer;
    private StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };

    private boolean isSubmodule;

    private String belongsTo;


    public IncludeTestStatementSource(boolean isSubmodule, String name, String belongsTo, String... includes) {
        this.name = name;
        this.includes = Arrays.asList(includes);
        this.isSubmodule=isSubmodule;
        this.belongsTo = belongsTo;
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

        String prefix = isSubmodule ? belongsTo : name;

        stmt(CONTAINER).arg(prefix+":my-container-with-prefix");

        end();
        stmt(CONTAINER).arg("my-container-without-prefix").end();

    }

    IncludeTestStatementSource header() throws SourceException {

        if(isSubmodule) writeSubmoduleHeader();
        else writeModuleHeader();

        return this;
    }

    /**
     *
     */
    private void writeSubmoduleHeader() throws SourceException {
        stmt(SUBMODULE).arg(name); {


            stmt(REVISION).arg("2000-01-01").end();
            if(belongsTo != null) {
                stmt(BELONGS_TO).arg(belongsTo);
                stmt(PREFIX).arg(belongsTo).end();
                end();
            }

            for(String inc : includes)  {
                stmt(INCLUDE).arg(inc);
                    stmt(REVISION_DATE).arg("2000-01-01").end();
                end();
            }
        }

    }

    /**
     *
     */
    private void writeModuleHeader() throws SourceException{
        stmt(MODULE).arg(name); {
            stmt(NAMESPACE).arg(getNamespace()).end();
            stmt(PREFIX).arg(name).end();
            stmt(REVISION).arg("2000-01-01").end();
            for(String inc : includes)  {
                stmt(INCLUDE).arg(inc);
                    stmt(REVISION_DATE).arg("2000-01-01").end();
                end();
            }
        }

    }

    private String getNamespace() {
        return NS_PREFIX + name;
    }

    protected IncludeTestStatementSource arg(String arg) throws SourceException {
        writer.argumentValue(arg, REF);
        return this;
    }

    protected IncludeTestStatementSource stmt(Rfc6020Mapping stmt) throws SourceException {
        writer.startStatement(stmt.getStatementName(), REF);
        return this;
    }

    protected IncludeTestStatementSource end() throws SourceException {
        writer.endStatement(REF);
        return this;
    }


}
