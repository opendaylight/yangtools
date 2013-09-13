/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

/**
 * Basic implementation of Builder.
 */
public abstract class AbstractBuilder implements Builder {
    protected String moduleName;
    protected final int line;
    protected Builder parent;

    protected List<UnknownSchemaNode> unknownNodes;
    protected final List<UnknownSchemaNodeBuilder> addedUnknownNodes = new ArrayList<UnknownSchemaNodeBuilder>();

    protected AbstractBuilder(final String moduleName, final int line) {
        this.moduleName = moduleName;
        this.line = line;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        result = prime * result + line;
        // result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((unknownNodes == null) ? 0 : unknownNodes.hashCode());
        // result = prime * result + ((addedUnknownNodes == null) ? 0 :
        // addedUnknownNodes.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractBuilder other = (AbstractBuilder) obj;

        if (moduleName == null) {
            if (other.moduleName != null) {
                return false;
            }
        } else if (!moduleName.equals(other.moduleName)) {
            return false;
        }
        if (line != other.line) {
            return false;
        }
        if (parent != other.parent) {
            return false;
        }
        if (unknownNodes == null) {
            if (other.unknownNodes != null) {
                return false;
            }
        } else if (!unknownNodes.equals(other.unknownNodes)) {
            return false;
        }
        if (addedUnknownNodes == null) {
            if (other.addedUnknownNodes != null) {
                return false;
            }
        } else if (!addedUnknownNodes.equals(other.addedUnknownNodes)) {
            return false;
        }

        return true;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public Builder getParent() {
        return parent;
    }

    @Override
    public void setParent(final Builder parent) {
        this.parent = parent;
    }

    @Override
    public List<UnknownSchemaNodeBuilder> getUnknownNodeBuilders() {
        return addedUnknownNodes;
    }

    @Override
    public void addUnknownNodeBuilder(UnknownSchemaNodeBuilder unknownNode) {
        addedUnknownNodes.add(unknownNode);
    }

    public void setUnknownNodes(List<UnknownSchemaNode> unknownNodes) {
        this.unknownNodes = unknownNodes;
    }

}
