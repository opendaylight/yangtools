/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Common interface for java type builders which allow attaching annotations to them.
 */
@Beta
@NonNullByDefault
public interface AnnotableTypeBuilder extends Mutable {
    /**
     * Add an {@link AttachedAnnotation} to this builder.
     *
     * @param annotation the {@link AttachedAnnotation}, if {@code null} this method does nothing
     * @return this instance
     */
    AnnotableTypeBuilder addAnnotation(@Nullable AttachedAnnotation annotation);
}
