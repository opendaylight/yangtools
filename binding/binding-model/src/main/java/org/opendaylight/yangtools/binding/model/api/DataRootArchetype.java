/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;

/**
 * The {@link Archetype} for {@link DataRoot} specializations.
 * @since 15.0.0
 */
@NonNullByDefault
public record DataRootArchetype() implements Archetype {
    public DataRootArchetype {

    }


    @Override
    public TypeComment getComment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public List<Type> getImplements() {
        return List.of();
    }

    @Override
    public List<GeneratedType> getEnclosedTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Enumeration> getEnumerations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Constant> getConstantDefinitions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodSignature> getMethodDefinitions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeneratedProperty> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<YangSourceDefinition> getYangSourceDefinition() {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public @NonNull JavaTypeName getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModuleName() {
        // TODO Auto-generated method stub
        return null;
    }
}
