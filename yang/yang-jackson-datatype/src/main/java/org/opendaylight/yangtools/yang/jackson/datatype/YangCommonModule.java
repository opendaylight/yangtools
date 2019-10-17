/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class YangCommonModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    @Override
    public String getModuleName() {
        return "YangCommonModule";
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(final SetupContext context) {
        context.addDeserializers(new YangCommonDeserializers());
        context.addSerializers(new YangCommonSerializers());
    }
}
