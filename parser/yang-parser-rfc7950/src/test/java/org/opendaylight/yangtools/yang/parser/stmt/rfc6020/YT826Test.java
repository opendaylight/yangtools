/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class YT826Test extends AbstractYangTest {
    @Test
    void testWhenExpressionWhitespace() {
        assertEffectiveModel("/bugs/yangtools826/example.yang");
    }
}
