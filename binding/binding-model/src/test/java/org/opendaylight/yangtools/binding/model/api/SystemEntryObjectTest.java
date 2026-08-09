/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class SystemEntryObjectTest {
    @Mock
    private ListEffectiveStatement statement;

    @Test
    @SuppressWarnings("removal")
    void reportsCorrectParameterizedType() {
        final var keyName = TypeName.of("foo", "BarKey");
        final var entryObject = EntryObjectArchetype.of(TypeName.of("foo", "Bar"), statement,
            TypeName.of("foo", "Parent"), keyName, List.of(), List.of(), List.of());
        final var seo = new SystemEntryObject(entryObject);

        assertEquals(new ConcreteTypeImpl(Map.class), seo.getRawType());
        final var params = seo.getActualTypeArguments();
        assertEquals(2, params.size());
        final var first = assertInstanceOf(TypeRef.class, params.getFirst());
        assertSame(keyName, first.name());
        assertSame(entryObject, params.getLast());
    }
}
