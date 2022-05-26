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
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolverTest;

public class YT1443Test {

    @Test
    public void buildEffectiveModelTest() throws Exception {
        YangTextSchemaSource bug = YangTextSchemaSource.forPath(Path.of(
                YangTextSchemaContextResolverTest.class.getResource("/YT1443/test-bug.yang").toURI()));
        YangTextSchemaSource test = YangTextSchemaSource.forPath(Path.of(
                YangTextSchemaContextResolverTest.class.getResource("/YT1443/extension-model.yang").toURI()));

        DefaultYangParserFactory parserFactory = new DefaultYangParserFactory();
        YangParser parser = parserFactory.createParser();
        parser.addSources(bug, test);

        assertNotNull(parser.buildEffectiveModel());

    }
}
