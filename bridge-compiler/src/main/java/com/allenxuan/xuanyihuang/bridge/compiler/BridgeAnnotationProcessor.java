package com.allenxuan.xuanyihuang.bridge.compiler;

import com.allenxuan.xuanyihuang.bridge.annotation.InterfaceTarget;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.*;

public class BridgeAnnotationProcessor extends AbstractProcessor {
    public static final String IMPL_BUILDER_CLASS_SUFFIX = "$$$$ImplBuilder";

    /**
     * for writing .java file
     */
    private Filer mFiler;
    /**
     * for printing error message when annotation processing encounters an error
     */
    private Messager mMessager;

    private Map<String, TypeElement> mGeneratedClassesInfo;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mGeneratedClassesInfo = new HashMap<String, TypeElement>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(InterfaceTarget.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * process() may be invoked more than once, which leads to an exception throwing("Attempt to recreate a file for type XXXX")
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //get all element annotated with MessageReceive
        Set<? extends Element> interfaceTargetAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(InterfaceTarget.class);
        //filter out non-method elements
        Set<TypeElement> interfaceTargetAnnotatedTypes = ElementFilter.typesIn(interfaceTargetAnnotatedElements);

        for (TypeElement type : interfaceTargetAnnotatedTypes) {
            updateGeneratedClassedInfo(type);
        }

        generateJavaFiles();

        return true;
    }

    private boolean isInterfaceTargetAnnotatedTypeValid(TypeElement type) {
        if (type.getKind() != ElementKind.CLASS) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, type.getSimpleName() + " -> only class can be annotated with InterfaceTarget");
            return false;
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, type.getSimpleName() + "-> class annotated with InterfaceTarget cannot be abstract");
            return false;
        }
        if (!type.getModifiers().contains(Modifier.PUBLIC)) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, type.getSimpleName() + "-> class annotated with InterfaceTarget should be public");
            return false;
        }
        Element packageElement = type.getEnclosingElement();
        if (packageElement.getKind() != ElementKind.PACKAGE && !type.getModifiers().contains(Modifier.STATIC)) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, type.getSimpleName() + "-> class annotated with InterfaceTarget cannot be a inner class");
            return false;
        }

        return true;
    }

    private void updateGeneratedClassedInfo(TypeElement type) {
        if (isInterfaceTargetAnnotatedTypeValid(type)) {
            Element packageElement = type.getEnclosingElement();
            String classFullName = packageElement.toString() + "." + type.getSimpleName() + IMPL_BUILDER_CLASS_SUFFIX;
            if (!mGeneratedClassesInfo.containsKey(classFullName)) {
                mGeneratedClassesInfo.put(classFullName, type);
            }
        }
    }

    private void generateSingleJavaFile(String packageName, String className, TypeElement type) {
        String targetName = className.split("\\$\\$\\$\\$")[0];
        ClassName target = ClassName.get(packageName, targetName);
        ClassName implBuilder = ClassName.get("com.allenxuan.xuanyihuang.bridge.core", "ImplBuilder");

        InterfaceTarget interfaceTargetAnnotation = type.getAnnotation(InterfaceTarget.class);
        String interfaceFullName;
        String interfacePackage = "";
        String interfaceSimpleName = "";
        String tweakedClassName = className;
        try {
            interfaceTargetAnnotation.Interface();
        } catch (MirroredTypeException e) {
            interfaceFullName = e.getTypeMirror().toString();
            int lastDotIndex = interfaceFullName.lastIndexOf(".");
            interfacePackage = interfaceFullName.substring(0, lastDotIndex);
            interfaceSimpleName = interfaceFullName.substring(lastDotIndex + 1, interfaceFullName.length());
        }
        tweakedClassName = String.format("%s%s", interfaceSimpleName, IMPL_BUILDER_CLASS_SUFFIX);
        ClassName interfaceTarget = ClassName.get(interfacePackage, interfaceSimpleName);
        ParameterizedTypeName implBuilderWithGenericType = ParameterizedTypeName.get(implBuilder, interfaceTarget);


        MethodSpec.Builder buildImplMethodSpec = MethodSpec.methodBuilder("buildImpl")
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceTarget)
                .addStatement("return new $T()", target);


        TypeSpec typeSpec = TypeSpec.classBuilder(tweakedClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(implBuilderWithGenericType)
                .addMethod(buildImplMethodSpec.build())
                .build();

        JavaFile javaFile = JavaFile.builder(interfacePackage, typeSpec)
                .build();


        try {
            javaFile.writeTo(mFiler);
        } catch (Throwable throwable) {
            //process() may be invoked more than once, which leads to an exception throwing("Attempt to recreate a file for type XXXX")
//            mMessager.printMessage(Diagnostic.Kind.WARNING, "generate java file error, cause: " + throwable.getCause() + ", message: " + throwable.getMessage());
        }
    }

    private void generateJavaFiles() {
        Iterator<Map.Entry<String, TypeElement>> iterator = mGeneratedClassesInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TypeElement> entry = iterator.next();
            int lastDotIndex = entry.getKey().lastIndexOf(".");
            String packageName = entry.getKey().substring(0, lastDotIndex);
            String className = entry.getKey().substring(lastDotIndex + 1, entry.getKey().length());
            generateSingleJavaFile(packageName, className, entry.getValue());
        }
    }


}