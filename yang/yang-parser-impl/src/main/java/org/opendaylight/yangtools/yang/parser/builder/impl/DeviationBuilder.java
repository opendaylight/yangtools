/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Deviation.Deviate;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class DeviationBuilder extends AbstractBuilder {
    private DeviationImpl instance;
    private final String targetPathStr;
    private SchemaPath targetPath;
    private Deviate deviate;
    private String reference;

    DeviationBuilder(final String moduleName, final int line, final String targetPathStr) {
        super(moduleName, line);
        if (!targetPathStr.startsWith("/")) {
            throw new YangParseException(moduleName, line,
                    "Deviation argument string must be an absolute schema node identifier.");
        }
        this.targetPathStr = targetPathStr;
        this.targetPath = BuilderUtils.parseXPathString(targetPathStr);
    }

    @Override
    public Deviation build() {
        if (targetPath == null) {
            throw new YangParseException(getModuleName(), getLine(), "Unresolved deviation target");
        }

        if (instance != null) {
            return instance;
        }

        instance = new DeviationImpl();
        instance.targetPath = targetPath;
        instance.deviate = deviate;
        instance.reference = reference;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    public SchemaPath getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(final SchemaPath targetPath) {
        this.targetPath = targetPath;
    }

    public void setDeviate(final String deviate) {
        if ("not-supported".equals(deviate)) {
            this.deviate = Deviate.NOT_SUPPORTED;
        } else if ("add".equals(deviate)) {
            this.deviate = Deviate.ADD;
        } else if ("replace".equals(deviate)) {
            this.deviate = Deviate.REPLACE;
        } else if ("delete".equals(deviate)) {
            this.deviate = Deviate.DELETE;
        } else {
            throw new YangParseException(getModuleName(), getLine(), "Unsupported type of 'deviate' statement: " + deviate);
        }
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "deviation " + targetPathStr;
    }

    private static final class DeviationImpl implements Deviation {
        private SchemaPath targetPath;
        private Deviate deviate;
        private String reference;
        private ImmutableList<UnknownSchemaNode> unknownNodes;

        private DeviationImpl() {
        }

        @Override
        public SchemaPath getTargetPath() {
            return targetPath;
        }

        @Override
        public Deviate getDeviate() {
            return deviate;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((targetPath == null) ? 0 : targetPath.hashCode());
            result = prime * result + ((deviate == null) ? 0 : deviate.hashCode());
            result = prime * result + ((reference == null) ? 0 : reference.hashCode());
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
            DeviationImpl other = (DeviationImpl) obj;
            if (targetPath == null) {
                if (other.targetPath != null) {
                    return false;
                }
            } else if (!targetPath.equals(other.targetPath)) {
                return false;
            }
            if (deviate == null) {
                if (other.deviate != null) {
                    return false;
                }
            } else if (!deviate.equals(other.deviate)) {
                return false;
            }
            if (reference == null) {
                if (other.reference != null) {
                    return false;
                }
            } else if (!reference.equals(other.reference)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(DeviationImpl.class.getSimpleName());
            sb.append("[");
            sb.append("targetPath=").append(targetPath);
            sb.append(", deviate=").append(deviate);
            sb.append(", reference=").append(reference);
            sb.append("]");
            return sb.toString();
        }
    }

}
