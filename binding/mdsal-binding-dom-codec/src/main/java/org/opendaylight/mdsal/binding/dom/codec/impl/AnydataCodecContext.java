/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.yang.binding.OpaqueData;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;

final class AnydataCodecContext<T extends OpaqueObject<T>> extends AbstractOpaqueCodecContext<T> {
    AnydataCodecContext(final AnydataSchemaNode schema, final String getterName, final Class<T> bindingClass,
            final BindingClassLoader loader) {
        super(schema, getterName, bindingClass, loader);
    }

    @Override
    AnydataNode<?> serializedData(final OpaqueData<?> opaqueData) {
        return buildAnydata(opaqueData);
    }

    private <M> @NonNull AnydataNode<M> buildAnydata(final OpaqueData<M> opaqueData) {
        return ImmutableNodes.newAnydataBuilder(opaqueData.getObjectModel())
            .withNodeIdentifier(getDomPathArgument())
            .withValue(opaqueData.getData())
            .build();
    }
}