/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;

public class YT859Test {
    @Test
    public void testFindDataSchemaNode() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/YT859/", ImmutableSet.of(),
            StatementParserMode.DEFAULT_MODE);
        assertEquals(3, context.getModules().size());
    }
}
