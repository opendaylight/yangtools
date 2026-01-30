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

class AugmentSimplestTest {
    @Test
    void readAndParseYangFileTest() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/semantic-statement-parser/augmented.yang"))
            .addSource(sourceForResource("/semantic-statement-parser/root.yang"))
            .buildDeclared());
    }
}
