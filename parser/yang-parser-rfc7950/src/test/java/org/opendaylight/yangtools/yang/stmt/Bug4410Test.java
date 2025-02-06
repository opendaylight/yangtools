/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;

class Bug4410Test extends AbstractYangTest {
    @Test
    void test() {
        final var cause = assertInstanceOf(InferenceException.class, assertInferenceExceptionDir("/bugs/bug4410",
            startsWith("Yang model processing phase EFFECTIVE_MODEL failed [at ")).getCause());
        assertThat(cause.getMessage()).startsWith("Type [(foo)").contains("was not found");
    }
}
