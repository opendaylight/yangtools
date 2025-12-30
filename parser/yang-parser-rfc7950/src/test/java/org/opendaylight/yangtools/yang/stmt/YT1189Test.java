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
        assertSourceException(startsWith("""
            'cont' is not a valid augment target-node on position 1: 'c' is not '/' as required by \
            absolute-schema-nodeid [at """), "/bugs/YT1189/foo.yang");
    }

    @Test
    void testAbsoluteUsesAugment() {
        assertSourceException(startsWith("""
            '/grp-cont' is not a valid augment target-node on position 1: '/' is not a valid prefix nor identifier [at \
            """), "/bugs/YT1189/bar.yang");
    }
}
