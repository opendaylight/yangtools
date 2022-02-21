/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.OutOfPixieDustNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Mdsal724Test extends AbstractBindingCodecTest {
    @Test
    public void testNotificationInstanceIdentifier() {
        // An InstanceIdentifier pointing at a notification, unsafe to create
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final var iid = InstanceIdentifier.create((Class) OutOfPixieDustNotification.class);

        final var ex = assertThrows(IllegalArgumentException.class, () -> codecContext.toYangInstanceIdentifier(iid));
        assertThat(ex.getMessage(), startsWith("Supplied class must not be a notification ("));
    }

    @Test
    public void testRpcInputInstanceIdentifier() {
        // An InstanceIdentifier pointing at a notification, unsafe to create
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final var iid = InstanceIdentifier.create((Class) KnockKnockInput.class);
        final var ex = assertThrows(IncorrectNestingException.class, () -> codecContext.toYangInstanceIdentifier(iid));
        assertEquals("interface org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock."
            + "rev180723.KnockKnockInput is not top-level item.", ex.getMessage());
    }
}
