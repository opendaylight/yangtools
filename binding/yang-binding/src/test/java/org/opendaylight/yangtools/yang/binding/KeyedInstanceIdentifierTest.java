/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class KeyedInstanceIdentifierTest {

    @Test
    public void basicTest() throws Exception {
        final Identifier key = mock(Identifier.class);
        final KeyedInstanceIdentifier keyedInstanceIdentifier =
                new KeyedInstanceIdentifier(Identifiable.class, ImmutableList.of(), false, 0, key);

        assertEquals(key, keyedInstanceIdentifier.getKey());

        assertFalse(keyedInstanceIdentifier.fastNonEqual(keyedInstanceIdentifier.builder().build()));
        assertTrue(new KeyedInstanceIdentifier(Identifiable.class, ImmutableList.of(), false, 0, null)
                .fastNonEqual(keyedInstanceIdentifier.builder().build()));
    }
}