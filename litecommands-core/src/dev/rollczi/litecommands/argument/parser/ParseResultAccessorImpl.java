package dev.rollczi.litecommands.argument.parser;

import dev.rollczi.litecommands.reflect.type.TypeIndex;
import dev.rollczi.litecommands.reflect.type.TypeRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseResultAccessorImpl implements ParseResultAccessor {
    private final TypeIndex<Object> arguments = new TypeIndex<>();
    private final Map<String, Object> byKey = new HashMap<>();
    private final List<Object> byIndexed = new ArrayList<>();

    public ParseResultAccessorImpl add(TypeRange<?> range, String argumentKey, Object argument) {
        this.arguments.put(range, argument);
        this.byKey.put(argumentKey, argument);
        this.byIndexed.add(argument);
        return this;
    }

    public boolean match(TypeRange<?> range) {
        for (Object o : byIndexed) {
            Class<?> type = o.getClass();
            if (range.isInRange(type)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> List<T> getByType(Class<T> clazz) {
        ArrayList<T> list = new ArrayList<>();
        for (Object o : arguments.get(clazz)) {
            list.add((T) o);
        }
        return list;
    }

    @Override
    public <T> @Nullable T firstByType(Class<T> clazz) {
        List<T> list = getByType(clazz);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getByIndex(int index, Class<T> clazz) {
        if (index >= byIndexed.size()) {
            return null;
        }
        Object o = byIndexed.get(index);
        if (clazz.isInstance(o)) {
            return (T) o;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getByIndex(int index) {
        if (index >= byIndexed.size()) {
            return null;
        }
        return (T) byIndexed.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getByName(String name, Class<T> clazz) {
        Object o = byKey.get(name);
        if (clazz.isInstance(o)) {
            return (T) o;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getByName(String name) {
        return (T) byKey.get(name);
    }
}
