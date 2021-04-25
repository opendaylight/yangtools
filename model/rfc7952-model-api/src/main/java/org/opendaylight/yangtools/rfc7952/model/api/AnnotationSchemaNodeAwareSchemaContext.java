/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A {@link SchemaContext} which is also {@link AnnotationSchemaNodeAware}. This is a utility capture for users which
 * need to deal with SchemaContext, but also require it to have annotation indices.
 */
@Beta
public interface AnnotationSchemaNodeAwareSchemaContext extends AnnotationSchemaNodeAware, SchemaContext {

}
