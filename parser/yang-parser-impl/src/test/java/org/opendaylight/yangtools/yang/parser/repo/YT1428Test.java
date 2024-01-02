/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

public class YT1428Test extends AbstractSchemaRepositoryTest {
    @Test
    void testDeviateSourceReported() {
        final var ex = assertExecutionException(null, "/yt1428/orig.yang", "/yt1428/deviate.yang");
        assertEquals(new SourceIdentifier("deviate"),
            assertInstanceOf(SchemaResolutionException.class, ex.getCause()).sourceId());
    }
}
