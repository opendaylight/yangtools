/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;

class Bug6131Test extends AbstractYangTest {
    @Test
    void test() {
        final var ex = assertThrows(ExtractorException.class, () -> TestUtils.loadModules("/bugs/bug6131"));
        assertEquals("Root of parsed AST must be either module or submodule [at foo:1:1]", ex.getMessage());
    }
}