/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.DataObject;

class AbstractDataContainerTest {
    private static final class Cont extends AbstractDataContainer<Cont> implements DataObject {
        private final int field;

        Cont(final int field) {
            this.field = field;
        }

        @Override
        public Class<Cont> implementedInterface() {
            return Cont.class;
        }

        @Override
        public int javaHC() {
            return field;
        }

        @Override
        public boolean javaEQ(final Cont obj) {
            return field == obj.field;
        }

        @Override
        public String javaTS() {
            return CodeHelpers.jcTS1(Cont.class, "field", field);
        }
    }

    @Test
    void zeroHashCodeWorks() {
        final var cont = new Cont(0);
        assertEquals(0, cont.hashCode());
        assertEquals(0, cont.hashCode());
    }

    @Test
    void nonZeroHashCodeWorks() {
        final var cont = new Cont(1);
        assertEquals(1, cont.hashCode());
        assertEquals(1, cont.hashCode());
    }

    @Test
    void equalsWorks() {
        final var cont1 = new Cont(1);
        assertNotEquals(cont1, null);
        assertNotEquals(cont1, "");
        assertEquals(cont1, cont1);
        assertEquals(cont1, new Cont(1));
        assertNotEquals(new Cont(1), new Cont(2));
    }

    @Test
    void toStringWorks() {
        assertEquals("Cont{field=42}", new Cont(42).toString());
    }
}
