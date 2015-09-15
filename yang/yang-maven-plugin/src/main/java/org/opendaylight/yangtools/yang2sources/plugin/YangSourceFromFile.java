/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

class YangSourceFromFile extends YangSourceFromDependency {

    private final File source;

    public YangSourceFromFile(final File source) {
        super(YangTextSchemaSource.identifierFromFilename(source.getName()));
        this.source = Preconditions.checkNotNull(source);
    }

    @Override
    public InputStream openStream() throws IOException {

        return new NamedFileInputStream(source, YangToSourcesProcessor.META_INF_YANG_STRING + File.separator
                + source.getName());
    }

    @Override
    public long size() throws IOException {
        return source.length();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("source", source);
    }
}
