/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class YangFileStmtTest {
    //basic statements to parse and write
    private static final YangStatementSourceImpl YANGFILE = new YangStatementSourceImpl("/semantic-statement-parser/test.yang", false);
    private static final YangStatementSourceImpl IMPORTEDYANGFILE = new YangStatementSourceImpl("/semantic-statement-parser/importedtest.yang", false);
    private static final YangStatementSourceImpl SIMPLENODES = new YangStatementSourceImpl("/semantic-statement-parser/simple-nodes-semantic.yang", false);
    private static final YangStatementSourceImpl FOOBAR = new YangStatementSourceImpl("/semantic-statement-parser/foobar.yang", false);
    //extension statement to parse and write
    private static final YangStatementSourceImpl EXTFILE = new YangStatementSourceImpl("/semantic-statement-parser/ext-typedef.yang", false);
    private static final YangStatementSourceImpl EXTUSE = new YangStatementSourceImpl("/semantic-statement-parser/ext-use.yang", false);


    private static final YangStatementSourceImpl BAR = new YangStatementSourceImpl("/model-new/bar.yang", false);
    private static final YangStatementSourceImpl BAZ = new YangStatementSourceImpl("/model-new/baz.yang", false);
    private static final YangStatementSourceImpl FOO = new YangStatementSourceImpl("/model-new/foo.yang", false);
    private static final YangStatementSourceImpl SUBFOO = new YangStatementSourceImpl("/model-new/subfoo.yang", false);

    private static final YangStatementSourceImpl BAR2 = new YangStatementSourceImpl("/model/bar.yang",false);
    private static final YangStatementSourceImpl BAZ2 = new YangStatementSourceImpl("/model/baz.yang",false);
    private static final YangStatementSourceImpl FOO2 = new YangStatementSourceImpl("/model/foo.yang",false);
    private static final YangStatementSourceImpl SUBFOO2 = new YangStatementSourceImpl("/model/subfoo.yang",false);

    @Test
    public void readAndParseYangFileTestModel() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        addSources(reactor, BAZ,FOO,BAR,SUBFOO);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void readAndParseYangFileTestModel2() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        addSources(reactor, BAZ2,FOO2,BAR2,SUBFOO2);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, YANGFILE, SIMPLENODES, IMPORTEDYANGFILE, FOOBAR);
        addSources(reactor, EXTFILE, EXTUSE);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}