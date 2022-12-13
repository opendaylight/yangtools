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
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import java.util.Comparator
import java.util.HashSet
import java.util.Optional
import java.util.Set
import java.util.TreeMap
import java.util.function.Function
import org.eclipse.xtend.lib.annotations.Accessors
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode
import org.opendaylight.yangtools.yang.binding.YangModuleInfo
import org.opendaylight.yangtools.yang.common.Revision
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.ModuleLike
import org.opendaylight.yangtools.yang.model.api.Submodule

/**
 * Template for {@link YangModuleInfo} implementation for a particular module. Aside from fulfilling that contract,
 * this class provides a static {@code createQName(String)} method, which is used by co-generated code to initialize
 * QNAME constants.
 */
final class YangModuleInfoTemplate {
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
    val EffectiveModelContext ctx
    val Function<ModuleLike, Optional<String>> moduleFilePathResolver
    val boolean hasYangData

    var importedTypes = CORE_IMPORT_STR

    @Accessors
    val String packageName

    @Accessors
    val String modelBindingProviderName

    new(Module module, EffectiveModelContext ctx, Function<ModuleLike, Optional<String>> moduleFilePathResolver) {
        Preconditions.checkArgument(module !== null, "Module must not be null.")
        this.module = module
        this.ctx = ctx
        this.moduleFilePathResolver = moduleFilePathResolver
        packageName = module.QNameModule.rootPackageName;
        modelBindingProviderName = '''«packageName».«MODEL_BINDING_PROVIDER_CLASS_NAME»'''
        hasYangData = module.unknownSchemaNodes.stream.anyMatch([s | s instanceof YangDataSchemaNode])
    }

    def String generate() {
        val Set<Submodule> submodules = new HashSet
        collectSubmodules(submodules, module)

        val body = '''
            /**
             * The {@link ResourceYangModuleInfo} for {@code «module.name»} module.
             */
            @«JavaFileTemplate.GENERATED»("mdsal-binding-generator")
            public final class «MODULE_INFO_CLASS_NAME» extends ResourceYangModuleInfo {
                «val rev = module.revision»
                private static final @NonNull QName NAME = QName.create("«module.QNameModule.namespace.toString»", «IF rev.present»"«rev.get.toString»", «ENDIF»"«module.name»").intern();
                private static final @NonNull YangModuleInfo INSTANCE = new «MODULE_INFO_CLASS_NAME»();

                private final @NonNull ImmutableSet<YangModuleInfo> importedModules;

                /**
                 * Return the singleton instance of this class.
                 *
                 * @return The singleton instance
                 */
                public static @NonNull YangModuleInfo getInstance() {
                    return INSTANCE;
                }

                /**
                 * Create an interned {@link QName} with specified {@code localName} and namespace/revision of this
                 * module.
                 *
                 * @param localName local name
                 * @return A QName
                 * @throws NullPointerException if {@code localName} is {@code null}
                 * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
                 */
                public static @NonNull QName «MODULE_INFO_QNAMEOF_METHOD_NAME»(final String localName) {
                    return QName.create(NAME, localName).intern();
                }
            «IF hasYangData»

                /**
                 * Create an interned {@link YangDataName} with specified {@code templateName} and namespace/revision of
                 * this module.
                 *
                 * @param templateName template name
                 * @return A YangDataName
                 * @throws NullPointerException if {@code templateName} is {@code null}
                 * @throws IllegalArgumentException if {@code templateName} is empty
                 */
                public static @NonNull YangDataName «MODULE_INFO_YANGDATANAMEOF_METHOD_NAME»(final String templateName) {
                    return new YangDataName(NAME.getModule(), templateName).intern();
                }
            «ENDIF»

                «classBody(module, MODULE_INFO_CLASS_NAME, submodules)»
            }
        '''
        return '''
            package «packageName»;

            «importedTypes»
            «IF hasYangData»
            import org.opendaylight.yangtools.yang.common.YangDataName;
            «ENDIF»

            «body»
        '''.toString
    }

    def String generateModelProvider() '''
        package «packageName»;

        import java.lang.Override;
        import java.util.ServiceLoader;
        import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
        import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

        /**
         * The {@link YangModelBindingProvider} for {@code «module.name»} module. This class should not be used
         * directly, but rather through {@link ServiceLoader}.
         */
        @«JavaFileTemplate.GENERATED»("mdsal-binding-generator")
        public final class «MODEL_BINDING_PROVIDER_CLASS_NAME» implements YangModelBindingProvider {
            /**
             * Construct a new provider.
             */
            public «MODEL_BINDING_PROVIDER_CLASS_NAME»() {
                // No-op
            }

            @Override
            public YangModuleInfo getModuleInfo() {
                return «MODULE_INFO_CLASS_NAME».getInstance();
            }
        }
    '''

    private static def void collectSubmodules(Set<Submodule> dest, ModuleLike module) {
        for (Submodule submodule : module.submodules) {
            if (dest.add(submodule)) {
                collectSubmodules(dest, submodule)
            }
        }
    }

    private def CharSequence classBody(ModuleLike m, String className, Set<Submodule> submodules) '''
        private «className»() {
            «IF !m.imports.empty || !submodules.empty»
                «extendImports»
                Set<YangModuleInfo> set = new HashSet<>();
            «ENDIF»
            «IF !m.imports.empty»
                «FOR imp : m.imports»
                    «val name = imp.moduleName.localName»
                    «val rev = imp.revision»
                    «IF !rev.present»
                        «val TreeMap<Optional<Revision>, Module> sorted = new TreeMap(REVISION_COMPARATOR)»
                        «FOR module : ctx.modules»
                            «IF name.equals(module.name)»
                                «sorted.put(module.revision, module)»
                            «ENDIF»
                        «ENDFOR»
                        set.add(«sorted.lastEntry().value.QNameModule.rootPackageName».«MODULE_INFO_CLASS_NAME».getInstance());
                    «ELSE»
                        set.add(«(ctx.findModule(name, rev).orElseThrow.QNameModule).rootPackageName».«MODULE_INFO_CLASS_NAME».getInstance());
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

    private def sourcePath(ModuleLike module) {
        val opt = moduleFilePathResolver.apply(module)
        Preconditions.checkState(opt.isPresent, "Module %s does not have a file path", module)
        return opt.get
    }

    private def generateSubInfo(Set<Submodule> submodules) '''
        «FOR submodule : submodules»
            «val className = submodule.name.className»

            private static final class «className»Info extends ResourceYangModuleInfo {
                «val rev = submodule.revision»
                private final @NonNull QName NAME = QName.create("«submodule.QNameModule.namespace.toString»", «
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
