/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.OutOfPixieDustNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;

class Mdsal724Test {
    @Test
    void testNotificationInstanceIdentifier() {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final DataObjectStep<?> step = DataObjectStep.of((Class) OutOfPixieDustNotification.class);
        final var steps = List.of(step);

        // A DataObjectReference pointing at a notification, unsafe to create
        final var ex = assertThrows(IllegalArgumentException.class, () -> DataObjectReference.ofUnsafeSteps(steps));
        assertEquals("interface org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test"
            + ".bi.ba.notification.rev150205.OutOfPixieDustNotification is not a valid path argument", ex.getMessage());
    }

    @Test
    void testRpcInputInstanceIdentifier() {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final DataObjectStep<?> step = DataObjectStep.of((Class) KnockKnockInput.class);
        final var steps = List.of(step);

        // A DataObjectReference pointing at a notification, unsafe to create
        final var ex = assertThrows(IllegalArgumentException.class, () -> DataObjectReference.ofUnsafeSteps(steps));
        assertEquals("interface org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock"
            + ".rev180723.KnockKnockInput is not a valid path argument", ex.getMessage());
    }
}
