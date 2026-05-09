/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.test.mock;

import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.lib.UnsafeAccessSupport;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;

public interface FooData extends DataRoot<FooData> {
    @NonNullByDefault
    RootMeta<FooData> META = new RootMeta<>(FooData.class,
        // Note: this usually supplied via YangModuleInfoProvider reference
        new YangModuleInfo() {
            @Override
            public QName name() {
                throw new UnsupportedOperationException();
            }

            @Override
            public InputStream openYangTextStream() {
                throw new UnsupportedOperationException();
            }
        },
        UnsafeAccessSupport.of(Naming.PACKAGE_PREFIX + "mock", FooData.class.getModule()).access());

    @Override
    default Class<FooData> implementedInterface() {
        return FooData.class;
    }
}
