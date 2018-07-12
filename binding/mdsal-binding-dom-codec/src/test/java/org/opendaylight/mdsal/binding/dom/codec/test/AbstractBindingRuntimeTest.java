/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class AbstractBindingRuntimeTest {

    private static SchemaContext schemaContext;
    private static BindingRuntimeContext runtimeContext;

    @BeforeClass
    public static void beforeClass() {
        ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        schemaContext = ctx.tryToCreateSchemaContext().get();
        runtimeContext = BindingRuntimeContext.create(ctx, schemaContext);
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
        runtimeContext = null;
    }

    public static final SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public static final BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
