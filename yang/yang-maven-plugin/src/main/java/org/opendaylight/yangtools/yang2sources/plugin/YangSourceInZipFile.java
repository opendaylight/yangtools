/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class YangSourceInZipFile extends YangSourceFromDependency {

    private final ZipFile file;
    private final ZipEntry entry;

    YangSourceInZipFile(ZipFile file, ZipEntry entry) {
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
    String getDescription() {
        return file.getName() + "::" + entry.getName();
    }
}
