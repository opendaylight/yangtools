/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator

import static org.opendaylight.yangtools.yang.binding.BindingMapping.MODEL_BINDING_PROVIDER_CLASS_NAME
import static org.opendaylight.yangtools.yang.binding.BindingMapping.MODULE_INFO_CLASS_NAME
import static org.opendaylight.yangtools.yang.binding.BindingMapping.MODULE_INFO_QNAMEOF_METHOD_NAME
import static extension org.opendaylight.yangtools.yang.binding.BindingMapping.getClassName
import static extension org.opendaylight.yangtools.yang.binding.BindingMapping.getRootPackageName

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import java.util.Collections
import java.util.Comparator
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.Map
import java.util.Optional
import java.util.Set
import java.util.TreeMap
import java.util.function.Function
import org.eclipse.xtend.lib.annotations.Accessors
import org.opendaylight.mdsal.binding.model.api.ParameterizedType
import org.opendaylight.mdsal.binding.model.api.Type
import org.opendaylight.mdsal.binding.model.api.WildcardType
import org.opendaylight.mdsal.binding.model.util.Types
import org.opendaylight.yangtools.yang.binding.ResourceYangModuleInfo
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider
import org.opendaylight.yangtools.yang.binding.YangModuleInfo
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.common.Revision
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.SchemaContext

/**
 * Template for {@link YangModuleInfo} implementation for a particular module. Aside from fulfilling that contract,
 * this class provides a static {@code createQName(String)} method, which is used by co-generated code to initialize
 * QNAME constants.
 */
class YangModuleInfoTemplate {
    static val Comparator<Optional<Revision>> REVISION_COMPARATOR =
        [ Optional<Revision> first, Optional<Revision> second | Revision.compare(first, second) ]

    val Module module
    val SchemaContext ctx
    val Map<String, String> importMap = new LinkedHashMap()
    val Function<Module, Optional<String>> moduleFilePathResolver

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
        val body = '''
            public final class «MODULE_INFO_CLASS_NAME» extends «ResourceYangModuleInfo.importedName» {
                «val rev = module.revision»
                private static final «QName.importedName» NAME = «QName.importedName».create("«module.namespace.toString»", «IF rev.present»"«rev.get.toString»", «ENDIF»"«module.name»").intern();
                private static final «YangModuleInfo.importedName» INSTANCE = new «MODULE_INFO_CLASS_NAME»();

                private final «Set.importedName»<«YangModuleInfo.importedName»> importedModules;

                public static «YangModuleInfo.importedName» getInstance() {
                    return INSTANCE;
                }

                public static «QName.importedName» «MODULE_INFO_QNAMEOF_METHOD_NAME»(final «String.importedName» localName) {
                    return «QName.importedName».create(NAME, localName).intern();
                }

                «classBody(module, MODULE_INFO_CLASS_NAME)»
            }
        '''
        return '''
            package «packageName»;

            «imports»

            «body»
        '''.toString
    }

    def String generateModelProvider() {
        '''
            package «packageName»;

            public final class «MODEL_BINDING_PROVIDER_CLASS_NAME» implements «YangModelBindingProvider.name» {
                @«Override.importedName»
                public «YangModuleInfo.name» getModuleInfo() {
                    return «MODULE_INFO_CLASS_NAME».getInstance();
                }
            }
        '''

    }

    private def CharSequence classBody(Module m, String className) '''
        private «className»() {
            «IF !m.imports.empty || !m.submodules.empty»
                «Set.importedName»<«YangModuleInfo.importedName»> set = new «HashSet.importedName»<>();
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
            «IF !m.submodules.empty»
                «FOR submodule : m.submodules»
                    set.add(«submodule.name.className»Info.getInstance());
                «ENDFOR»
            «ENDIF»
            «IF m.imports.empty && m.submodules.empty»
                importedModules = «Collections.importedName».emptySet();
            «ELSE»
                importedModules = «ImmutableSet.importedName».copyOf(set);
            «ENDIF»
        }

        @«Override.importedName»
        public «QName.importedName» getName() {
            return NAME;
        }

        @«Override.importedName»
        protected «String.importedName» resourceName() {
            return "«sourcePath(m)»";
        }

        @«Override.importedName»
        public «Set.importedName»<«YangModuleInfo.importedName»> getImportedModules() {
            return importedModules;
        }

        «generateSubInfo(m)»

    '''

    private def sourcePath(Module module) {
        val opt = moduleFilePathResolver.apply(module)
        Preconditions.checkState(opt.isPresent, "Module %s does not have a file path", module)
        return opt.get
    }

    private def imports() '''
        «IF !importMap.empty»
            «FOR entry : importMap.entrySet»
                «IF entry.value != module.QNameModule.rootPackageName»
                    import «entry.value».«entry.key»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    final protected def importedName(Class<?> cls) {
        val Type intype = Types.typeForClass(cls)
        putTypeIntoImports(intype)
        intype.explicitType
    }

    final def void putTypeIntoImports(Type type) {
        val String typeName = type.name
        val String typePackageName = type.packageName
        if (typePackageName.startsWith("java.lang") || typePackageName.empty) {
            return
        }
        if (!importMap.containsKey(typeName)) {
            importMap.put(typeName, typePackageName)
        }
        if (type instanceof ParameterizedType) {
            val Type[] params = type.actualTypeArguments
            if (params !== null) {
                for (Type param : params) {
                    putTypeIntoImports(param)
                }
            }
        }
    }

    final def String getExplicitType(Type type) {
        val String typePackageName = type.packageName
        val String typeName = type.name
        val String importedPackageName = importMap.get(typeName)
        var StringBuilder builder
        if (typePackageName.equals(importedPackageName)) {
            builder = new StringBuilder(typeName)
            if (builder.toString().equals("Void")) {
                return "void"
            }
            addActualTypeParameters(builder, type)
        } else {
            if (type.equals(Types.voidType())) {
                return "void"
            }
            builder = new StringBuilder()
            if (!typePackageName.empty) {
                builder.append(typePackageName).append(Constants.DOT).append(typeName)
            } else {
                builder.append(typeName)
            }
            addActualTypeParameters(builder, type)
        }
        return builder.toString()
    }

    final def StringBuilder addActualTypeParameters(StringBuilder builder, Type type) {
        if (type instanceof ParameterizedType) {
            val Type[] pTypes = type.actualTypeArguments
            builder.append('<').append(getParameters(pTypes)).append('>')
        }
        return builder
    }

    final def String getParameters(Type[] pTypes) {
        if (pTypes === null || pTypes.length == 0) {
            return "?"
        }
        val StringBuilder builder = new StringBuilder()

        var int i = 0
        for (pType : pTypes) {
            val Type t = pTypes.get(i)

            var String separator = ","
            if (i == (pTypes.length - 1)) {
                separator = ""
            }

            var String wildcardParam = ""
            if (t.equals(Types.voidType())) {
                builder.append("java.lang.Void").append(separator)
            } else {

                if (t instanceof WildcardType) {
                    wildcardParam = "? extends "
                }

                builder.append(wildcardParam).append(t.explicitType).append(separator)
                i = i + 1
            }
        }
        return builder.toString()
    }

    private def generateSubInfo(Module module) '''
        «FOR submodule : module.submodules»
            «val className = submodule.name.className»
            private static final class «className»Info extends «ResourceYangModuleInfo.importedName» {
                «val rev = submodule.revision»
                private final «QName.importedName» NAME = «QName.importedName».create("«
                    submodule.namespace.toString»", «IF rev.present»"«rev.get.toString»", «ENDIF»"«submodule.name»").intern();
                private static final «YangModuleInfo.importedName» INSTANCE = new «className»Info();

                private final «Set.importedName»<YangModuleInfo> importedModules;

                public static «YangModuleInfo.importedName» getInstance() {
                    return INSTANCE;
                }

                «classBody(submodule, className + "Info")»
            }
        «ENDFOR»
    '''
}
