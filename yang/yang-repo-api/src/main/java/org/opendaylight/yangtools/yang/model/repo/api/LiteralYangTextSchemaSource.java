/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;

public class LiteralYangTextSchemaSource extends YangTextSchemaSource implements Delegator<String> {

    private final String sourceString;
    private final String localName;

    LiteralYangTextSchemaSource(final SourceIdentifier identifier, final String literal, final String localName) {
        super(identifier);
        sourceString = requireNonNull(literal);
        this.localName = localName;
    }

    @Override
    protected MoreObjects.ToStringHelper addToStringAttributes(MoreObjects.ToStringHelper toStringHelper) {
        return toStringHelper.add("literal", localName);
    }

    @Override
    public InputStream openStream() throws IOException {
        return CharSource.wrap(sourceString).asByteSource(StandardCharsets.UTF_8).openStream();
    }

    @Override
    public @NonNull String getDelegate() {
        return sourceString;
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(localName);
    }
}
