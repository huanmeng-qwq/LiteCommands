package dev.rollczi.litecommands.argument.parser;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.input.raw.RawInput;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.range.Range;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class DependencyParserRedirect<SENDER, PARSED, T extends Parser<SENDER, PARSED> & DependencyParser> implements Parser<SENDER, PARSED>, DependencyParser {
    private static final ByteClassLoader CLASS_LOADER = new ByteClassLoader(new URL[0], DependencyParserRedirect.class.getClassLoader());
    private static final Map<String, Class<?>> CACHE_CLASS = new HashMap<>();
    private final T parser;

    private ParseResultAccessor accessor;
    private ParserDependResult depends;

    private Field fieldAccessor;
    private Field fieldDepends;

    public DependencyParserRedirect(Parser<SENDER, PARSED> parser) {
        this.parser = proxy(parser);
    }

    @SuppressWarnings("unchecked")
    private T proxy(Parser<SENDER, PARSED> parent) {
        Class<? extends Parser> clazz = parent.getClass();
        String name = clazz.getName();
        String superName = name.replace(".", "/");
        String classFileName = superName + "$Impl";
        String className = name + "$Impl";
        Class<?> loadClass;
        if (CACHE_CLASS.containsKey(className)) {
            loadClass = CACHE_CLASS.get(className);
        } else {
            byte[] bytes = AsmGenerator.make(classFileName, clazz.getSimpleName(), superName);
            CLASS_LOADER.extraClassDefs.put(className, bytes);
            try {
                loadClass = CLASS_LOADER.loadClass(className);
                CACHE_CLASS.put(className, loadClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            fieldAccessor = loadClass.getDeclaredField("accessor");
            fieldDepends = loadClass.getDeclaredField("depends");
            Constructor<?> constructor = loadClass.getConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure(Builder builder) {
        parser.configure(builder);
        this.depends = ParserDependResult.of(builder);
        try {
            this.fieldDepends.set(parser, depends);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void setAccessor(ParseResultAccessor accessor) {
        this.accessor = accessor;
        try {
            this.fieldAccessor.set(parser, accessor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ParseResultAccessor accessor() {
        return accessor;
    }

    @Override
    public ParserDependResult depends() {
        return depends;
    }

    @Override
    public ParseResult<PARSED> parse(Invocation<SENDER> invocation, Argument<PARSED> argument, RawInput input) {
        return parser.parse(invocation, argument, input);
    }

    @Override
    public Range getRange(Argument<PARSED> parsedArgument) {
        return parser.getRange(parsedArgument);
    }

    @Override
    public boolean canParse(Argument<PARSED> argument) {
        return parser.canParse(argument);
    }

    @Override
    public boolean match(Invocation<SENDER> invocation, Argument<PARSED> argument, RawInput input) {
        return parser.match(invocation, argument, input);
    }

    private static class AsmGenerator {
        static byte[] make(String className, String fileName, String superClass) {
            ClassWriter classWriter = new ClassWriter(0);
            FieldVisitor fieldVisitor;
            MethodVisitor methodVisitor;

            String descriptor = "L" + className + ";";
            String ParserDependResult = dev.rollczi.litecommands.argument.parser.ParserDependResult.class.getName().replace(".", "/");
            String ParseResultAccessor = dev.rollczi.litecommands.argument.parser.ParseResultAccessor.class.getName().replace(".", "/");

            classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, superClass, null);

            classWriter.visitSource(fileName, null);

            {
                fieldVisitor = classWriter.visitField(Opcodes.ACC_PUBLIC, "accessor", "L" + ParseResultAccessor + ";", null, null);
                fieldVisitor.visitEnd();
            }
            {
                fieldVisitor = classWriter.visitField(Opcodes.ACC_PUBLIC, "depends", "L" + ParserDependResult + ";", null, null);
                fieldVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(64, label0);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass, "<init>", "()V", false);
                methodVisitor.visitInsn(Opcodes.RETURN);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLocalVariable("this", descriptor, null, label0, label1, 0);
                methodVisitor.visitMaxs(1, 1);
                methodVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "accessor", "()L" + ParseResultAccessor + ";", null, null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(69, label0);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, className, "accessor", "L" + ParseResultAccessor + ";");
                methodVisitor.visitInsn(Opcodes.ARETURN);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLocalVariable("this", descriptor, null, label0, label1, 0);
                methodVisitor.visitMaxs(1, 1);
                methodVisitor.visitEnd();
            }
            {
                methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "depends", "()L" + ParserDependResult + ";", null, null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(74, label0);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, className, "depends", "L" + ParserDependResult + ";");
                methodVisitor.visitInsn(Opcodes.ARETURN);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLocalVariable("this", descriptor, null, label0, label1, 0);
                methodVisitor.visitMaxs(1, 1);
                methodVisitor.visitEnd();
            }
            classWriter.visitEnd();
            return classWriter.toByteArray();
        }
    }

    public static class ByteClassLoader extends URLClassLoader {
        private final Map<String, byte[]> extraClassDefs;

        public ByteClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.extraClassDefs = new HashMap<>();
        }

        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            byte[] classBytes = this.extraClassDefs.remove(name);
            if (classBytes != null) {
                return defineClass(name, classBytes, 0, classBytes.length);
            }
            return super.findClass(name);
        }

    }
}
