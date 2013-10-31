/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class ModuleContext {
    private GeneratedTypeBuilder moduleNode;
    private final List<GeneratedTOBuilder> genTOs = new ArrayList<GeneratedTOBuilder>();
    private final Map<SchemaPath, Type> typedefs = new HashMap<SchemaPath, Type>();
    private final Map<SchemaPath, GeneratedTypeBuilder> childNodes = new HashMap<SchemaPath, GeneratedTypeBuilder>();
    private final Map<SchemaPath, GeneratedTypeBuilder> groupings = new HashMap<SchemaPath, GeneratedTypeBuilder>();
    private final Map<SchemaPath, GeneratedTypeBuilder> cases = new HashMap<SchemaPath, GeneratedTypeBuilder>();
    private final Set<GeneratedTOBuilder> identities = new HashSet<GeneratedTOBuilder>();
    private final Set<GeneratedTypeBuilder> topLevelNodes = new HashSet<GeneratedTypeBuilder>();
    private final List<GeneratedTypeBuilder> augmentations = new ArrayList<GeneratedTypeBuilder>();


    List<Type> getGeneratedTypes() {
        List<Type> result = new ArrayList<>();

        if (moduleNode != null) {
            result.add(moduleNode.toInstance());
        }

        for (GeneratedTOBuilder b : genTOs) {
            result.add(b.toInstance());
        }
        for (Type b : typedefs.values()) {
            if (b != null) {
                result.add(b);
            }
        }
        for (GeneratedTypeBuilder b : childNodes.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : groupings.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : cases.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTOBuilder b : identities) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : topLevelNodes) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : augmentations) {
            result.add(b.toInstance());
        }
        return result;
    }

    public GeneratedTypeBuilder getModuleNode() {
        return moduleNode;
    }

    public GeneratedTypeBuilder getChildNode(SchemaPath p) {
        return childNodes.get(p);
    }

    public GeneratedTypeBuilder getGrouping(SchemaPath p) {
        return groupings.get(p);
    }

    public GeneratedTypeBuilder getCase(SchemaPath p) {
        return cases.get(p);
    }

    public void addModuleNode(GeneratedTypeBuilder moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void addGeneratedTOBuilder(GeneratedTOBuilder b) {
        genTOs.add(b);
    }

    public void addChildNodeType(SchemaPath p, GeneratedTypeBuilder b) {
        childNodes.put(p, b);
    }

    public void addGroupingType(SchemaPath p, GeneratedTypeBuilder b) {
        groupings.put(p, b);
    }

    public void addTypedefType(SchemaPath p, Type t) {
        typedefs.put(p, t);
    }

    public void addCaseType(SchemaPath p, GeneratedTypeBuilder b) {
        cases.put(p, b);
    }

    public void addIdentityType(GeneratedTOBuilder b) {
        identities.add(b);
    }

    public void addTopLevelNodeType(GeneratedTypeBuilder b) {
        topLevelNodes.add(b);
    }

    public void addAugmentType(GeneratedTypeBuilder b) {
        augmentations.add(b);
    }

}
