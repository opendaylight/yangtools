/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.wood.tree.IFellBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.notification.rev150205.wood.tree.InGrpBuilder;
import org.opendaylight.yangtools.binding.KeyedListNotification;

class YT1912Test {
    @Test
    void instanceNotificationsAreSpecializedToEntryObject() {
        assertInstanceOf(KeyedListNotification.class, new IFellBuilder().build());
        assertInstanceOf(KeyedListNotification.class, new InGrpBuilder().build());
    }
}
