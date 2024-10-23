package dev.rollczi.litecommands.argument.resolver.nullable;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.parser.Parser;
import dev.rollczi.litecommands.argument.parser.ParserRegistry;
import dev.rollczi.litecommands.argument.profile.ProfiledMultipleArgumentResolver;
import dev.rollczi.litecommands.argument.suggester.Suggester;
import dev.rollczi.litecommands.argument.suggester.SuggesterRegistry;
import dev.rollczi.litecommands.input.raw.RawInput;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.range.Range;
import dev.rollczi.litecommands.reflect.type.TypeToken;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;

public class NullableArgumentResolver<SENDER> extends ProfiledMultipleArgumentResolver<SENDER, Object, NullableProfile> {

    private final ParserRegistry<SENDER> parserRegistry;
    private final SuggesterRegistry<SENDER> suggesterRegistry;

    public NullableArgumentResolver(ParserRegistry<SENDER> parserRegistry, SuggesterRegistry<SENDER> suggesterRegistry) {
        super(NullableProfile.NAMESPACE);
        this.parserRegistry = parserRegistry;
        this.suggesterRegistry = suggesterRegistry;
    }

    @Override
    protected ParseResult<Object> parse(Invocation<SENDER> invocation, Argument<Object> argument, RawInput rawInput, NullableProfile unused) {
        TypeToken<Object> optionalType = argument.getType();

        return parseValue(optionalType, invocation, argument, rawInput);
    }

    private <E> ParseResult<E> parseValue(TypeToken<E> type, Invocation<SENDER> invocation, Argument<Object> optionalArgument, RawInput input) {
        Argument<E> argument = Argument.of(optionalArgument.getName(), type);
        Parser<SENDER, E> parser = parserRegistry.getParser(argument);
        ParseResult<E> parseResult = parser.parse(invocation, argument, input);

        return parseResult
            .mapFailure(failure -> failure == InvalidUsage.Cause.MISSING_ARGUMENT
                ? ParseResult.successNull()
                : ParseResult.failure(failure)
            );
    }

    @Override
    protected SuggestionResult suggest(Invocation<SENDER> invocation, Argument<Object> argument, SuggestionContext context, NullableProfile nullableProfile) {
        TypeToken<Object> type = argument.getType();
        Suggester<SENDER, Object> suggester = suggesterRegistry.getSuggester(type.getRawType(), argument.getKey().withDefaultNamespace());

        return suggester.suggest(invocation, argument, context);
    }

    @Override
    protected Range getRange(Argument<Object> argument, NullableProfile unused) {
        return Range.range(0, 1);
    }

}