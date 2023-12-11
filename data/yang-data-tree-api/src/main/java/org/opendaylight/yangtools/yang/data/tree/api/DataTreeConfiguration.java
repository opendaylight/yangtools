/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * DataTree configuration class.
 *
 * <p>
 * TreeConfig supports currently the following options:
 * <ul>
 * <li>treeType</li>
 * <li>enable/disable unique indexes and unique constraint validation</li>
 * <li>enable/disable mandatory nodes validation</li>
 * </ul>
 *
 * <p>
 * TreeConfig can be easily extended in order to support further data tree configuration options, like following:
 * <ul>
 * <li>enable/disable case exclusion validation</li>
 * <li>enable/disable other indexes</li>
 * <li>other schema aware validation options</li>
 * </ul>
 *
 * <p>
 * This can be useful when strict validation is not required or useful for some reasons.
 */
@Beta
public class DataTreeConfiguration implements Immutable {
    public static final DataTreeConfiguration DEFAULT_CONFIGURATION = new Builder(TreeType.CONFIGURATION)
            .setMandatoryNodesValidation(true).build();
    public static final DataTreeConfiguration DEFAULT_OPERATIONAL = new Builder(TreeType.OPERATIONAL)
            .setMandatoryNodesValidation(true).build();

    private final @NonNull TreeType treeType;
    private final @NonNull YangInstanceIdentifier rootPath;
    private final boolean uniqueIndexes;
    private final boolean mandatoryNodesValidation;
    private final boolean commitMetadata;

    DataTreeConfiguration(final TreeType treeType, final YangInstanceIdentifier rootPath, final boolean uniqueIndexes,
            final boolean mandatoryNodesValidation, final boolean commitMetadata) {
        this.treeType = requireNonNull(treeType);
        this.rootPath = requireNonNull(rootPath);
        this.uniqueIndexes = uniqueIndexes;
        this.mandatoryNodesValidation = mandatoryNodesValidation;
        this.commitMetadata = commitMetadata;
    }

    public @NonNull YangInstanceIdentifier getRootPath() {
        return rootPath;
    }

    public @NonNull TreeType getTreeType() {
        return treeType;
    }

    public boolean isUniqueIndexEnabled() {
        return uniqueIndexes;
    }

    public boolean isMandatoryNodesValidationEnabled() {
        return mandatoryNodesValidation;
    }

    public boolean isCommitMetadataEnabled() {
        return commitMetadata;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", treeType).add("root", rootPath)
            .add("commit-meta", commitMetadata)
            .add("mandatory", mandatoryNodesValidation)
            .add("unique", uniqueIndexes)
            .toString();
    }

    public static DataTreeConfiguration getDefault(final TreeType treeType) {
        return switch (requireNonNull(treeType)) {
            case CONFIGURATION -> DEFAULT_CONFIGURATION;
            case OPERATIONAL -> DEFAULT_OPERATIONAL;
        };
    }

    public static @NonNull Builder builder(final TreeType treeType) {
        return new Builder(treeType);
    }

    public @NonNull Builder copyBuilder() {
        return new Builder(treeType)
                .setMandatoryNodesValidation(isMandatoryNodesValidationEnabled())
                .setUniqueIndexes(isUniqueIndexEnabled())
                .setRootPath(getRootPath());
    }

    public static class Builder implements Mutable {
        private final TreeType treeType;

        private YangInstanceIdentifier rootPath;
        private boolean uniqueIndexes;
        private boolean mandatoryNodesValidation;
        private boolean commitMetadata;

        public Builder(final TreeType treeType) {
            this.treeType = requireNonNull(treeType);
            rootPath = YangInstanceIdentifier.of();
        }

        public @NonNull Builder setUniqueIndexes(final boolean uniqueIndexes) {
            this.uniqueIndexes = uniqueIndexes;
            return this;
        }

        public @NonNull Builder setMandatoryNodesValidation(final boolean mandatoryNodesValidation) {
            this.mandatoryNodesValidation = mandatoryNodesValidation;
            return this;
        }

        public @NonNull Builder setRootPath(final YangInstanceIdentifier rootPath) {
            this.rootPath = rootPath.toOptimized();
            return this;
        }

        public @NonNull Builder setCommitMetadata(final boolean commitMetadata) {
            this.commitMetadata = commitMetadata;
            return this;
        }

        /**
         * Return {@link DataTreeConfiguration} as defined by this builder's current state.
         *
         * @return A DataTreeConfiguration
         */
        public @NonNull DataTreeConfiguration build() {
            return new DataTreeConfiguration(treeType, rootPath, uniqueIndexes, mandatoryNodesValidation,
                commitMetadata);
        }
    }
}
