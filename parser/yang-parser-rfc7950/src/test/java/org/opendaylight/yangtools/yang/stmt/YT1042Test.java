/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.Test;

class YT1042Test extends AbstractYangTest {
    @Test
    void testSubmoduleConflict() {
        assertSourceExceptionDir("/bugs/YT1042",
            startsWith("Cannot add data tree child with name (foo)foo, a conflicting child already exists"));
    }
}
