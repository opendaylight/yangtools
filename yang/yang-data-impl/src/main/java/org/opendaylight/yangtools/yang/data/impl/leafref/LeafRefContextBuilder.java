/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

class LeafRefContextBuilder {

    private QName currentNodeQName;
    private SchemaPath currentNodePath;
    private SchemaContext schemaContext;

    private LeafRefPath leafRefTargetPath = null;
    private LeafRefPath absoluteLeafRefTargetPath = null;
    private String leafRefTargetPathString = "";

    private boolean isReferencedBy = false;
    private boolean isReferencing = false;

    private final Map<QName, LeafRefContext> referencingChildren = new HashMap<>();
    private final Map<QName, LeafRefContext> referencedByChildren = new HashMap<>();
    private final Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<>();

    public LeafRefContextBuilder(final QName currentNodeQName,
            final SchemaPath currentNodePath, final SchemaContext schemaContext) {
        this.currentNodeQName = currentNodeQName;
        this.currentNodePath = currentNodePath;
        this.schemaContext = schemaContext;
    }

    public LeafRefContext build() {
        final LeafRefContext leafRefContext = new LeafRefContext(this);

        // LeafRefContext has made a copy of these
        referencingChildren.clear();
        referencedByChildren.clear();
        referencedByLeafRefCtx.clear();

        return leafRefContext;
    }

    public boolean hasLeafRefContextChild() {
        return hasReferencedByChild() || hasReferencingChild();
    }

    public boolean hasReferencedByChild() {
        return !referencedByChildren.isEmpty();
    }

    public boolean hasReferencingChild() {
        return !referencingChildren.isEmpty();
    }

    public boolean isReferencedBy() {
        return isReferencedBy;
    }

    public void setReferencedBy(final boolean isReferencedBy) {
        this.isReferencedBy = isReferencedBy;
    }

    public boolean isReferencing() {
        return isReferencing;
    }

    public void setReferencing(final boolean isReferencing) {
        this.isReferencing = isReferencing;
    }

    public void addReferencingChild(final LeafRefContext child, final QName childQName) {
        referencingChildren.put(childQName, child);
    }

    public LeafRefContext getReferencingChildByName(final QName name) {
        return referencingChildren.get(name);
    }

    public Map<QName, LeafRefContext> getReferencingChilds() {
        return referencingChildren;
    }

    public void addReferencedByChild(final LeafRefContext child, final QName childQName) {
        referencedByChildren.put(childQName, child);
    }

    public LeafRefContext getReferencedByChildByName(final QName name) {
        return referencedByChildren.get(name);
    }

    public Map<QName, LeafRefContext> getReferencedByChilds() {
        return referencedByChildren;
    }

    public SchemaPath getCurrentNodePath() {
        return currentNodePath;
    }

    public void setCurrentNodePath(final SchemaPath currentNodePath) {
        this.currentNodePath = currentNodePath;
    }

    public LeafRefPath getLeafRefTargetPath() {
        return leafRefTargetPath;
    }

    public void setLeafRefTargetPath(final LeafRefPath leafRefPath) {
        this.leafRefTargetPath = leafRefPath;
    }

    public String getLeafRefTargetPathString() {
        return leafRefTargetPathString;
    }

    public void setLeafRefTargetPathString(final String leafRefPathString) {
        this.leafRefTargetPathString = leafRefPathString;
    }

    public QName getCurrentNodeQName() {
        return currentNodeQName;
    }

    public void setCurrentNodeQName(final QName currentNodeQName) {
        this.currentNodeQName = currentNodeQName;
    }

    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public void setSchemaContext(final SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    public LeafRefPath getAbsoluteLeafRefTargetPath() {

        if (isReferencing && absoluteLeafRefTargetPath == null) {
            if (leafRefTargetPath.isAbsolute()) {
                absoluteLeafRefTargetPath = leafRefTargetPath;
            } else {
                absoluteLeafRefTargetPath = LeafRefUtils
                        .createAbsoluteLeafRefPath(leafRefTargetPath,
                                currentNodePath, getLeafRefContextModule());
            }
        }

        return absoluteLeafRefTargetPath;
    }

    public Module getLeafRefContextModule() {
        final Iterator<QName> it = currentNodePath.getPathFromRoot().iterator();
        final QNameModule qnameModule = it.hasNext() ? it.next().getModule() : currentNodeQName.getModule();

        return schemaContext.findModuleByNamespaceAndRevision(qnameModule.getNamespace(), qnameModule.getRevision());
    }

    public void addReferencedByLeafRefCtx(final QName qname, final LeafRefContext leafRef) {
        referencedByLeafRefCtx.put(qname, leafRef);
    }

    public LeafRefContext getReferencedByLeafRefCtxByName(final QName qname) {
        return referencedByLeafRefCtx.get(qname);
    }

    public Map<QName, LeafRefContext> getAllReferencedByLeafRefCtxs() {
        return referencedByLeafRefCtx;
    }

}
