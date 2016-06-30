/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * DataTree configuration class.
 *
 * TreeConfig supports currently the following options:
 * <ul>
 * <li>treeType</li>
 * <li>enable/disable unique indexes and unique constraint validation</li>
 * <li>enable/disable mandatory nodes validation</li>
 * </ul>
 *
 * TreeConfig can be easily extended in order to support further data tree
 * configuration options, like following:
 * <ul>
 * <li>enable/disable case exclusion validation</li>
 * <li>enable/disable other indexes</li>
 * <li>other schema aware validation options</li>
 * </ul>
 *
 * This can be useful when strict validation is not required or useful for some
 * reasons.
 *
 */
@Beta
public class DataTreeConfiguration implements Immutable {
    public static final DataTreeConfiguration DEFAULT_CONFIGURATION = new DataTreeConfiguration(TreeType.CONFIGURATION,
            false, true);
    public static final DataTreeConfiguration DEFAULT_OPERATIONAL = new DataTreeConfiguration(TreeType.OPERATIONAL,
            false, true);

    private final TreeType treeType;
    private final boolean uniqueIndexes;
    private final boolean mandatoryNodesValidation;

    private DataTreeConfiguration(final TreeType treeType, final boolean uniqueIndexes,
            final boolean mandatoryNodesValidation) {
        this.treeType = Preconditions.checkNotNull(treeType);
        this.uniqueIndexes = uniqueIndexes;
        this.mandatoryNodesValidation = mandatoryNodesValidation;
    }

    public TreeType getTreeType() {
        return treeType;
    }

    public boolean isUniqueIndexEnabled() {
        return uniqueIndexes;
    }

    public boolean isMandatoryNodesValidationEnabled() {
        return mandatoryNodesValidation;
    }

    public static DataTreeConfiguration getDefault(final TreeType treeType) {
        Preconditions.checkNotNull(treeType);
        switch (treeType) {
        case CONFIGURATION:
            return DEFAULT_CONFIGURATION;
        case OPERATIONAL:
            return DEFAULT_OPERATIONAL;
        default:
            return new DataTreeConfiguration(treeType, false, true);
        }
    }

    public static class Builder {
        private final TreeType treeType;
        private boolean uniqueIndexes;
        private boolean mandatoryNodesValidation;

        public Builder(final TreeType treeType) {
            this.treeType = Preconditions.checkNotNull(treeType);
        }

        public Builder setUniqueIndexes(final boolean uniqueIndexes) {
            this.uniqueIndexes = uniqueIndexes;
            return this;
        }

        public Builder setMandatoryNodesValidation(final boolean mandatoryNodesValidation) {
            this.mandatoryNodesValidation = mandatoryNodesValidation;
            return this;
        }

        public DataTreeConfiguration build() {
            return new DataTreeConfiguration(treeType, uniqueIndexes, mandatoryNodesValidation);
        }
    }
}