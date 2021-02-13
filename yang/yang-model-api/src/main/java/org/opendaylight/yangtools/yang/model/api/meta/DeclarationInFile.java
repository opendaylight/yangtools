/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link DeclarationReference} to something which resembles file.
 */
@Beta
@NonNullByDefault
public interface DeclarationInFile extends DeclarationReference {
    /**
     * Return the file where the declaration occurred. Returned is guaranteed to be non-empty and reference a file-like
     * resource. No further guarantees on structure of the string are made.
     *
     * @return A non-empty file name.
     */
    String fileName();
}
