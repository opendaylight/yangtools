/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource.forResource;

import java.io.IOException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug6150Test {

    private static final YangTextSchemaSource TARGET = forResource("/bugs/bug6150/target.yang");
    private static final YangTextSchemaSource AUGMENT_FIRST = forResource("/bugs/bug6150/aug-first.yang");
    private static final YangTextSchemaSource AUGMENT_SECOND = forResource("/bugs/bug6150/aug-second.yang");

    @Test
    public void effectiveAugmentFirstTest() throws ReactorException, YangSyntaxErrorException, IOException {
        final SchemaContext result = TestUtils.defaultParser().addSources(TARGET, AUGMENT_FIRST).buildSchemaContext();
        assertNotNull(result);
    }

    @Test
    public void effectiveAugmentSecondTest() throws ReactorException, YangSyntaxErrorException, IOException {
        final SchemaContext result = TestUtils.defaultParser().addSources(TARGET, AUGMENT_SECOND).buildSchemaContext();
        assertNotNull(result);
    }

    @Test
    public void effectiveAugmentBothTest() throws ReactorException, YangSyntaxErrorException, IOException {
        final SchemaContext result = TestUtils.defaultParser().addSources(TARGET, AUGMENT_FIRST, AUGMENT_SECOND)
                .buildSchemaContext();
        assertNotNull(result);
    }
}
