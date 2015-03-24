package org.opendaylight.yangtools.yang.stmt.test;

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
import org.opendaylight.yangtools.yang.stmt.test.ImportResolutionTest.ModuleEntry;

import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Container;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Import;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Module;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Namespace;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Prefix;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Revision;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.RevisionDate;

class TestStatementSource implements StatementStreamSource {

    private static final String NS_PREFIX = "urn:org:opendaylight:yangtools:test:";

    private final String name;
    private final String revision;
    private final List<ModuleEntry> imports;
    private StatementWriter writer;
    private StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };

    public TestStatementSource(ModuleEntry module, ModuleEntry... imports) {

        this.name = module.getName();
        this.revision = module.getRevision();
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
        // stmt(YangVersion).arg("1");end();
        // stmt(Description).arg("Here goes description of this module");end();
        // stmt(Contact).arg("Here goes our address...");end();
        //
        // stmt(Revision).arg("2015-03-12");
        // stmt(Description).arg("This revision brings this and that...");end();
        // stmt(Reference).arg("Learn more here...");end();
        // end();

        stmt(Container).arg(name + ":my-container-with-prefix");
        // stmt(Leaf).arg("MyContainerLeaf");
        // stmt(Type).arg("string");end();
        // stmt(Config).arg("TRUE");end();
        // end();
        // stmt(Choice).arg("choose-your-destiny");
        // stmt(Default).arg("destiny-one");end();
        // stmt(Case).arg("destiny-one");
        // stmt(Leaf).arg("destiny-one-leaf");end();
        // end();
        // stmt(Case).arg("destiny-two");
        // stmt(Mandatory).arg("true");
        // stmt(Container).arg("InnerContainer");
        // stmt(Leaf).arg("leaf-of-deep");end();
        // end();
        // end();
        // end();
        // stmt(Container).arg("MyContainerInContainer");
        // stmt(Must).arg("xpath expression");
        // stmt(Leaf).arg("MyContainerInContainerLeaf");
        // stmt(Type).arg("string");end();
        // end();
        // stmt(Deviation).arg("/tst:test/tst:element");
        // stmt(Deviate).arg("add");end();
        // end();
        // end();
        end();
        stmt(Container).arg("my-container-without-prefix").end();

        int i = 0;
        for (ModuleEntry imp : imports) {
            i++;
            stmt(Container).arg(imp.getName() + ":my-container-with-imported-prefix" + i).end();
        }

    }

    TestStatementSource header() throws SourceException {
        stmt(Module).arg(name);
        {
            stmt(Namespace).arg(getNamespace()).end();
            stmt(Prefix).arg(name).end();
            stmt(Revision).arg(revision).end();

            for (ModuleEntry impEntry : imports) {

                String imp = impEntry.getName();
                String rev = impEntry.getRevision();

                stmt(Import).arg(imp);
                stmt(Prefix).arg(imp).end();

                if (rev != null)
                    stmt(RevisionDate).arg(rev).end();

                end();
            }
        }
        return this;
    }

    private String getNamespace() {
        return NS_PREFIX + name;
    }

    protected TestStatementSource arg(String arg) throws SourceException {
        writer.argumentValue(arg, REF);
        return this;
    }

    protected TestStatementSource stmt(Rfc6020Mapping stmt) throws SourceException {
        writer.startStatement(stmt.getStatementName(), REF);
        return this;
    }

    protected TestStatementSource end() throws SourceException {
        writer.endStatement(REF);
        return this;
    }

}
