/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode.Composite;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public abstract sealed class AbstractCompositeContextNode extends AbstractDataSchemaContextNode implements Composite
        permits AbstractMixinContextNode, DataContainerContextNode {
    AbstractCompositeContextNode(final NodeIdentifier pathStep, final DataSchemaNode schema) {
        super(pathStep, schema);
    }

    @Override
    public final DataSchemaContextNode enterChild(final SchemaInferenceStack stack, final QName child) {
        return enterChild(requireNonNull(child), requireNonNull(stack));
    }

    @Override
    public final DataSchemaContextNode enterChild(final SchemaInferenceStack stack,
            final PathArgument child) {
        return enterChild(requireNonNull(child), requireNonNull(stack));
    }

    abstract @Nullable DataSchemaContextNode enterChild(@NonNull QName child, @NonNull SchemaInferenceStack stack);

    abstract @Nullable DataSchemaContextNode enterChild(@NonNull PathArgument child,
        @NonNull SchemaInferenceStack stack);
}