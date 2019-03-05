/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Extension to the NormalizedNodeStreamWriter with attribute support.
 */
// FIXME: YANGTOOLS-961: remove this class
@Deprecated
public interface NormalizedNodeStreamAttributeWriter extends NormalizedNodeStreamWriter {

    void attributes(Map<QName, String> attributes) throws IOException;
}
