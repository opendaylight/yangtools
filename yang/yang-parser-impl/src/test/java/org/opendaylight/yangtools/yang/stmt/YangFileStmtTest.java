/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class YangFileStmtTest {
    //basic statements to parse and write
    private static final StatementStreamSource YANGFILE = sourceForResource("/semantic-statement-parser/test.yang");
    private static final StatementStreamSource IMPORTEDYANGFILE = sourceForResource(
        "/semantic-statement-parser/importedtest.yang");
    private static final StatementStreamSource SIMPLENODES = sourceForResource(
        "/semantic-statement-parser/simple-nodes-semantic.yang");
    private static final StatementStreamSource FOOBAR = sourceForResource("/semantic-statement-parser/foobar.yang");
    //extension statement to parse and write
    private static final StatementStreamSource EXTFILE = sourceForResource(
        "/semantic-statement-parser/ext-typedef.yang");
    private static final StatementStreamSource EXTUSE = sourceForResource("/semantic-statement-parser/ext-use.yang");


    private static final StatementStreamSource BAR = sourceForResource("/model-new/bar.yang");
    private static final StatementStreamSource BAZ = sourceForResource("/model-new/baz.yang");
    private static final StatementStreamSource FOO = sourceForResource("/model-new/foo.yang");
    private static final StatementStreamSource SUBFOO = sourceForResource("/model-new/subfoo.yang");

    private static final StatementStreamSource BAR2 = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource BAZ2 = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource FOO2 = sourceForResource("/model/foo.yang");
    private static final StatementStreamSource SUBFOO2 = sourceForResource("/model/subfoo.yang");

    @Test
    public void readAndParseYangFileTestModel() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        reactor.addSources(BAZ,FOO,BAR,SUBFOO);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void readAndParseYangFileTestModel2() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        reactor.addSources(BAZ2,FOO2,BAR2,SUBFOO2);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(YANGFILE, SIMPLENODES, IMPORTEDYANGFILE, FOOBAR);
        reactor.addSources(EXTFILE, EXTUSE);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }
}