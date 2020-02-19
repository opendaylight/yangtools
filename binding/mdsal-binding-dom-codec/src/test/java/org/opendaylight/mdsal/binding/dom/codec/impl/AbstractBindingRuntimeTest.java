/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;

public abstract class AbstractBindingRuntimeTest {
    private static BindingRuntimeContext runtimeContext;

    @BeforeClass
    public static void beforeClass() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext(new DefaultBindingRuntimeGenerator());
    }

    @AfterClass
    public static void afterClass() {
        runtimeContext = null;
    }

    public static final BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
