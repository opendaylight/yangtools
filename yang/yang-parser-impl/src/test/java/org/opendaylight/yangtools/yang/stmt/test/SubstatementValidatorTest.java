/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class SubstatementValidatorTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void noException() throws URISyntaxException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment").toURI());
        assertNotNull(modules);
    }

    @Test
    public void undesirableElementException() throws URISyntaxException, ReactorException {
        expectedEx.expect(YangValidationException.class);
        expectedEx.expectMessage("TYPE is not valid for REVISION");

        Set<Module> modules = TestUtils.loadModules(getClass().getResource
                ("/substatement-validator/undesirable-element")
                .toURI());
    }

    @Test
    public void maximalElementCountException() throws URISyntaxException, ReactorException {
        expectedEx.expect(YangValidationException.class);
        expectedEx.expectMessage("Maximal count for DESCRIPTION is 1, detected 2");

        Set<Module> modules = TestUtils.loadModules(getClass().getResource
                ("/substatement-validator/maximal-element")
                .toURI());
    }

    @Test
    public void missingElementException() throws URISyntaxException, ReactorException {
        expectedEx.expect(YangValidationException.class);
        expectedEx.expectMessage("IMPORT Is missing PREFIX. Minimal count is 1");

        Set<Module> modules = TestUtils.loadModules(getClass().getResource
                ("/substatement-validator/missing-element")
                .toURI());
    }

    @Test
    public void emptyElementException() throws URISyntaxException, ReactorException {
        expectedEx.expect(YangValidationException.class);
        expectedEx.expectMessage("REFINE Must contain atleast 1 element");

        Set<Module> modules = TestUtils.loadModules(getClass().getResource
                ("/substatement-validator/empty-element")
                .toURI());
    }
}
