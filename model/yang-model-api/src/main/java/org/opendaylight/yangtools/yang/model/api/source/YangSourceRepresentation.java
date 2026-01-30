/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A YANG {@link SourceRepresentation}.
 */
@NonNullByDefault
public non-sealed interface YangSourceRepresentation extends SourceRepresentation {
    @Override
    Class<? extends YangSourceRepresentation> getType();

    @Override
    default Class<YangTextSource> textRepresentation() {
        return YangTextSource.class;
    }
}
