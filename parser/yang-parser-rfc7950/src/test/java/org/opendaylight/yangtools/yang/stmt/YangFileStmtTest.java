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
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class YangFileStmtTest extends AbstractYangTest {
    private static final StatementStreamSource BAR = sourceForResource("/model-new/bar.yang");
    private static final StatementStreamSource BAZ = sourceForResource("/model-new/baz.yang");
    private static final StatementStreamSource FOO = sourceForResource("/model-new/foo.yang");
    private static final StatementStreamSource SUBFOO = sourceForResource("/model-new/subfoo.yang");

    private static final StatementStreamSource BAR2 = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource BAZ2 = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource FOO2 = sourceForResource("/model/foo.yang");
    private static final StatementStreamSource SUBFOO2 = sourceForResource("/model/subfoo.yang");

    @Test
    void readAndParseYangFileTestModel() throws ReactorException {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild().addSources(BAZ, FOO, BAR, SUBFOO).build());
    }

    @Test
    void readAndParseYangFileTestModel2() throws ReactorException {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild().addSources(BAZ2, FOO2, BAR2, SUBFOO2).build());
    }

    @Test
    void readAndParseYangFileTest() {
        assertEffectiveModel(
            //basic statements to parse and write
            "/semantic-statement-parser/test.yang",
            "/semantic-statement-parser/simple-nodes-semantic.yang",
            "/semantic-statement-parser/importedtest.yang",
            "/semantic-statement-parser/foobar.yang",
            //extension statement to parse and write
            "/semantic-statement-parser/ext-typedef.yang",
            "/semantic-statement-parser/ext-use.yang");
    }
}