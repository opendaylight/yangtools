/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.util;
import static org.junit.Assert.assertSame;
import static org.opendaylight.mdsal.binding.spec.util.DataObjectUtils.nullToEmpty;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class DataObjectUtilsTest {
    @Test
    public void nullToEmptyNullTest() {
        assertSame(ImmutableList.of(), nullToEmpty(null));
    }

    @Test
    public void nullToEmptyNonNullTest() {
        final List<Object> list = Collections.singletonList(null);
        assertSame(list, nullToEmpty(list));
    }
}
