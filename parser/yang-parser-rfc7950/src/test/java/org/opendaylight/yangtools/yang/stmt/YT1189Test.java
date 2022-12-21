/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class YT1189Test extends AbstractYangTest {
    @Test
    void testDescendantAugment() {
        assertSourceException(startsWith("Descendant schema node identifier is not allowed when used outside"
            + " of a uses statement [at "), "/bugs/YT1189/foo.yang");
    }

    @Test
    void testAbsoluteUsesAugment() {
        assertSourceException(startsWith("Absolute schema node identifier is not allowed when used within a"
            + " uses statement [at "), "/bugs/YT1189/bar.yang");
    }
}
