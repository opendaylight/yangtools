/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.util.HashMap;
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

    private Map<QName, LeafRefContext> referencingChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByChilds = new HashMap<QName, LeafRefContext>();
    private Map<QName, LeafRefContext> referencedByLeafRefCtx = new HashMap<QName, LeafRefContext>();

    public LeafRefContextBuilder(final QName currentNodeQName,
            final SchemaPath currentNodePath, final SchemaContext schemaContext) {
        this.currentNodeQName = currentNodeQName;
        this.currentNodePath = currentNodePath;
        this.schemaContext = schemaContext;
    }

    public LeafRefContext build() {
        final LeafRefContext leafRefContext = new LeafRefContext(this);

        referencingChilds = new HashMap<QName, LeafRefContext>();
        referencedByChilds = new HashMap<QName, LeafRefContext>();
        referencedByLeafRefCtx = new HashMap<QName, LeafRefContext>();

        return leafRefContext;
    }

    public boolean hasLeafRefContextChild() {
        return hasReferencedByChild() || hasReferencingChild();
    }

    public boolean hasReferencedByChild() {
        return !referencedByChilds.isEmpty();
    }

    public boolean hasReferencingChild() {
        return !referencingChilds.isEmpty();
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
        referencingChilds.put(childQName, child);
    }

    public LeafRefContext getReferencingChildByName(final QName name) {
        return referencingChilds.get(name);
    }

    public Map<QName, LeafRefContext> getReferencingChilds() {
        return referencingChilds;
    }

    public void addReferencedByChild(final LeafRefContext child, final QName childQName) {
        referencedByChilds.put(childQName, child);
    }

    public LeafRefContext getReferencedByChildByName(final QName name) {
        return referencedByChilds.get(name);
    }

    public Map<QName, LeafRefContext> getReferencedByChilds() {
        return referencedByChilds;
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
        final QNameModule qnameModule = currentNodeQName.getModule();

        return schemaContext.findModuleByNamespaceAndRevision(
                qnameModule.getNamespace(), qnameModule.getRevision());
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
