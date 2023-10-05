/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class YT1482Test extends AbstractYangDataTest {
    @Test
    void duplicateNamesAreRejected() throws Exception {
        final var action = REACTOR.newBuild().addSources(IETF_RESTCONF_MODULE, sourceForResource("/yt1482.yang"));

        final var ex = assertThrows(SomeModifiersUnresolvedException.class, action::buildEffective);
        final var cause = assertInstanceOf(SourceException.class, ex.getCause());
        assertThat(cause.getMessage(), startsWith("""
            Error in module 'yt1482': cannot add 'YangDataName[module=QNameModule{ns=yt1482}, name=some]'. Node name \
            collision: 'YangDataName[module=QNameModule{ns=yt1482}, name=some]' already declared at """));
    }
}
