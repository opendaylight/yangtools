/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LongRangeGeneratorTest {

    @Test
    @Deprecated
    public void convertTest() {
        assertTrue(new LongRangeGenerator().convert(1).equals(Integer.valueOf(1).longValue()));
    }
}
