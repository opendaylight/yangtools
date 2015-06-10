/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

import static org.junit.Assert.assertNotNull;

public class YangTypesStmtTest {

    private static final YangStatementSourceImpl TYPEFILE1 = new YangStatementSourceImpl
            ("/semantic-statement-parser/types.yang",false);
    private static final YangStatementSourceImpl TYPEFILE2 = new YangStatementSourceImpl
            ("/semantic-statement-parser/simple-types.yang",false);
    private static final YangStatementSourceImpl TYPEFILE3 = new YangStatementSourceImpl
            ("/semantic-statement-parser/identityreftest.yang",false);

    private static final YangStatementSourceImpl FILE1 = new YangStatementSourceImpl
            ("/semantic-statement-parser/model/bar.yang",false);
    private static final YangStatementSourceImpl FILE2 = new YangStatementSourceImpl
            ("/semantic-statement-parser/model/baz.yang",false);
    private static final YangStatementSourceImpl FILE3 = new YangStatementSourceImpl
            ("/semantic-statement-parser/model/subfoo.yang",false);
    private static final YangStatementSourceImpl FILE4 = new YangStatementSourceImpl
            ("/semantic-statement-parser/model/foo.yang",false);

    @Test
    public void readAndParseYangFileTest() throws SourceException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, TYPEFILE1, TYPEFILE2, TYPEFILE3);
        addSources(reactor, FILE1, FILE2, FILE3, FILE4);
        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    private void addSources(CrossSourceStatementReactor.BuildAction reactor, StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
