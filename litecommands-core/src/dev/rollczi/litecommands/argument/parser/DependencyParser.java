package dev.rollczi.litecommands.argument.parser;

import dev.rollczi.litecommands.reflect.type.TypeRange;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ApiStatus.Experimental
public interface DependencyParser {
    void configure(Builder builder);

    default ParseResultAccessor accessor() {
        throw new UnsupportedOperationException();
    }

    default ParserDependResult depends() {
        throw new UnsupportedOperationException();
    }

    @ApiStatus.Experimental
    class Builder {

        private final List<TypeRange<?>> depends;
        private final List<TypeRange<?>> optionalDepends;

        public Builder(List<TypeRange<?>> depends, List<TypeRange<?>> optionalDepends) {
            this.depends = depends;
            this.optionalDepends = optionalDepends;
        }

        public static Builder create() {
            return new Builder(new ArrayList<>(), new ArrayList<>());
        }

        public Builder depends(TypeRange<?>... depends) {
            this.depends.addAll(Arrays.asList(depends));
            return this;
        }

        public Builder depends(Class<?>... depends) {
            for (Class<?> depend : depends) {
                this.depends.add(TypeRange.same(depend));
            }
            return this;
        }

        public Builder optional(TypeRange<?>... optionalDepends){
            this.optionalDepends.addAll(Arrays.asList(optionalDepends));
            return this;
        }

        public Builder optional(Class<?>... optionalDepends){
            for (Class<?> optionalDepend : optionalDepends) {
                this.optionalDepends.add(TypeRange.same(optionalDepend));
            }
            return this;
        }

        public List<TypeRange<?>> depends() {
            return Collections.unmodifiableList(this.depends);
        }

        public List<TypeRange<?>> optionalDepends() {
            return Collections.unmodifiableList(optionalDepends);
        }
    }
}