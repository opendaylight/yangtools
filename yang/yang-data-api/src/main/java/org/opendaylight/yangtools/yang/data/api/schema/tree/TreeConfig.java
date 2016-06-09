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

/**
 * DataTree configuration class.
 *
 * TreeConfig supports currently the following options:
 * <ul>
 * <li>treeType</li>
 * <li>enable/disable unique indexes and unique constraint validation</li>
 * </ul>
 *
 * TreeConfig can be easily extended in order to support further data tree
 * configuration options, like following:
 * <ul>
 * <li>enable/disable mandatory nodes validation</li>
 * <li>enable/disable case exclusion validation</li>
 * <li>enable/disable other indexes</li>
 * <li>other schema aware validation options</li>
 * </ul>
 *
 * This can be useful when strict validation is not required or useful for some reasons.
 *
 */
@Beta
public class TreeConfig {
    public static final TreeConfig DEFAULT_CONFIGURATION = new TreeConfig(TreeType.CONFIGURATION, false);
    public static final TreeConfig DEFAULT_OPERATIONAL = new TreeConfig(TreeType.OPERATIONAL, false);

    private final TreeType treeType;
    private final boolean uniqueIndexes;

    public TreeConfig(final TreeType treeType, final boolean uniqueIndexes) {
        this.treeType = Preconditions.checkNotNull(treeType);
        this.uniqueIndexes = uniqueIndexes;
    }

    public TreeType getTreeType() {
        return treeType;
    }

    public boolean isUniqueIndexEnabled() {
        return uniqueIndexes;
    }

    public static TreeConfig getDefault(final TreeType treeType) {
        Preconditions.checkNotNull(treeType);
        switch (treeType) {
        case CONFIGURATION:
            return DEFAULT_CONFIGURATION;
        case OPERATIONAL:
            return DEFAULT_OPERATIONAL;
        default:
            return new TreeConfig(treeType, false);
        }
    }
}