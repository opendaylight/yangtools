/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang2sources.plugin;

import static java.io.OutputStream.nullOutputStream;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
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
     * Builds hash code of serializable object.
     *
     * @param object object
     * @return hash code
     */
    static @Nullable String buildHashCode(final Serializable object) {
        try (
                HashingOutputStream hos = new HashingOutputStream(HASH_FUNCTION, nullOutputStream());
                ObjectOutputStream oos = new ObjectOutputStream(hos)
        ) {
            oos.writeUnshared(object);
            oos.flush();
            return hos.hash().toString();
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
    static Serializable toSerializable(final Module module) {
        Objects.requireNonNull(module, "module should not be null");
        final ModuleEffectiveStatement moduleStatement = module.asEffectiveStatement();
        final SerializableStatement result = new SerializableStatement();
        result.setName(moduleStatement.statementDefinition().getStatementName());
        result.setChildren(toSerializableStatements(moduleStatement.effectiveSubstatements()));
        return result;
    }

    private static List<SerializableStatement> toSerializableStatements(
            List<? extends EffectiveStatement<?, ?>> statements) {
        if (statements == null || statements.isEmpty()) {
            return List.of();
        }
        return statements.stream().map(es -> {
            final SerializableStatement serializable = new SerializableStatement();
            serializable.setName(es.statementDefinition().getStatementName());
            serializable.setArgName(es.statementDefinition().getArgumentDefinition()
                    .map(ArgumentDefinition::argumentName).orElse(null));
            serializable.setArgument(es.getDeclared() == null ? null : es.getDeclared().rawArgument());
            serializable.setChildren(toSerializableStatements(es.effectiveSubstatements()));
            return serializable;
        }).collect(Collectors.toList());
    }

    private static class SerializableStatement implements Serializable {
        private static final long serialVersionUID = 1L;

        private QName name;
        private QName argName;
        private String argument;
        private List<SerializableStatement> children;

        public QName getName() {
            return name;
        }

        public void setName(QName name) {
            this.name = name;
        }

        public QName getArgName() {
            return argName;
        }

        public void setArgName(QName argName) {
            this.argName = argName;
        }

        public String getArgument() {
            return argument;
        }

        public void setArgument(String argument) {
            this.argument = argument;
        }

        public void setChildren(List<SerializableStatement> children) {
            this.children = children;
        }

        public List<SerializableStatement> getChildren() {
            return children;
        }
    }

}
