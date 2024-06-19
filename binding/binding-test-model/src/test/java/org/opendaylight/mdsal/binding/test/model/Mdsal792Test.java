/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont.VlanId.Enumeration;

public class Mdsal792Test {
    @Test
    public void testRejectNulls() {
        assertThrows(NullPointerException.class, () -> new VlanId((Enumeration) null));
    }
}
