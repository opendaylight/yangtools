/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

final class ResourceYangSource extends YangTextSchemaSource {

    private final String resourceName;

    ResourceYangSource(final String resourceName) {
        super(identifierFromFilename(resourceName));
        this.resourceName = resourceName;
    }

    @Override
    protected MoreObjects.ToStringHelper addToStringAttributes(final MoreObjects.ToStringHelper toStringHelper) {
        return toStringHelper.add("resource", resourceName);
    }

    @Override
    public InputStream openStream() throws IOException {
        return Preconditions.checkNotNull(getClass().getResourceAsStream(resourceName));
    }
}
