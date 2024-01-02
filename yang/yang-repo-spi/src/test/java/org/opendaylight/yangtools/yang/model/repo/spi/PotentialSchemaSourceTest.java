/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangSchemaSourceRepresentation;

@ExtendWith(MockitoExtension.class)
class PotentialSchemaSourceTest {
    private interface TestSchemaSourceRepresentation extends YangSchemaSourceRepresentation {
        @Override
        default Class<TestSchemaSourceRepresentation> getType() {
            return TestSchemaSourceRepresentation.class;
        }
    }

    public final SourceIdentifier sourceIdentifier = new SourceIdentifier("foo");
    @SuppressWarnings("exports")
    public PotentialSchemaSource<TestSchemaSourceRepresentation> source;
    @SuppressWarnings("exports")
    public PotentialSchemaSource<TestSchemaSourceRepresentation> same;

    @BeforeEach
    void before() {
        source = PotentialSchemaSource.create(sourceIdentifier, TestSchemaSourceRepresentation.class,
            PotentialSchemaSource.Costs.LOCAL_IO.getValue());
        same = PotentialSchemaSource.create(source.getSourceIdentifier(), source.getRepresentation(),
            source.getCost());
    }

    @Test
    void testNegativeCost() {
        assertThrows(IllegalArgumentException.class,
            () -> PotentialSchemaSource.create(sourceIdentifier, TestSchemaSourceRepresentation.class, -1));
    }

    @Test
    void testMethods() {
        assertEquals(PotentialSchemaSource.Costs.LOCAL_IO.getValue(), source.getCost());
        assertSame(sourceIdentifier, source.getSourceIdentifier());
        assertSame(TestSchemaSourceRepresentation.class, source.getRepresentation());
        assertEquals(same.hashCode(), source.hashCode());
        assertNotEquals(null, source);
        assertEquals(source, source);
        assertEquals(source, same);
    }

    @Test
    void testIntern() {
        assertSame(source, source.cachedReference());
        assertSame(source, same.cachedReference());
    }
}
