/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;

public class YT1428Test extends AbstractSchemaRepositoryTest {
    @Test
    public void testDeviateSourceReported() {
        final var cause = assertExecutionException(null, "/yt1428/orig.yang", "/yt1428/deviate.yang").getCause();
        assertThat(cause, instanceOf(SchemaResolutionException.class));
        assertEquals(RevisionSourceIdentifier.create("deviate"), ((SchemaResolutionException) cause).getFailedSource());
    }
}
