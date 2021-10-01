/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug8831Test {
    @Test
    public void test() throws Exception {
        assertNotNull(TestUtils.parseYangSource("/bugs/bug8831/valid"));
    }

    @Test
    public void invalidModelsTest() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.parseYangSource("/bugs/bug8831/invalid/inv-model.yang"));
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), containsString("has default value 'any' marked with an if-feature statement"));
    }

    @Test
    public void invalidModelsTest2() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.parseYangSource("/bugs/bug8831/invalid/inv-model2.yang"));
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), containsString("has default value 'any' marked with an if-feature statement"));
    }
}
