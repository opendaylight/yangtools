/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A {@link YinTextSchemaSource} backed by a file.
 *
 * @author Robert Varga
 */
final class YinTextFileSchemaSource extends YinTextSchemaSource implements Delegator<File> {
    private final File file;

    YinTextFileSchemaSource(final SourceIdentifier identifier, final File file) {
        super(identifier);
        this.file = Preconditions.checkNotNull(file);
    }

    @Override
    public File getDelegate() {
        return file;
    }

    @Override
    public InputStream openStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("file", file);
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(file.toString());
    }
}
