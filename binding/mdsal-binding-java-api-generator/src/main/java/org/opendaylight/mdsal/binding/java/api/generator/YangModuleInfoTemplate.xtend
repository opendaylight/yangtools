/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.getClassName
import static extension org.opendaylight.mdsal.binding.spec.naming.BindingMapping.getRootPackageName
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.MODULE_INFO_CLASS_NAME
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.MODULE_INFO_QNAMEOF_METHOD_NAME

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import java.util.Comparator
import java.util.HashSet
import java.util.Optional
import java.util.Set
import java.util.TreeMap
import java.util.function.Function
import org.eclipse.xtend.lib.annotations.Accessors
import org.gaul.modernizer_maven_annotations.SuppressModernizer
import org.opendaylight.yangtools.yang.binding.YangModuleInfo
import org.opendaylight.yangtools.yang.common.Revision
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.SchemaContext

/**
 * Template for {@link YangModuleInfo} implementation for a particular module. Aside from fulfilling that contract,
 * this class provides a static {@code createQName(String)} method, which is used by co-generated code to initialize
 * QNAME constants.
 */
@SuppressModernizer
class YangModuleInfoTemplate {
    static val Comparator<Optional<Revision>> REVISION_COMPARATOR =
        [ Optional<Revision> first, Optional<Revision> second | Revision.compare(first, second) ]

    // These are always imported. Note we need to import even java.lang members, as there can be conflicting definitions
    // in our package
    static val CORE_IMPORT_STR = '''
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.yang.binding.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
    '''
    static val EXT_IMPORT_STR = '''
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import java.util.HashSet;
        import java.util.Set;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.yang.binding.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
    '''

    val Module module
    val SchemaContext ctx
    val Function<Module, Optional<String>> moduleFilePathResolver

    var importedTypes = CORE_IMPORT_STR

    @Accessors
    val String packageName

    @Accessors
    val String modelBindingProviderName

    new(Module module, SchemaContext ctx, Function<Module, Optional<String>> moduleFilePathResolver) {
        Preconditions.checkArgument(module !== null, "Module must not be null.")
        this.module = module
        this.ctx = ctx
        this.moduleFilePathResolver = moduleFilePathResolver
        packageName = module.QNameModule.rootPackageName;
        modelBindingProviderName = '''«packageName».«MODEL_BINDING_PROVIDER_CLASS_NAME»'''
    }

    def String generate() {
        val Set<Module> submodules = new HashSet
        collectSubmodules(submodules, module)

        val body = '''
            public final class «MODULE_INFO_CLASS_NAME» extends ResourceYangModuleInfo {
                «val rev = module.revision»
                private static final @NonNull QName NAME = QName.create("«module.namespace.toString»", «IF rev.present»"«rev.get.toString»", «ENDIF»"«module.name»").intern();
                private static final @NonNull YangModuleInfo INSTANCE = new «MODULE_INFO_CLASS_NAME»();

                private final @NonNull ImmutableSet<YangModuleInfo> importedModules;

                public static YangModuleInfo getInstance() {
                    return INSTANCE;
                }

                public static @NonNull QName «MODULE_INFO_QNAMEOF_METHOD_NAME»(final String localName) {
                    return QName.create(NAME, localName).intern();
                }

                «classBody(module, MODULE_INFO_CLASS_NAME, submodules)»
            }
        '''
        return '''
            package «packageName»;

            «importedTypes»

            «body»
        '''.toString
    }

    def String generateModelProvider() '''
        package «packageName»;

        import java.lang.Override;
        import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
        import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

        public final class «MODEL_BINDING_PROVIDER_CLASS_NAME» implements YangModelBindingProvider {
            @Override
            public YangModuleInfo getModuleInfo() {
                return «MODULE_INFO_CLASS_NAME».getInstance();
            }
        }
    '''

    private static def void collectSubmodules(Set<Module> dest, Module module) {
        for (Module submodule : module.submodules) {
            if (dest.add(submodule)) {
                collectSubmodules(dest, submodule)
            }
        }
    }

    private def CharSequence classBody(Module m, String className, Set<Module> submodules) '''
        private «className»() {
            «IF !m.imports.empty || !submodules.empty»
                «extendImports»
                Set<YangModuleInfo> set = new HashSet<>();
            «ENDIF»
            «IF !m.imports.empty»
                «FOR imp : m.imports»
                    «val name = imp.moduleName»
                    «val rev = imp.revision»
                    «IF !rev.present»
                        «val Set<Module> modules = ctx.modules»
                        «val TreeMap<Optional<Revision>, Module> sorted = new TreeMap(REVISION_COMPARATOR)»
                        «FOR module : modules»
                            «IF module.name.equals(name)»
                                «sorted.put(module.revision, module)»
                            «ENDIF»
                        «ENDFOR»
                        set.add(«sorted.lastEntry().value.QNameModule.rootPackageName».«MODULE_INFO_CLASS_NAME».getInstance());
                    «ELSE»
                        set.add(«(ctx.findModule(name, rev).get.QNameModule).rootPackageName».«MODULE_INFO_CLASS_NAME».getInstance());
                    «ENDIF»
                «ENDFOR»
            «ENDIF»
            «FOR submodule : submodules»
                set.add(«submodule.name.className»Info.getInstance());
            «ENDFOR»
            «IF m.imports.empty && submodules.empty»
                importedModules = ImmutableSet.of();
            «ELSE»
                importedModules = ImmutableSet.copyOf(set);
            «ENDIF»
        }

        @Override
        public QName getName() {
            return NAME;
        }

        @Override
        protected String resourceName() {
            return "«sourcePath(m)»";
        }

        @Override
        public ImmutableSet<YangModuleInfo> getImportedModules() {
            return importedModules;
        }
        «generateSubInfo(submodules)»
    '''

    private def void extendImports() {
        importedTypes = EXT_IMPORT_STR
    }

    private def sourcePath(Module module) {
        val opt = moduleFilePathResolver.apply(module)
        Preconditions.checkState(opt.isPresent, "Module %s does not have a file path", module)
        return opt.get
    }

    private def generateSubInfo(Set<Module> submodules) '''
        «FOR submodule : submodules»
            «val className = submodule.name.className»

            private static final class «className»Info extends ResourceYangModuleInfo {
                «val rev = submodule.revision»
                private final @NonNull QName NAME = QName.create("«submodule.namespace.toString»", «
                IF rev.present»"«rev.get.toString»", «ENDIF»"«submodule.name»").intern();
                private static final @NonNull YangModuleInfo INSTANCE = new «className»Info();

                private final @NonNull ImmutableSet<YangModuleInfo> importedModules;

                public static @NonNull YangModuleInfo getInstance() {
                    return INSTANCE;
                }

                «classBody(submodule, className + "Info", ImmutableSet.of)»
            }
        «ENDFOR»
    '''
}
