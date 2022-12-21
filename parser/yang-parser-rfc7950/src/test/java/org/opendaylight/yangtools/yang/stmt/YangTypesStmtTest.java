/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class YangTypesStmtTest {

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
    void readAndParseYangFileTest() throws ReactorException {
        SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(TYPEFILE1, TYPEFILE2, TYPEFILE3, FILE1, FILE2, FILE3, FILE4)
            .buildEffective();
        assertNotNull(result);
    }
}
