/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * A {@link Module} providing mapping for {@link Uint8} and similar types.
 */
@MetaInfServices(value = Module.class)
public final class YangModule extends Module {
    @Override
    public String getModuleName() {
        return "YangModule";
    }

    @Override
    public Version version() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setupModule(final Module.SetupContext context) {
        context.addDeserializers(new YangDeserializers());
        context.addSerializers(new YangSerializers());
    }
}
