/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Bug3859Test extends AbstractYangTest {
    @Test
    void test() throws Exception {
        assertEquals(1, assertEffectiveModelDir("/bugs/bug3859").findModules("reference-in-unknown").size());
    }
}
