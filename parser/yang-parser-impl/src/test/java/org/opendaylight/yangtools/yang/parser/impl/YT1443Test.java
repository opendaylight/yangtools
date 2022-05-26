/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class YT1443Test {

    @Test
    public void buildEffectiveModelTest() throws Exception {
        final var bugSource = YangTextSchemaSource.forPath(Path.of(
                getClass().getResource("/YT1443/test-bug.yang").toURI()));
        final var extensionSource = YangTextSchemaSource.forPath(Path.of(
                getClass().getResource("/YT1443/extension-model.yang").toURI()));
        final var parser = new DefaultYangParserFactory().createParser();

        parser.addSources(bugSource, extensionSource);
        assertNotNull(parser.buildEffectiveModel());
    }
}
