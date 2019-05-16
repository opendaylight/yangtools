/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.schema.OpaqueAnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.OpaqueAnydataExtension.StreamWriter;

/**
 * Support for metadata in {@link OpaqueAnydataNode}s.
 */
@Beta
public interface OpaqueAnydataStreamWriter extends StreamWriter, StreamWriterMethods {

}
