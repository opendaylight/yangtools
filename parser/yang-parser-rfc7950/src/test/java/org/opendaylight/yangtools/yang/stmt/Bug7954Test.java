/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class Bug7954Test extends AbstractYangTest {
    @Test
    void testParsingTheSameModuleTwice() {
        assertIllegalStateException(startsWith("Module namespace collision: [foo-ns]."),
            "/bugs/bug7954/foo.yang", "/bugs/bug7954/foo.yang");
    }

    @Test
    void testParsingTheSameSubmoduleTwice() {
        assertIllegalStateException(startsWith("Submodule name collision: [subbar]."), "/bugs/bug7954/bar.yang",
            "/bugs/bug7954/subbar.yang", "/bugs/bug7954/subbar.yang");
    }
}
