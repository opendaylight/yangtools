/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6871Test extends AbstractYangTest {
    @Test
    void testValidYang11Model() {
        final var foo = assertEffectiveModel("/rfc7950/bug6871/foo.yang")
            .findModule("foo", Revision.of("2016-12-14")).orElseThrow();

        final var notifications = foo.getNotifications();
        assertEquals(1, notifications.size());
        final var myNotification = notifications.iterator().next();
        var mustConstraints = myNotification.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final var rpcs = foo.getRpcs();
        assertEquals(1, rpcs.size());
        final var myRpc = rpcs.iterator().next();

        final var input = myRpc.getInput();
        assertNotNull(input);
        mustConstraints = input.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final var output = myRpc.getOutput();
        assertNotNull(output);
        mustConstraints = output.getMustConstraints();
        assertEquals(2, mustConstraints.size());
    }

    @Test
    void testInvalidYang10Model() {
        assertThat(assertInvalidSubstatementException("/rfc7950/bug6871/foo10.yang").getMessage())
            .startsWith("statement notification does not allow must substatements: 2 present [at ");
        assertThat(assertInvalidSubstatementException("/rfc7950/bug6871/bar10.yang").getMessage())
            .startsWith("statement input does not allow must substatements: 2 present [at ");
        assertThat(assertInvalidSubstatementException("/rfc7950/bug6871/baz10.yang").getMessage())
            .startsWith("statement output does not allow must substatements: 2 present [at ");
    }
}
