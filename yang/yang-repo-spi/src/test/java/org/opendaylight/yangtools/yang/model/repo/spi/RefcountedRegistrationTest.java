/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.concepts.Registration;

@ExtendWith(MockitoExtension.class)
class RefcountedRegistrationTest {
    @Mock
    private Registration reg;

    @Test
    void refcountDecTrue() {
        final var ref = new RefcountedRegistration(reg);
        doNothing().when(reg).close();
        assertTrue(ref.decRef());
    }

    @Test
    void refcountIncDecFalse() {
        final var ref = new RefcountedRegistration(reg);
        ref.incRef();
        assertFalse(ref.decRef());
    }

    @Test
    void refcountIncDecTrue() {
        final var ref = new RefcountedRegistration(reg);
        ref.incRef();
        assertFalse(ref.decRef());
        doNothing().when(reg).close();
        assertTrue(ref.decRef());
    }
}
