/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class YT1385Test extends AbstractYangTest {
    @Test
    public void testSameModuleWrongUnique() {
        final var ex = assertInferenceException(startsWith("Yang model processing phase EFFECTIVE_MODEL failed [at "),
            "/bugs/YT1385/foo.yang");
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Following components of unique statement argument refer to "
            + "non-existent nodes: [Descendant{qnames=[(foo)bar]}] [at "));
    }
}
