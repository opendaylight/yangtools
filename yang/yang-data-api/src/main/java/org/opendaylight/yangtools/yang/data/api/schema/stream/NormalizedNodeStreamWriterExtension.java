/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.ObjectExtension;

/**
 * Extension interface for {@link NormalizedNodeStreamWriter}. Extensions should extend this interface and their
 * instances should be made available through {@link NormalizedNodeStreamWriter#getExtensions()}.
 *
 * @author Robert Varga
 */
@Beta
interface NormalizedNodeStreamWriterExtension extends
    ObjectExtension<NormalizedNodeStreamWriter, NormalizedNodeStreamWriterExtension> {

}
