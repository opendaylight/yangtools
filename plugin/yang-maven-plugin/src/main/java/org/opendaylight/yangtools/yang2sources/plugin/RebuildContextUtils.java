/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class serving RebuildContext functionality.
 */
final class RebuildContextUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RebuildContextUtils.class);
    private static final HashFunction HASH_FUNCTION = Hashing.crc32c();

    private RebuildContextUtils() {
        // utility class
    }

    /**
     * Builds hash code of writable object.
     *
     * @param writable object
     * @return hash code
     */
    static @Nullable String buildHashCode(final WritableObject writable) {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)
        ) {
            writable.writeTo(oos);
            oos.flush();
            return HASH_FUNCTION.hashBytes(baos.toByteArray()).toString();
        } catch (final IOException e) {
            LOGGER.warn("Could not build hash", e);
            return null;
        }
    }

    /**
     * Converts module into a serializable object representing effective model.
     *
     * @param module module as provided via effective model context
     * @return serializable
     */
    static WritableObject toWritableObject(final Module module) {
        Objects.requireNonNull(module, "module should not be null");
        final ModuleEffectiveStatement moduleStatement = module.asEffectiveStatement();
        final QName name = moduleStatement.statementDefinition().getStatementName();
        final List<WritableStatement> children = toWritableStatements(moduleStatement.effectiveSubstatements());
        return new WritableStatement(name, null, null, children);
    }

    private static List<WritableStatement> toWritableStatements(
            List<? extends EffectiveStatement<?, ?>> statements) {
        if (statements == null || statements.isEmpty()) {
            return List.of();
        }
        return statements.stream().map(es -> {
            final QName name = es.statementDefinition().getStatementName();
            final QName argName = es.statementDefinition().getArgumentDefinition()
                    .map(ArgumentDefinition::argumentName).orElse(null);
            final String argument = es.getDeclared() == null ? null : es.getDeclared().rawArgument();
            final List<WritableStatement> children = toWritableStatements(es.effectiveSubstatements());
            return new WritableStatement(name, argName, argument, children);
        }).collect(Collectors.toList());
    }

    private record WritableStatement(
            QName name, @Nullable QName argName, @Nullable String argument, List<WritableStatement> children
    ) implements WritableObject {

        @Override
        public void writeTo(DataOutput out) throws IOException {
            name.writeTo(out);
            out.writeBoolean(argName == null);
            if (argName != null) {
                argName.writeTo(out);
            }
            out.writeBoolean(argument == null);
            if (argument != null) {
                out.writeUTF(argument);
            }
            out.writeInt(children.size());
            for (WritableStatement child : children) {
                child.writeTo(out);
            }
        }
    }

}
