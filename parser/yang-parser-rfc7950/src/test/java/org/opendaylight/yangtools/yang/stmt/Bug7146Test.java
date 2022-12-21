/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

class Bug7146Test {
    @Test
    void shouldFailOnSyntaxError() {
        final var cause = assertThrows(IllegalArgumentException.class,
            () -> StmtTestUtils.parseYangSources(sourceForResource("/bugs/bug7146/foo.yang"))).getCause();
        assertInstanceOf(YangSyntaxErrorException.class, cause);
        assertThat(cause.getMessage(), containsString("extraneous input '#'"));
    }
}
