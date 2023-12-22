/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A factory for concrete {@link NormalizedNode.Builder}s.
 */
@NonNullByDefault
public interface NormalizedNodeBuilderFactory {

    <T> AnydataNode.Builder<T> newAnydataBuilder(Class<T> objectModel);

    ContainerNode.Builder newContainerBuilder();

    ContainerNode.Builder newContainerBuilder(int sizeHint);

    SystemMapNode.Builder newSystemMapBuilder();

    SystemMapNode.Builder newSystenMapBuilder(int sizeHint);

    UserMapNode.Builder newUserMapBuilder();

    UserMapNode.Builder newUserMapBuilder(int sizeHint);
}
