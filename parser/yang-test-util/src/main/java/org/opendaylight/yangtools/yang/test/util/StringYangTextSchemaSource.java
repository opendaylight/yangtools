/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.test.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.StringReader;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * A {@link YangTextSchemaSource} backed by a string literal.
 */
final class StringYangTextSchemaSource extends YangTextSchemaSource {
    private final @NonNull String sourceString;

    private StringYangTextSchemaSource(final SourceIdentifier identifier, final String sourceString,
            final String symbolicName) {
        super(identifier);
        this.sourceString = requireNonNull(sourceString);
    }

    /**
     * Create a new {@link YangTextSchemaSource} backed by a String input.
     *
     * @param sourceString YANG file as a String
     * @return A new instance.
     * @throws NullPointerException if {@code sourceString} is {@code null}
     * @throws IllegalArgumentException if {@code sourceString} does not a valid YANG body, given a rather restrictive
     *         view of what is valid.
     */
    static @NonNull StringYangTextSchemaSource ofLiteral(final String sourceString) {
        // First line of a YANG file looks as follows:
        //   `module module-name {`
        // therefore in order to extract the name of the module from a plain string, we are interested in the second
        // word of the first line
        final String[] firstLine = sourceString.substring(0, sourceString.indexOf("{")).strip().split(" ");
        final String moduleOrSubmoduleString = firstLine[0].strip();
        checkArgument(moduleOrSubmoduleString.equals("module") || moduleOrSubmoduleString.equals("submodule"));

        final String arg = firstLine[1].strip();
        final var localName = UnresolvedQName.tryLocalName(arg);
        checkArgument(localName != null);
        return new StringYangTextSchemaSource(new SourceIdentifier(localName), sourceString, arg);
    }

    @Override
    public StringReader openStream() {
        return new StringReader(sourceString);
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(getIdentifier().name().getLocalName());
    }
}
