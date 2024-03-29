/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 * Common interface for common container-like constructs. This includes {@link ContainerSchemaNode},
 * {@link InputSchemaNode}, {@link OutputSchemaNode} and, for legacy reasons, {@link SchemaContext}.
 */
public interface ContainerLike extends DataNodeContainer,
        AugmentationTarget, DataSchemaNode, NotificationNodeContainer, ActionNodeContainer, MustConstraintAware {

}
