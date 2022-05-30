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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RefcountedRegistrationTest {
    @Mock
    public SchemaSourceRegistration<?> reg;

    @Test
    public void refcountDecTrue() {
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        doNothing().when(reg).close();
        assertTrue(ref.decRef());
        verify(reg).close();
    }

    @Test
    public void refcountIncDecFalse() {
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        ref.incRef();
        assertFalse(ref.decRef());
        verify(reg, never()).close();
    }

    @Test
    public void refcountIncDecTrue() {
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        ref.incRef();
        assertFalse(ref.decRef());
        doNothing().when(reg).close();
        assertTrue(ref.decRef());
        verify(reg).close();
    }
}
