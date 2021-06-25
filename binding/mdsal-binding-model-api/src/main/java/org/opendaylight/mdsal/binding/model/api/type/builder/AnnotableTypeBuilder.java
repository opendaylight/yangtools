/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api.type.builder;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

/**
 * Common interface for java type builders which allow attaching annotations to them.
 */
@Beta
public interface AnnotableTypeBuilder {
    /**
     * The method creates new {@link AnnotationTypeBuilder} containing specified package name an annotation name.
     *
     * @param identifier JavaTypeName of the annotation
     * @return a new instance of Annotation Type Builder.
     */
    AnnotationTypeBuilder addAnnotation(JavaTypeName identifier);

    /**
     * The method creates new {@link AnnotationTypeBuilder} containing specified package name an annotation name.
     * Neither the package name or annotation name can contain <code>null</code> references. In case that any
     * of parameters contains <code>null</code> the method SHOULD thrown {@link IllegalArgumentException}
     *
     * @param packageName Package Name of Annotation Type
     * @param simpleName Name of Annotation Type
     * @return <code>new</code> instance of Annotation Type Builder.
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if any of the arguments is an empty string
     */
    default AnnotationTypeBuilder addAnnotation(final String packageName, final String simpleName) {
        return addAnnotation(JavaTypeName.create(packageName, simpleName));
    }
}
