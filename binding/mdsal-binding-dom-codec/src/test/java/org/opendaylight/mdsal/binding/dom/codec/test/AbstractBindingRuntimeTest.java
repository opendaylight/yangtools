/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import org.junit.Before;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class AbstractBindingRuntimeTest {

    private SchemaContext schemaContext;
    private BindingRuntimeContext runtimeContext;

    @Before
    public void setup() {
        ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(BindingReflections.loadModuleInfos());
        schemaContext = ctx.tryToCreateSchemaContext().get();
        runtimeContext = BindingRuntimeContext.create(ctx, schemaContext);

    }

    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
