/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.junit.Test;

public class ReMatchXPathFunctionTest {

    @Test
    public void testRematchFunction() throws Exception {
        // re-match() uses regex processing from yang-parser-impl which has been thoroughly tested within
        // the Bug5410Test unit test class, so here is just a basic test
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function rematchFunction = yangFunctionContext.getFunction(null, null, "re-match");

        final Context mockedContext = mock(Context.class);

        boolean rematchResult = (boolean) rematchFunction.call(mockedContext, ImmutableList.of("abbc", "[abc]{1,4}"));
        assertTrue(rematchResult);
        rematchResult = (boolean) rematchFunction.call(mockedContext, ImmutableList.of("abbcc", "[abc]{1,4}"));
        assertFalse(rematchResult);
    }

    @Test
    public void shouldFailOnInvalidNumberOfArguments() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function rematchFunction = yangFunctionContext.getFunction(null, null, "re-match");

        final Context mockedContext = mock(Context.class);

        try {
            rematchFunction.call(mockedContext, ImmutableList.of("abbc", "[abc]{1,4}", "should not be here"));
            fail("Function call should have failed on invalid number of arguments.");
        } catch (final FunctionCallException ex) {
            assertEquals("re-match() takes two arguments: string subject, string pattern.", ex.getMessage());
        }
    }

    @Test
    public void shouldFailOnInvalidTypeOfArgument() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function rematchFunction = yangFunctionContext.getFunction(null, null, "re-match");

        final Context mockedContext = mock(Context.class);

        try {
            rematchFunction.call(mockedContext, ImmutableList.of(100, "[abc]{1,4}"));
            fail("Function call should have failed on invalid type of the subject argument.");
        } catch (final FunctionCallException ex) {
            assertEquals("First argument of re-match() should be a String.", ex.getMessage());
        }

        try {
            rematchFunction.call(mockedContext, ImmutableList.of("abbc", 100));
            fail("Function call should have failed on invalid type of the pattern argument.");
        } catch (final FunctionCallException ex) {
            assertEquals("Second argument of re-match() should be a String.", ex.getMessage());
        }
    }
}