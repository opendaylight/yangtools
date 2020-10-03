/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.mockito.configuration;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ArgumentsExtractorVerifierTest {
    @Mock
    List<String> mockedList;

    @Test
    public void test() {
        doReturn(Boolean.TRUE).when(this.mockedList).add(any(String.class));
        final String argument = "something";
        this.mockedList.add(argument);
        // retrieve argument
        final ArgumentsExtractorVerifier argumentsExtractorVerifier = new ArgumentsExtractorVerifier();
        verify(this.mockedList, argumentsExtractorVerifier).add(any(String.class));
        assertArrayEquals(new Object[] { argument }, argumentsExtractorVerifier.getArguments());
    }
}
