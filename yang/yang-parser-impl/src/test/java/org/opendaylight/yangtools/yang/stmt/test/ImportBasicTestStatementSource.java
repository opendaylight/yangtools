/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.CONTAINER;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.IMPORT;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.MODULE;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.NAMESPACE;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.PREFIX;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.REVISION;
import static org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.REVISION_DATE;
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

class ImportBasicTestStatementSource implements StatementStreamSource {

    private static final String NS_PREFIX = "urn:org:opendaylight:yangtools:test:";

    private final String name;
    private final List<String> imports;
    private StatementWriter writer;
    private final StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };


    public ImportBasicTestStatementSource(final String name, final String... imports) {
        this.name = name;
        this.imports = Arrays.asList(imports);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes)
            throws SourceException {
        this.writer = writer;
        header();
        extensions();
        body();
        end();

    }



    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) throws SourceException {
        this.writer = writer;
        header().end();
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) throws SourceException {
        this.writer = writer;
        header();
        extensions();
        end();

    }

    protected void extensions() throws SourceException {

    }

    protected void body() throws SourceException {
//        stmt(YangVersion).arg("1");end();
//        stmt(Description).arg("Here goes description of this module");end();
//        stmt(Contact).arg("Here goes our address...");end();
//
//        stmt(Revision).arg("2015-03-12");
//            stmt(Description).arg("This revision brings this and that...");end();
//            stmt(Reference).arg("Learn more here...");end();
//        end();

        stmt(CONTAINER).arg(name+":my-container-with-prefix");
//            stmt(Leaf).arg("MyContainerLeaf");
//                stmt(Type).arg("string");end();
//                stmt(Config).arg("TRUE");end();
//            end();
//            stmt(Choice).arg("choose-your-destiny");
//                stmt(Default).arg("destiny-one");end();
//                stmt(Case).arg("destiny-one");
//                    stmt(Leaf).arg("destiny-one-leaf");end();
//                end();
//                stmt(Case).arg("destiny-two");
//                    stmt(Mandatory).arg("true");
//                    stmt(Container).arg("InnerContainer");
//                        stmt(Leaf).arg("leaf-of-deep");end();
//                    end();
//                end();
//            end();
//            stmt(Container).arg("MyContainerInContainer");
//                stmt(Must).arg("xpath expression");
//                stmt(Leaf).arg("MyContainerInContainerLeaf");
//                    stmt(Type).arg("string");end();
//                end();
//                stmt(Deviation).arg("/tst:test/tst:element");
//                    stmt(Deviate).arg("add");end();
//                end();
//            end();
        end();
        stmt(CONTAINER).arg("my-container-without-prefix").end();

        int i = 0;
        for(String imp : imports)  {
            i++;
            stmt(CONTAINER).arg(imp+":my-container-with-imported-prefix"+i).end();
        }

    }

    ImportBasicTestStatementSource header() throws SourceException {
        stmt(MODULE).arg(name); {
            stmt(NAMESPACE).arg(getNamespace()).end();
            stmt(PREFIX).arg(name).end();
            stmt(REVISION).arg("2000-01-01").end();
            for(String imp : imports)  {
                stmt(IMPORT).arg(imp);
                    stmt(PREFIX).arg(imp).end();
                    stmt(REVISION_DATE).arg("2000-01-01").end();
                end();
            }
        }
        return this;
    }

    private String getNamespace() {
        return NS_PREFIX + name;
    }

    protected ImportBasicTestStatementSource arg(final String arg) throws SourceException {
        writer.argumentValue(arg, REF);
        return this;
    }

    protected ImportBasicTestStatementSource stmt(final Rfc6020Mapping stmt) throws SourceException {
        writer.startStatement(stmt.getStatementName(), REF);
        return this;
    }

    protected ImportBasicTestStatementSource end() throws SourceException {
        writer.endStatement(REF);
        return this;
    }
}
