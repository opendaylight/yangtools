/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;

/**
 * A {@link DeclarationReference} to a position within a some text document.
 */
@Beta
public interface DeclarationInText extends DeclarationReference {
    /**
     * Return the line where the declaration starts.
     *
     * @return A positive line number.
     */
    int startLine();

    /**
     * Return the column where the declaration starts.
     *
     * @return A positive column number.
     */
    int startColumn();
}
