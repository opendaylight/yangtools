/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class YT1042Test {
    @Test
    public void testSubmoduleConflict() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/bugs/YT1042");
            fail("Sechema assembly should have failed");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertThat(cause, isA(SourceException.class));
            assertThat(cause.getMessage(),
                startsWith("Cannot add data tree child with name (foo)foo, a conflicting child already exists"));
        }
    }
}
