/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class Bug6150Test {

    private static final StatementStreamSource TARGET = sourceForResource("/bugs/bug6150/target.yang");
    private static final StatementStreamSource AUGMENT_FIRST = sourceForResource("/bugs/bug6150/aug-first.yang");
    private static final StatementStreamSource AUGMENT_SECOND = sourceForResource("/bugs/bug6150/aug-second.yang");

    @Test
    public void effectiveAugmentFirstTest() throws ReactorException {
        final SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(TARGET, AUGMENT_FIRST)
                .buildEffective();
        assertNotNull(result);
    }

    @Test
    public void effectiveAugmentSecondTest() throws ReactorException {
        final SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(TARGET, AUGMENT_SECOND)
                .buildEffective();
        assertNotNull(result);
    }

    @Test
    public void effectiveAugmentBothTest() throws ReactorException {
        final SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(TARGET, AUGMENT_FIRST, AUGMENT_SECOND)
                .buildEffective();
        assertNotNull(result);
    }
}
