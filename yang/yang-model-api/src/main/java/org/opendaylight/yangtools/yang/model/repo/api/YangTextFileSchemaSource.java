/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A {@link YangTextSchemaSource} backed by a file.
 *
 * @author Robert Varga
 */
@Beta
public final class YangTextFileSchemaSource extends YangTextSchemaSource implements Delegator<File> {
    private final File file;

    private YangTextFileSchemaSource(final SourceIdentifier identifier, final File file) {
        super(identifier);
        this.file = Preconditions.checkNotNull(file);
    }

    /**
     * Create a new YangTextSchemaSource backed by a {@link File} with {@link SourceIdentifier} derived from the file
     * name.
     *
     * @param file Backing File
     * @return A new YangTextSchemaSource
     * @throws IllegalArgumentException if the file name has invalid format or if the supplied File is not a file
     * @throws NullPointerException if file is null
     */
    public static YangTextFileSchemaSource create(final File file) {
        Preconditions.checkArgument(file.isFile(), "Supplied file %s is not a file");
        return new YangTextFileSchemaSource(identifierFromFilename(file.getName()), file);
    }

    @Override
    public File getDelegate() {
        return file;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("file", file);
    }
}