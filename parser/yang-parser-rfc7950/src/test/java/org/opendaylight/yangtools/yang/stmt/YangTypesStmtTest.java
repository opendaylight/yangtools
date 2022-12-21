/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YangTypesStmtTest extends AbstractYangTest {
    @Test
    void readAndParseYangFileTest() {
        assertEffectiveModel(
            "/semantic-statement-parser/types.yang",
            "/semantic-statement-parser/simple-types.yang",
            "/semantic-statement-parser/identityreftest.yang",
            "/semantic-statement-parser/model/bar.yang",
            "/semantic-statement-parser/model/baz.yang",
            "/semantic-statement-parser/model/subfoo.yang",
            "/semantic-statement-parser/model/foo.yang");
    }
}
