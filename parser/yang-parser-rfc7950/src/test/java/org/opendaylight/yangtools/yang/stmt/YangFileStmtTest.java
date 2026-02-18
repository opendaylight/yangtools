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

class YangFileStmtTest extends AbstractYangTest {
    @Test
    void readAndParseYangFileTestModel() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/model-new/baz.yang"))
            .addSource(sourceForResource("/model-new/foo.yang"))
            .addSource(sourceForResource("/model-new/bar.yang"))
            .addSource(sourceForResource("/model-new/subfoo.yang"))
            .buildDeclared());
    }

    @Test
    void readAndParseYangFileTestModel2() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/model/baz.yang"))
            .addSource(sourceForResource("/model/foo.yang"))
            .addSource(sourceForResource("/model/bar.yang"))
            .addSource(sourceForResource("/model/subfoo.yang"))
            .buildDeclared());
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
