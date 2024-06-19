/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import java.util.Optional;

/**
 * Interface mixin for {@link BindingObject}s which can hold additional metadata, as specified by
 * <a href="https://tools.ietf.org/html/rfc7952">RFC7952</a>. It exposes this metadata as individual annotations via
 * {@link #annotation(Class)} method. This similar to how {@link Augmentable} works, with the notable difference that
 * there is no strong tie between an annotation and its bearer object.
 */
@Beta
public interface AnnotationAware {
    /**
     * Returns an instance of a requested annotation type.
     *
     * @param annotationType Type of annotation to be returned.
     * @param <A$$> Type capture for augmentation type
     * @return instance of annotation, or empty if the annotation is not present.
     */
    // A$$ is an identifier which cannot be generated from models.
    @SuppressWarnings("checkstyle:methodTypeParameterName")
    <A$$ extends Annotation<?>> Optional<A$$> annotation(Class<A$$> annotationType);
}
