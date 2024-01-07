/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;

class Bug4410Test extends AbstractYangTest {
    @Test
    void test() {
        final var ex = assertInferenceExceptionDir("/bugs/bug4410",
            startsWith("Yang model processing phase EFFECTIVE_MODEL failed [at "));
        final var cause = assertInstanceOf(InferenceException.class, ex.getCause());
        assertThat(cause.getMessage(), allOf(startsWith("Type [(foo)"), containsString("was not found")));
    }
}
