package dev.rollczi.litecommands.argument.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ParseResultAccessor {
    @NotNull
    <T> List<T> getByType(Class<T> clazz);

    @Nullable
    <T> T firstByType(Class<T> clazz);

    @Nullable
    <T> T getByIndex(int index, Class<T> clazz);

    @Nullable
    <T> T getByIndex(int index);

    @Nullable
    <T> T getByName(String name, Class<T> clazz);

    @Nullable
    <T> T getByName(String name);


}
