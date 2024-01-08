/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.MandatoryLeafEnforcer;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public sealed class ContainerContext extends AbstractCompositeContext {
    public static final class WithMandatory extends ContainerContext
            implements DataSchemaContext.MandatoryEnforcementPoint {
        private final @Nullable MandatoryLeafEnforcer configEnforcer;
        private final @Nullable MandatoryLeafEnforcer operEnforcer;

        public WithMandatory(final ContainerLike schema, final @Nullable MandatoryLeafEnforcer configEnforcer,
                final @Nullable MandatoryLeafEnforcer operEnforcer) {
            super(schema);
            this.configEnforcer = configEnforcer;
            this.operEnforcer = operEnforcer;
        }

        @Override
        public void enforceMandatory(final boolean configFalse, final NormalizedNode data) {
            final var enforcer = configFalse ? operEnforcer : configEnforcer;
            if (enforcer != null) {
                enforcer.enforceOnData(data);
            }
        }
    }

    private ContainerContext(final ContainerLike schema) {
        super(NodeIdentifier.create(schema.getQName()), schema, schema);
    }

    public static @NonNull ContainerContext of(final ContainerLike schema) {
        if (schema instanceof ContainerSchemaNode container && container.isPresenceContainer()) {
            final var configEnforcer = MandatoryLeafEnforcer.forContainer(container, false);
            final var operEnforcer = MandatoryLeafEnforcer.forContainer(container, true);
            if (configEnforcer != null || operEnforcer != null) {
                return new WithMandatory(container, configEnforcer, operEnforcer);
            }
        }
        return new ContainerContext(schema);
    }
}
