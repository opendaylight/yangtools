/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;

abstract class AbstractBindingRuntimeTest {
    private static BindingRuntimeContext runtimeContext;

    @BeforeAll
    static final void setupRuntimeContext() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
    }

    @AfterAll
    static final void tearDownRuntimeContext() {
        runtimeContext = null;
    }

    static final BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
