/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YangOrganizationTreeTest {
    @Test
    void testNotification() {
        final var notifModule = YangParserTestUtils.parseYangResource("/bugs/bug2444/yang/notification.yang")
            .getModuleStatements().values().iterator().next();

        assertEquals("""
            module: notification
              +--rw r
              |  +---n n2
              +--rw l* [id]
                 +--rw id    int16
                 +---n n3

              augment /r:
                +---u grp
                +---n n4

              notifications:
                x---n n1
                   +--rw c

              grouping grp:
                +---n n5
            """, DeclaredStatementFormatter.defaultInstance().toYangOrganizationTree(notifModule,
                notifModule.getDeclared(), 2).toString());
    }

    @Test
    void testNACMModule() {
        final var schema = YangParserTestUtils.parseYangResourceDirectory("/yang");
        final var nacmModule = schema.findModule("ietf-netconf-acm", Revision.of("2018-02-14"))
            .orElseThrow()
            .asEffectiveStatement();

        assertEquals("""
            module: ietf-netconf-acm
              +--rw nacm
                 +--rw enable-nacm?              boolean
                 +--rw read-default?             action-type
                 +--rw write-default?            action-type
                 +--rw exec-default?             action-type
                 +--rw enable-external-groups?   boolean
                 +--ro denied-operations         yang:zero-based-counter32
                 +--ro denied-data-writes        yang:zero-based-counter32
                 +--ro denied-notifications      yang:zero-based-counter32
                 +--rw groups
                 |  +--rw group* [name]
                 |     +--rw name         group-name-type
                 |     +--rw user-name*   user-name-type
                 +--rw rule-list* [name]
                    +--rw name     string
                    +--rw group*   union
                    +--rw rule* [name]
                       +--rw name                 string
                       +--rw module-name?         union
                       +--rw (rule-type)?
                       |  +--:(protocol-operation)
                       |  |  +--rw rpc-name?   union
                       |  +--:(notification)
                       |  |  +--rw notification-name?   union
                       |  +--:(data-node)
                       |     +--rw path    node-instance-identifier
                       +--rw access-operations?   union
                       +--rw action               action-type
                       +--rw comment?             string
            """, DeclaredStatementFormatter.defaultInstance().toYangOrganizationTree(nacmModule,
                nacmModule.getDeclared(), 8).toString());
    }
}
