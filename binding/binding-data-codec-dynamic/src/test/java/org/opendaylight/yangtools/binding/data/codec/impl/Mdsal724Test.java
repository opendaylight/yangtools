/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.OutOfPixieDustNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class Mdsal724Test {
    @Test
    void testNotificationInstanceIdentifier() {
        // An InstanceIdentifier pointing at a notification, unsafe to create
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> InstanceIdentifier.create((Class) OutOfPixieDustNotification.class));
        assertEquals("interface org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test"
            + ".bi.ba.notification.rev150205.OutOfPixieDustNotification is not a valid path argument", ex.getMessage());
    }

    @Test
    void testRpcInputInstanceIdentifier() {
        // An InstanceIdentifier pointing at a notification, unsafe to create
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> InstanceIdentifier.create((Class) KnockKnockInput.class));
        assertEquals("interface org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock"
            + ".rev180723.KnockKnockInput is not a valid path argument", ex.getMessage());
    }
}
