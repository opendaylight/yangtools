/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import org.junit.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataConstants;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class YT1338Test extends AbstractYangDataTest {
    @Test
    public void testAddedLeaves() throws ReactorException {
        final var module = REACTOR.newBuild().addSources(IETF_RESTCONF_MODULE, sourceForResource("/yt1338/foo.yang"))
            .buildEffective()
            .findModuleStatement(YangDataConstants.RFC8040_MODULE)
            .orElseThrow();
    }
}
