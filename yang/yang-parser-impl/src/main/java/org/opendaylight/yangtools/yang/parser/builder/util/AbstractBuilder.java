/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.util;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;

/**
 * Base helper implementation of Builders for Yang Model elements.
 *
 */
public abstract class AbstractBuilder implements Builder {
    private final String moduleName;
    private final int line;
    private Builder parentBuilder;

    protected final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
    protected final List<UnknownSchemaNodeBuilder> addedUnknownNodes = new ArrayList<>();
    private boolean sealed;

    protected AbstractBuilder(final String moduleName, final int line) {
        this.moduleName = Preconditions.checkNotNull(moduleName,"moduleName must not be null");
        this.line = line;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public Builder getParent() {
        return parentBuilder;
    }

    @Override
    public void setParent(final Builder parentBuilder) {
        checkNotSealed();
        this.parentBuilder = parentBuilder;
    }

    @Override
    public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
        return addedUnknownNodes;
    }

    @Override
    public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {
        addedUnknownNodes.add(unknownNode);
    }

    void seal() {
        checkNotSealed();
        sealed  = true;
    }

    protected final void checkNotSealed() {
        Preconditions.checkState(!sealed, "Builder is sealed. No further modifications allowed");
    }

    boolean isSealed() {
        return sealed;
    }

}
