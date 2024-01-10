/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArgumentsExtractorVerifierTest {
    @Mock
    private List<String> mockedList;

    @Test
    void test() {
        doReturn(Boolean.TRUE).when(mockedList).add(any(String.class));
        final var argument = "something";
        mockedList.add(argument);
        // retrieve argument
        final var argumentsExtractorVerifier = new ArgumentsExtractorVerifier();
        verify(mockedList, argumentsExtractorVerifier).add(any(String.class));
        assertArrayEquals(new Object[]{ argument }, argumentsExtractorVerifier.getArguments());
    }
}
