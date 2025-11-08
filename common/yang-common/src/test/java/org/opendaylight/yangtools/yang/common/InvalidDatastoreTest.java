/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InvalidDatastoreTest {
    @Test
    void defaultConstructorThrows() {
        assertThrows(UnsupportedOperationException.class, InvalidDatastore::new);
    }

    @Test
    void hasOnlyDefaultConstructor() {
        final var defaultCtor = assertDoesNotThrow(() -> InvalidDatastore.class.getDeclaredConstructor());
        assertThat(InvalidDatastore.class.getDeclaredConstructors()).containsExactly(defaultCtor);
    }
}
