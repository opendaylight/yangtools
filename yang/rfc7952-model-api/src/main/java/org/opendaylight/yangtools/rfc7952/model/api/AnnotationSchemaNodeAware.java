/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Interface for entities which can lookup {@link AnnotationSchemaNode}s based on their name.
 *
 * @author Robert Varga
 */
@Beta
public interface AnnotationSchemaNodeAware {
    /**
     * Find an annotation based on its QName.
     *
     * @param qname Annotation name
     * @return AnnotationSchemaNode if found
     * @throws NullPointerException if {@code qname} is null
     */
    @NonNull Optional<AnnotationSchemaNode> findAnnotation(QName qname);
}
