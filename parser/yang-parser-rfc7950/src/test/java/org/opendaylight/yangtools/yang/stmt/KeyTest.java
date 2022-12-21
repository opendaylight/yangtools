/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class KeyTest extends AbstractYangTest {
    @Test
    void keySimpleTest() {
        assertEffectiveModel("/semantic-statement-parser/key-arg-parsing/key-simple-and-comp.yang");
    }

    @Test
    void keyCompositeInvalid() {
        assertSourceException(startsWith("Key argument 'key1 key2 key2' contains duplicates"),
            "/semantic-statement-parser/key-arg-parsing/key-comp-duplicate.yang");
    }
}
