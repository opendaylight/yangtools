/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TypeTest {
    @Test
    void testCreateNewReferencedType() {
        final var refType = TypeRef.of(JavaTypeName.create("org.opendaylight.yangtools.test", "RefTypeTest"));
        assertEquals("RefTypeTest", refType.simpleName());
    }

    @Test
    void testToStringMethod() {
        final var refType = TypeRef.of(JavaTypeName.create("org.opendaylight.yangtools.test", "RefTypeTest"));
        assertEquals("TypeRef{name=org.opendaylight.yangtools.test.RefTypeTest}", refType.toString());
    }
}
