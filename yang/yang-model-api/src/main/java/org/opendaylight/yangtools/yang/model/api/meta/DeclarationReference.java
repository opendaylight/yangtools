/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Reference to a statement declaration. This interface serves to provide additional information as to where a
 * particular statement comes from. More accurate information may be available through further subclasses of this
 * interface such as {@link DeclarationInText} and/or {@link DeclarationInFile}.
 */
@Beta
public interface DeclarationReference extends Immutable {
    /**
     * Returns human readable representation of this reference. This method does not prescribe any format of the
     * returned string.
     *
     * @return human readable representation of this reference.
     */
    @NonNull String toHumanReadable();
}
