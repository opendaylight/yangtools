/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;

class Bug7146Test {
    @Test
    void shouldFailOnSyntaxError() throws Exception {
        final var ex = assertThrows(SourceSyntaxException.class,
            () -> TestUtils.assertYangSource("/bugs/bug7146/foo.yang"));
        assertThat(ex.getMessage())
            .startsWith("extraneous input '#' expecting {'}', SEP, IDENTIFIER} [at ")
            .endsWith("/foo.yang:7:4]");
    }
}
