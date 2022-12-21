/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class YT1441Test extends AbstractYangTest {
    @Test
    void testInvalidRange() {
        assertSourceException(startsWith(
            "Range constraint does not match fraction-digits: Decreasing scale of 2.345 to 2 requires rounding [at "),
            "/bugs/YT1441/foo.yang");
    }
}
