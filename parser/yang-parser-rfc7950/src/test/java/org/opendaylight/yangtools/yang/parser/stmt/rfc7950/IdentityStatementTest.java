/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class IdentityStatementTest extends AbstractYangTest {
    @Test
    void testMultipleBaseIdentities() {
        final var context = assertEffectiveModel("/rfc7950/identity-stmt/foo.yang");

        final var foo = context.findModule("foo", Revision.of("2016-12-21")).orElseThrow();
        for (var identity : foo.getIdentities()) {
            if ("derived-id".equals(identity.getQName().getLocalName())) {
                final var baseIdentities = identity.getBaseIdentities();
                assertEquals(3, baseIdentities.size());
            }
        }
    }

    @Test
    void testInvalidYang10() {
        assertInvalidSubstatementException(
            startsWith("identity statement allows at most 1 base substatement(s), 3 present [at "),
            "/rfc7950/identity-stmt/foo10.yang");
    }
}
