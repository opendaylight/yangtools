/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;

@RunWith(MockitoJUnitRunner.class)
public class RefcountedRegistrationTest {
    @Mock
    public SchemaSourceRegistration<?> reg;

    @Before
    public void before() {
        doNothing().when(reg).close();
    }

    @Test
    public void refcountDecTrue() {
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        assertTrue(ref.decRef());
        verify(reg, times(1)).close();
    }

    @Test
    public void refcountIncDecFalse() {
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        ref.incRef();
        assertFalse(ref.decRef());
        verify(reg, times(0)).close();
    }

    @Test
    public void refcountIncDecTrue() {
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        ref.incRef();
        assertFalse(ref.decRef());
        assertTrue(ref.decRef());
        verify(reg, times(1)).close();
    }
}
