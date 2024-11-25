package dev.rollczi.litecommands.argument.parser;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.input.raw.RawInput;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.range.Range;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DependencyParserRedirect<SENDER, PARSED, T extends Parser<SENDER, PARSED> & DependencyParser> implements Parser<SENDER, PARSED>, DependencyParser {
    private static final Method accessorMethod;
    private static final Method dependsMethod;

    static {
        try {
            accessorMethod = DependencyParser.class.getDeclaredMethod("accessor");
            dependsMethod = DependencyParser.class.getDeclaredMethod("depends");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final T parser;

    private ParseResultAccessor accessor;
    private ParserDependResult depends;

    public DependencyParserRedirect(Parser<SENDER, PARSED> parser) {
        this.parser = proxy((T) parser);
    }

    @SuppressWarnings("unchecked")
    private T proxy(T parent) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Parser.class, DependencyParser.class}, (proxy, method, args) -> {
            if (method == accessorMethod) {
                return accessor;
            }
            if (method == dependsMethod) {
                return depends;
            }
            return method.invoke(parent, args);
        });
    }

    @Override
    public void configure(Builder builder) {
        parser.configure(builder);
        this.depends = ParserDependResult.of(builder);
    }


    public void setAccessor(ParseResultAccessor accessor) {
        this.accessor = accessor;
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
}
