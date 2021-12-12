/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reactor's view of significant semantic history of a particular statement.
 */
@Beta
@NonNullByDefault
// FIXME: YANGTOOLS-1150: this should live in yang-reactor-api
public interface CopyHistory {
    /**
     * Return the last copy operation in this history.
     *
     * @return Last {@link CopyType}
     */
    CopyType getLastOperation();
}
