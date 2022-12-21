/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class Bug7954Test {
    @Test
    void testParsingTheSameModuleTwice() throws Exception {
        final File yang = new File(getClass().getResource("/bugs/bug7954/foo.yang").toURI());

        final Throwable cause = assertThrows(ReactorException.class, () -> StmtTestUtils.parseYangSources(yang, yang))
            .getCause();
        assertInstanceOf(SourceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Module namespace collision: foo-ns."));
    }

    @Test
    void testParsingTheSameSubmoduleTwice() throws Exception {
        final File yang = new File(getClass().getResource("/bugs/bug7954/bar.yang").toURI());
        final File childYang = new File(getClass().getResource("/bugs/bug7954/subbar.yang").toURI());

        final Throwable cause = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources(yang, childYang, childYang)).getCause();
        assertInstanceOf(SourceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Submodule name collision: subbar."));
    }
}
