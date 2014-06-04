/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class ChoiceBuilder extends AbstractSchemaNodeBuilder implements DataSchemaNodeBuilder,
        AugmentationTargetBuilder {
    private ChoiceNodeImpl instance;

    // DataSchemaNode args
    private boolean augmenting;
    private boolean addedByUses;
    private boolean configuration;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final Set<AugmentationSchema> augmentations = new HashSet<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();
    // ChoiceNode args
    private final Set<ChoiceCaseBuilder> caseBuilders = new HashSet<>();
    private String defaultCase;

    public ChoiceBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        constraints = new ConstraintsBuilderImpl(moduleName, line);
    }

    public ChoiceBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final ChoiceNode base) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        constraints = new ConstraintsBuilderImpl(moduleName, line, base.getConstraints());

        description = base.getDescription();
        reference = base.getReference();
        status = base.getStatus();
        augmenting = base.isAugmenting();
        addedByUses = base.isAddedByUses();
        configuration = base.isConfiguration();
        augmentations.addAll(base.getAvailableAugmentations());

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        Set<DataSchemaNodeBuilder> wrapped = BuilderUtils.wrapChildNodes(moduleName, line, new HashSet<DataSchemaNode>(
                base.getCases()), path, ns, rev, pref);
        for (DataSchemaNodeBuilder wrap : wrapped) {
            if (wrap instanceof ChoiceCaseBuilder) {
                caseBuilders.add((ChoiceCaseBuilder) wrap);
            }
        }

        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path, ns,
                rev, pref));
    }

    @Override
    public ChoiceNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new ChoiceNodeImpl(qname, schemaPath);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;
        instance.augmenting = augmenting;
        instance.addedByUses = addedByUses;
        instance.configuration = configuration;

        instance.constraints = constraints.toInstance();
        instance.defaultCase = defaultCase;

        // CASES
        final Set<ChoiceCaseNode> cases = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        for (ChoiceCaseBuilder caseBuilder : caseBuilders) {
            cases.add(caseBuilder.build());
        }
        instance.cases = ImmutableSet.copyOf(cases);

        // AUGMENTATIONS
        for (AugmentationSchemaBuilder builder : augmentationBuilders) {
            augmentations.add(builder.build());
        }
        instance.augmentations = ImmutableSet.copyOf(augmentations);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    public Set<ChoiceCaseBuilder> getCases() {
        return caseBuilders;
    }

    /**
     * Get case by name.
     *
     * @param caseName
     *            name of case to search
     * @return case with given name if present, null otherwise
     */
    public ChoiceCaseBuilder getCaseNodeByName(final String caseName) {
        for (ChoiceCaseBuilder addedCase : caseBuilders) {
            if (addedCase.getQName().getLocalName().equals(caseName)) {
                return addedCase;
            }
        }
        return null;
    }

    /**
     * Add case node to this choice.
     *
     * If node is not declared with 'case' keyword, create new case builder and
     * make this node child of newly created case.
     *
     * @param caseNode
     *            case node
     */
    public void addCase(final DataSchemaNodeBuilder caseNode) {
        QName caseQName = caseNode.getQName();
        String caseName = caseQName.getLocalName();

        for (ChoiceCaseBuilder existingCase : caseBuilders) {
            if (existingCase.getQName().getLocalName().equals(caseName)) {
                throw new YangParseException(caseNode.getModuleName(), caseNode.getLine(), "Can not add '" + caseNode
                        + "' to node '" + qname.getLocalName() + "' in module '" + getModuleName()
                        + "': case with same name already declared at line " + existingCase.getLine());
            }
        }

        if (caseNode instanceof ChoiceCaseBuilder) {
            caseBuilders.add((ChoiceCaseBuilder) caseNode);
        } else {
            ChoiceCaseBuilder caseBuilder = new ChoiceCaseBuilder(caseNode.getModuleName(), caseNode.getLine(),
                    caseQName, caseNode.getPath());
            if (caseNode.isAugmenting()) {
                // if node is added by augmentation, set case builder augmenting
                // as true and node augmenting as false
                caseBuilder.setAugmenting(true);
                caseNode.setAugmenting(false);
            }
            SchemaPath newPath = caseNode.getPath().createChild(caseQName);
            caseNode.setPath(newPath);
            caseBuilder.addChildNode(caseNode);
            caseBuilders.add(caseBuilder);
        }
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public void setAugmenting(final boolean augmenting) {
        this.augmenting = augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(final boolean configuration) {
        this.configuration = configuration;
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    @Override
    public void addAugmentation(final AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    public List<AugmentationSchemaBuilder> getAugmentationBuilders() {
        return augmentationBuilders;
    }

    public String getDefaultCase() {
        return defaultCase;
    }

    public void setDefaultCase(final String defaultCase) {
        this.defaultCase = defaultCase;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
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
        ChoiceBuilder other = (ChoiceBuilder) obj;
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "choice " + qname.getLocalName();
    }

    private static final class ChoiceNodeImpl implements ChoiceNode {
        private final QName qname;
        private final SchemaPath path;
        private String description;
        private String reference;
        private Status status;
        private boolean augmenting;
        private boolean addedByUses;
        private boolean configuration;
        private ConstraintDefinition constraints;
        private ImmutableSet<ChoiceCaseNode> cases;
        private ImmutableSet<AugmentationSchema> augmentations;
        private ImmutableList<UnknownSchemaNode> unknownNodes;
        private String defaultCase;

        private ChoiceNodeImpl(final QName qname, final SchemaPath path) {
            this.qname = qname;
            this.path = path;
        }

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public boolean isAugmenting() {
            return augmenting;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        @Override
        public boolean isConfiguration() {
            return configuration;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return constraints;
        }

        @Override
        public Set<AugmentationSchema> getAvailableAugmentations() {
            return augmentations;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public Set<ChoiceCaseNode> getCases() {
            return cases;
        }

        @Override
        public ChoiceCaseNode getCaseNodeByName(final QName name) {
            if (name == null) {
                throw new IllegalArgumentException("Choice Case QName cannot be NULL!");
            }
            for (final ChoiceCaseNode caseNode : cases) {
                if (caseNode != null && name.equals(caseNode.getQName())) {
                    return caseNode;
                }
            }
            return null;
        }

        @Override
        public ChoiceCaseNode getCaseNodeByName(final String name) {
            if (name == null) {
                throw new IllegalArgumentException("Choice Case string Name cannot be NULL!");
            }
            for (final ChoiceCaseNode caseNode : cases) {
                if (caseNode != null && (caseNode.getQName() != null)
                        && name.equals(caseNode.getQName().getLocalName())) {
                    return caseNode;
                }
            }
            return null;
        }

        @Override
        public String getDefaultCase() {
            return defaultCase;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
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
            ChoiceNodeImpl other = (ChoiceNodeImpl) obj;
            if (qname == null) {
                if (other.qname != null) {
                    return false;
                }
            } else if (!qname.equals(other.qname)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(ChoiceNodeImpl.class.getSimpleName());
            sb.append("[");
            sb.append("qname=" + qname);
            sb.append("]");
            return sb.toString();
        }
    }

}
