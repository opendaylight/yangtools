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
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

class YangSourceInZipFile extends YangSourceFromDependency {

    private final ZipFile file;
    private final ZipEntry entry;

    YangSourceInZipFile(final ZipFile file, final ZipEntry entry) {
        super(YangTextSchemaSource.identifierFromFilename(entry.getName()));
        this.file = Preconditions.checkNotNull(file);
        this.entry = Preconditions.checkNotNull(entry);
    }

    @Override
    public long size() {
        return entry.getSize();
    }

    @Override
    public InputStream openStream() throws IOException {
        return file.getInputStream(entry);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("file", file).add("entry", entry);
    }
}