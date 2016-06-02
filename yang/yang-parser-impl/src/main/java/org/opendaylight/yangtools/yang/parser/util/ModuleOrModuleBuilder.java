/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;

/**
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 *
 * This class needs to be removed and its sole user, {@link ModuleDependencySort}, use Module instances only.
 */
@Deprecated
class ModuleOrModuleBuilder {
    private final Optional<Module> maybeModule;
    private final Optional<ModuleBuilder> maybeModuleBuilder;

    ModuleOrModuleBuilder(final Module module) {
        maybeModule = Optional.of(module);
        maybeModuleBuilder = Optional.absent();
    }

    ModuleOrModuleBuilder(final ModuleBuilder moduleBuilder) {
        maybeModule = Optional.absent();
        maybeModuleBuilder = Optional.of(moduleBuilder);
    }
    boolean isModule(){
        return maybeModule.isPresent();
    }
    boolean isModuleBuilder(){
        return maybeModuleBuilder.isPresent();
    }
    Module getModule(){
        return maybeModule.get();
    }
    ModuleBuilder getModuleBuilder(){
        return maybeModuleBuilder.get();
    }

    static List<ModuleOrModuleBuilder> fromAll(final Collection<Module> modules, final Collection<ModuleBuilder> moduleBuilders) {
        List<ModuleOrModuleBuilder> result = new ArrayList<>(modules.size() + moduleBuilders.size());
        for (Module m: modules){
            result.add(new ModuleOrModuleBuilder(m));
        }
        for (ModuleBuilder mb : moduleBuilders) {
            result.add(new ModuleOrModuleBuilder(mb));
        }
        return result;
    }
}