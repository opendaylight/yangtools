/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug4650Test {
    @Test
    public void test() throws SourceException, ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
        try {
            StmtTestUtils.parseYangSource("/bugs/bug4650/foo.yang");
            fail("Test should fail due to invalid default value in yang model.");
        } catch (final IllegalArgumentException e) {
            // DefaultViolatesConstraintsException
        }
    }

    @Test
    public void positiveTest() throws SourceException, ReactorException, URISyntaxException, IOException, YangSyntaxErrorException {
            StmtTestUtils.parseYangSource("/bugs/bug4650/foo-positive.yang");
    }
}
