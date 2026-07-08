/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class Bug7954Test extends AbstractYangTest {
    @Test
    void testParsingTheSameModuleTwice() {
        final var ex = assertInferenceException("/bugs/bug7954/foo.yang", "/bugs/bug7954/foo.yang");
        assertEquals(
            "Module namespace collision: foo-ns@2017-03-14 is already defined [at somewhere in foo@2017-03-14.yang]",
            ex.getMessage());
    }

    @Test
    @Disabled("FIXME: name-based detection not implemented yet")
    void testParsingTheSameSubmoduleTwice() {
        assertInferenceExceptionMessage(
            "/bugs/bug7954/bar.yang", "/bugs/bug7954/subbar.yang", "/bugs/bug7954/subbar.yang")
            .startsWith("Submodule name collision: subbar.");
    }
}
