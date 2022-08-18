/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class NamespaceTest {
    @Test
    public void testNamespaces() {
        // Touch behaviors
        // FIXME: add more checks/split this up when behaviours are testable
        assertNotNull(StatementDefinitions.BEHAVIOUR);
    }
}
