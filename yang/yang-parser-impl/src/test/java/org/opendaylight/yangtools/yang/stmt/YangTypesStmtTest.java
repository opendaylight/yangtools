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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class YangTypesStmtTest {

    private static final StatementStreamSource TYPEFILE1 = sourceForResource("/semantic-statement-parser/types.yang");
    private static final StatementStreamSource TYPEFILE2 = sourceForResource(
        "/semantic-statement-parser/simple-types.yang");
    private static final StatementStreamSource TYPEFILE3 = sourceForResource(
        "/semantic-statement-parser/identityreftest.yang");

    private static final StatementStreamSource FILE1 = sourceForResource("/semantic-statement-parser/model/bar.yang");
    private static final StatementStreamSource FILE2 = sourceForResource("/semantic-statement-parser/model/baz.yang");
    private static final StatementStreamSource FILE3 = sourceForResource(
        "/semantic-statement-parser/model/subfoo.yang");
    private static final StatementStreamSource FILE4 = sourceForResource("/semantic-statement-parser/model/foo.yang");

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, TYPEFILE1, TYPEFILE2, TYPEFILE3);
        addSources(reactor, FILE1, FILE2, FILE3, FILE4);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
