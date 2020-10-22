/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PotentialSchemaSourceTest {
    private interface TestSchemaSourceRepresentation extends SchemaSourceRepresentation {

    }

    @Mock
    public SourceIdentifier sourceIdentifier;
    public PotentialSchemaSource<TestSchemaSourceRepresentation> source;
    public PotentialSchemaSource<TestSchemaSourceRepresentation> same;

    @Before
    public void before() {
        source = PotentialSchemaSource.create(sourceIdentifier, TestSchemaSourceRepresentation.class,
            PotentialSchemaSource.Costs.LOCAL_IO.getValue());
        same = PotentialSchemaSource.create(source.getSourceIdentifier(), source.getRepresentation(),
            source.getCost());
    }

    @Test
    public void testNegativeCost() {
        assertThrows(IllegalArgumentException.class,
            () -> PotentialSchemaSource.create(sourceIdentifier, TestSchemaSourceRepresentation.class, -1));
    }

    @Test
    public void testMethods() {
        assertEquals(PotentialSchemaSource.Costs.LOCAL_IO.getValue(), source.getCost());
        assertSame(sourceIdentifier, source.getSourceIdentifier());
        assertSame(TestSchemaSourceRepresentation.class, source.getRepresentation());
        assertEquals(same.hashCode(), source.hashCode());
        assertFalse(source.equals(null));
        assertTrue(source.equals(source));
        assertTrue(source.equals(same));
    }

    @Test
    public void testIntern() {
        assertSame(source, source.cachedReference());
        assertSame(source, same.cachedReference());
    }
}
