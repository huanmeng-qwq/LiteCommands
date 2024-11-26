package dev.rollczi.litecommands.argument.parser;

import dev.rollczi.litecommands.reflect.type.TypeRange;

import java.util.Collections;
import java.util.List;

public class ParserDependResult {
    private List<TypeRange<?>> depends;
    private List<TypeRange<?>> optionalDepends;

    private ParserDependResult(List<TypeRange<?>> depends, List<TypeRange<?>> optionalDepends) {
        this.depends = depends;
        this.optionalDepends = optionalDepends;
    }

    public boolean contains(Class<?> clazz) {
        for (TypeRange<?> depend : depends) {
            if (depend.isInRange(clazz)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Object object) {
        return contains(object.getClass());
    }


    public static ParserDependResult of(DependencyParser.Builder builder) {
        return new ParserDependResult(builder.depends(), builder.optionalDepends());
    }

    public static ParserDependResult empty() {
        return new ParserDependResult(Collections.emptyList(), Collections.emptyList());
    }

    public int containsAll(ParseResultAccessorImpl accessor) {
        for (TypeRange<?> depend : depends) {
            if (!accessor.match(depend)) {
                return -1;
            }
        }
        int opt = 0;
        for (TypeRange<?> optionalDepend : optionalDepends) {
            if (!accessor.match(optionalDepend)) {
                opt++;
            }
        }
        return opt;
    }
}
