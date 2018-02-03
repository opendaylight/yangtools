/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static java.util.Objects.requireNonNull;
import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;
import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;

import com.google.common.annotations.Beta;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.QNameModuleToPrefixNamespace;

/**
 * A YANG text snippet generated from a {@link DeclaredStatement}. Generated {@link #stream()} or {@link #iterator()}
 * are guaranteed to not contain null nor empty strings. Furthermore, newlines are always emitted at the end
 * on the generated string -- which can be checked with {@link #isEolString(String)} utility method.
 *
 * <p>
 * This allows individual strings to be escaped as needed and external indentation can be accounted for by inserting
 * outer document indentation after the string which matched {@link #isEolString(String)} is emitted to the stream.
 *
 * @author Robert Varga
 */
@Beta
@ThreadSafe
@NonNullByDefault({ PARAMETER, RETURN_TYPE })
public final class YangTextSnippet implements Immutable, Iterable<@NonNull String> {
    private final Map<QNameModule, @NonNull String> mapper;
    private final DeclaredStatement<?> statement;

    private YangTextSnippet(final DeclaredStatement<?> statement,
            final Map<QNameModule, @NonNull String> namespaces) {
        this.statement = requireNonNull(statement);
        this.mapper = requireNonNull(namespaces);
    }

    public static YangTextSnippet of(final ModuleEffectiveStatement module, final DeclaredStatement<?> declared) {
        return new YangTextSnippet(declared, module.getAll(QNameModuleToPrefixNamespace.class));
    }

    @Override
    public Iterator<@NonNull String> iterator() {
        return new YangTextSnippetIterator(statement, mapper);
    }

    @Override
    @SuppressWarnings("null")
    public Spliterator<@NonNull String> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(),
            Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }

    @SuppressWarnings("null")
    public Stream<@NonNull String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Check if an emitted string contains End-Of-Line character.
     *
     * @param str String to be checked
     * @return True if the string contains end of line.
     */
    public static boolean isEolString(final String str) {
        return str.charAt(str.length() - 1) == '\n';
    }

    @Override
    @SuppressWarnings("null")
    public String toString() {
        return stream().collect(Collectors.joining());
    }
}
