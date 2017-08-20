/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;
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
 * TreeConfig can be easily extended in order to support further data tree
 * configuration options, like following:
 * <ul>
 * <li>enable/disable case exclusion validation</li>
 * <li>enable/disable other indexes</li>
 * <li>other schema aware validation options</li>
 * </ul>
 *
 * <p>
 * This can be useful when strict validation is not required or useful for some
 * reasons.
 */
@Beta
public class DataTreeConfiguration implements Immutable {
    public static final DataTreeConfiguration DEFAULT_CONFIGURATION = new Builder(TreeType.CONFIGURATION)
            .setMandatoryNodesValidation(true).build();
    public static final DataTreeConfiguration DEFAULT_OPERATIONAL = new Builder(TreeType.OPERATIONAL)
            .setMandatoryNodesValidation(true).build();

    private final TreeType treeType;
    private final YangInstanceIdentifier rootPath;
    private final boolean uniqueIndexes;
    private final boolean mandatoryNodesValidation;

    DataTreeConfiguration(final TreeType treeType, final YangInstanceIdentifier rootPath, final boolean uniqueIndexes,
            final boolean mandatoryNodesValidation) {
        this.treeType = Preconditions.checkNotNull(treeType);
        this.rootPath = Preconditions.checkNotNull(rootPath);
        this.uniqueIndexes = uniqueIndexes;
        this.mandatoryNodesValidation = mandatoryNodesValidation;
    }

    public @Nonnull YangInstanceIdentifier getRootPath() {
        return rootPath;
    }

    public @Nonnull TreeType getTreeType() {
        return treeType;
    }

    public boolean isUniqueIndexEnabled() {
        return uniqueIndexes;
    }

    public boolean isMandatoryNodesValidationEnabled() {
        return mandatoryNodesValidation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", treeType).add("root", rootPath)
                .add("mandatory", mandatoryNodesValidation)
                .add("unique", uniqueIndexes).toString();
    }

    public static DataTreeConfiguration getDefault(final TreeType treeType) {
        Preconditions.checkNotNull(treeType);
        switch (treeType) {
            case CONFIGURATION:
                return DEFAULT_CONFIGURATION;
            case OPERATIONAL:
                return DEFAULT_OPERATIONAL;
            default:
                return new DataTreeConfiguration(treeType, YangInstanceIdentifier.EMPTY, false, true);
        }
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<DataTreeConfiguration> {
        private final TreeType treeType;
        private YangInstanceIdentifier rootPath;
        private boolean uniqueIndexes;
        private boolean mandatoryNodesValidation;

        public Builder(final TreeType treeType) {
            this.treeType = Preconditions.checkNotNull(treeType);
            this.rootPath = YangInstanceIdentifier.EMPTY;
        }

        public Builder setUniqueIndexes(final boolean uniqueIndexes) {
            this.uniqueIndexes = uniqueIndexes;
            return this;
        }

        public Builder setMandatoryNodesValidation(final boolean mandatoryNodesValidation) {
            this.mandatoryNodesValidation = mandatoryNodesValidation;
            return this;
        }

        public Builder setRootPath(final YangInstanceIdentifier rootPath) {
            this.rootPath = rootPath.toOptimized();
            return this;
        }

        @Override
        public DataTreeConfiguration build() {
            return new DataTreeConfiguration(treeType, rootPath, uniqueIndexes, mandatoryNodesValidation);
        }
    }
}
