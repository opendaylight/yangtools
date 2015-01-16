/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ReplaceAllIllegalCharsTest {

    @Test
    public void unicodeCharReplaceTest() {
        String inputString = "abcu\\uuuuu\\uuua\\u\\\\uabc\\\\uuuu\\\\\\uuuu\\\\\\\\uuuu///uu/u/u/u/u/u/u";

        StringBuilder sb = new StringBuilder(inputString);
        String resultString = BindingGeneratorImpl.replaceAllIllegalChars(sb);

        String expectedString = "abcu\\\\uuuuu\\\\uuua\\\\u\\\\uabc\\\\uuuu\\\\uuuu\\\\uuuu///uu/u/u/u/u/u/u";
        assertEquals(expectedString, resultString);
    }

}
