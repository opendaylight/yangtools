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
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

/**
 * DataTree configuration class.
 *
 * <p>TreeConfig supports currently the following options:
 * <ul>
 * <li>treeType</li>
 * <li>enable/disable unique indexes and unique constraint validation</li>
 * <li>enable/disable mandatory nodes validation</li>
 * </ul>
 *
 * <p>TreeConfig can be easily extended in order to support further data tree configuration options, like following:
 * <ul>
 * <li>enable/disable case exclusion validation</li>
 * <li>enable/disable other indexes</li>
 * <li>other schema aware validation options</li>
 * </ul>
 *
 * <p>This can be useful when strict validation is not required or useful for some reasons.
 */
@Beta
public final class DataTreeConfiguration implements Immutable {
    /**
     * Default configuration for a data tree storing {@code config true} data nodes. It has the following
     * characteristics:
     * <ul>
     *   <li>mandatory node presence is enforced</li>
     *   <li>{@code unique} constraints are not enforced</li>
     *   <li>version information is not tracked</li>
     *   <li>recursive validation of written structures is performed</li>
     * </ul>
     */
    public static final @NonNull DataTreeConfiguration DEFAULT_CONFIGURATION = new Builder(TreeType.CONFIGURATION)
            .setMandatoryNodesValidation(true)
            .build();
    /**
     * Default configuration for a data tree storing {@code config false} data nodes. It has the following
     * characteristics:
     * <ul>
     *   <li>mandatory node presence is enforced</li>
     *   <li>{@code unique} constraints are not enforced</li>
     *   <li>version information is not tracked</li>
     *   <li>recursive validation of written structures is not performed</li>
     * </ul>
     */
    public static final @NonNull DataTreeConfiguration DEFAULT_OPERATIONAL = new Builder(TreeType.OPERATIONAL)
            .setMandatoryNodesValidation(true)
            .build();

    private final @NonNull TreeType treeType;
    private final @NonNull YangInstanceIdentifier rootPath;
    private final boolean uniqueIndexes;
    private final boolean mandatoryNodesValidation;
    private final boolean trackVersionInfo;
    private final boolean recursiveWriteValidation;

    DataTreeConfiguration(final TreeType treeType, final YangInstanceIdentifier rootPath, final boolean uniqueIndexes,
            final boolean mandatoryNodesValidation, final boolean trackVersionInfo,
            final boolean recursiveWriteValidation) {
        this.treeType = requireNonNull(treeType);
        this.rootPath = requireNonNull(rootPath);
        this.uniqueIndexes = uniqueIndexes;
        this.mandatoryNodesValidation = mandatoryNodesValidation;
        this.trackVersionInfo = trackVersionInfo;
        this.recursiveWriteValidation = recursiveWriteValidation;
    }

    /**
     * Returns the default configuration for a {@link TreeType}.
     *
     * @param treeType the {@link TreeType}
     * @return the default configuration
     */
    public static @NonNull DataTreeConfiguration getDefault(final TreeType treeType) {
        return switch (requireNonNull(treeType)) {
            case CONFIGURATION -> DEFAULT_CONFIGURATION;
            case OPERATIONAL -> DEFAULT_OPERATIONAL;
        };
    }

    /**
     * Returns a new {@link Builder} initialized for a {@link TreeType}.
     *
     * @param treeType the {@link TreeType}
     * @return a new {@link Builder}
     */
    public static @NonNull Builder builder(final TreeType treeType) {
        return new Builder(treeType);
    }

    @Deprecated(since = "14.0.14", forRemoval = true)
    public @NonNull Builder copyBuilder() {
        return toBuilder();
    }

    /**
     * Returns a new {@link Builder} initialized to produce an equivalent configuration to this one.
     *
     * @return a new {@link Builder}
     */
    public @NonNull Builder toBuilder() {
        return new Builder(treeType)
            .setMandatoryNodesValidation(isMandatoryNodesValidationEnabled())
            .setRecursiveWriteValidation(isRecursiveWriteValidationEnabled())
            .setRootPath(getRootPath())
            .setUniqueIndexes(isUniqueIndexEnabled())
            .setTrackVersionInfo(isVersionInfoTrackingEnabled());
    }

    /**
     * Returns the path prefix from the conceptual YANG data store root to the root node of this data tree. Empty
     * indicates the data store root.
     *
     * @return the path prefix
     */
    public @NonNull YangInstanceIdentifier getRootPath() {
        return rootPath;
    }

    /**
     * Returns the {@link TreeType} of this data tree.
     *
     * @return the {@link TreeType}
     */
    public @NonNull TreeType getTreeType() {
        return treeType;
    }

    /**
     * Returns {@code true} if the presence of mandatory nodes should be enforced. This option is expected to have
     * a minor performance impact.
     *
     * @return {@code true} if the presence of mandatory nodes should be enforced
     */
    public boolean isMandatoryNodesValidationEnabled() {
        return mandatoryNodesValidation;
    }

    /**
     * Returns {@code true} if {@code unique} constraints should be enforced. Reference implementation is known to not
     * scale well with this option enabled.
     *
     * @return {@code true} if {@code unique} constraints should be enforced
     */
    public boolean isUniqueIndexEnabled() {
        return uniqueIndexes;
    }

    /**
     * Returns {@code true} if requests to store additional versioning information should be honored. This option is
     * expected to have increase heap metadata usage by 0 (typical JVM configuration) to 50%. This does not includes
     * the overhead of actually stored data, as that is controlled by the user.
     *
     * @return {@code true} if requests to store additional versioning information should be honored
     */
    public boolean isVersionInfoTrackingEnabled() {
        return trackVersionInfo;
    }

    /**
     * Returns {@code true} if written {@link DataContainerNode}s should be recursively verified to not contain
     * incorrect data. This option can have significant impact on CPU usage, depending on access patterns.
     *
     * @return {@code true} if written {@link DataContainerNode}s should be recursively verified
     */
    public boolean isRecursiveWriteValidationEnabled() {
        return recursiveWriteValidation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(treeType, rootPath, uniqueIndexes, mandatoryNodesValidation, trackVersionInfo,
            recursiveWriteValidation);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof DataTreeConfiguration other
            && treeType == other.treeType && rootPath.equals(other.rootPath) && uniqueIndexes == other.uniqueIndexes
            && mandatoryNodesValidation == other.mandatoryNodesValidation && trackVersionInfo == other.trackVersionInfo
            && recursiveWriteValidation == other.recursiveWriteValidation;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("type", treeType)
            .add("root", rootPath)
            .add("mandatory", mandatoryNodesValidation)
            .add("unique", uniqueIndexes)
            .add("info", trackVersionInfo)
            .add("recursive", recursiveWriteValidation)
            .toString();
    }

    public static class Builder implements Mutable {
        private final TreeType treeType;

        private YangInstanceIdentifier rootPath = YangInstanceIdentifier.of();
        private boolean uniqueIndexes;
        private boolean mandatoryNodesValidation;
        private boolean trackVersionInfo;
        private boolean recursiveWriteValidation;

        @Deprecated(since = "14.0.14", forRemoval = true)
        public Builder(final TreeType treeType) {
            this.treeType = requireNonNull(treeType);
            recursiveWriteValidation = treeType == TreeType.CONFIGURATION;
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

        public @NonNull Builder setTrackVersionInfo(final boolean trackVersionInfo) {
            this.trackVersionInfo = trackVersionInfo;
            return this;
        }

        public @NonNull Builder setRecursiveWriteValidation(final boolean recursiveWriteValidation) {
            this.recursiveWriteValidation = recursiveWriteValidation;
            return this;
        }

        /**
         * Return {@link DataTreeConfiguration} as defined by this builder's current state.
         *
         * @return A DataTreeConfiguration
         */
        public @NonNull DataTreeConfiguration build() {
            return new DataTreeConfiguration(treeType, rootPath, uniqueIndexes, mandatoryNodesValidation,
                trackVersionInfo, recursiveWriteValidation);
        }
    }
}
