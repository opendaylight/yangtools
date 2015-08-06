/*
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

final class UsesNodeImpl implements UsesNode {
    private final SchemaPath groupingPath;
    ImmutableSet<AugmentationSchema> augmentations;
    private boolean addedByUses;
    ImmutableMap<SchemaPath, SchemaNode> refines;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    UsesNodeImpl(final SchemaPath groupingPath) {
        this.groupingPath = groupingPath;
    }

    @Override
    public SchemaPath getGroupingPath() {
        return groupingPath;
    }

    @Override
    public Set<AugmentationSchema> getAugmentations() {
        return augmentations;
    }

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    @Override
    public Map<SchemaPath, SchemaNode> getRefines() {
        return refines;
    }

    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupingPath == null) ? 0 : groupingPath.hashCode());
        result = prime * result + ((augmentations == null) ? 0 : augmentations.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UsesNodeImpl other = (UsesNodeImpl) obj;
        if (groupingPath == null) {
            if (other.groupingPath != null) {
                return false;
            }
        } else if (!groupingPath.equals(other.groupingPath)) {
            return false;
        }
        if (augmentations == null) {
            if (other.augmentations != null) {
                return false;
            }
        } else if (!augmentations.equals(other.augmentations)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(UsesNodeImpl.class.getSimpleName());
        sb.append("[groupingPath=");
        sb.append(groupingPath);
        sb.append("]");
        return sb.toString();
    }
}