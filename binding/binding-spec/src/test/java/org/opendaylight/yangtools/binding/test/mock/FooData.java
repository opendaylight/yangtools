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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;

public interface FooData extends DataRoot<FooData> {
    @NonNullByDefault
    RootMeta<FooData> META = new RootMeta<>(FooData.class,
        // Note: this usually supplied via YangModuleInfoProvider reference
        new YangModuleInfo() {
            @Override
            public InputStream openYangTextStream() {
                throw new UnsupportedOperationException();
            }

            @Override
            public QName getName() {
                throw new UnsupportedOperationException();
            }
        },
        // Note: this usually supplied from YangModuleInfoImpl singleton
        new UnsafeAccess() {
            @Override
            public <T extends ScalarTypeObject<V>, V>
                    @Nullable UnsafeScalarTypeObjectFactory<T, V> lookupUnsafeScalarTypeObjectFactory(
                        final Class<T> typeClass) {
                throw new UnsupportedOperationException();
            }
        });

    @Override
    default Class<FooData> implementedInterface() {
        return FooData.class;
    }

    @Override
    default RootMeta<FooData> meta() {
        return META;
    }
}
