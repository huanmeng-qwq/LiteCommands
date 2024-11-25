package dev.rollczi.litecommands.jda;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.parser.ParseResultAccessor;
import dev.rollczi.litecommands.argument.parser.input.ParseableInputMatcher;
import dev.rollczi.litecommands.input.raw.RawInput;
import dev.rollczi.litecommands.argument.parser.Parser;
import dev.rollczi.litecommands.argument.parser.input.ParseableInput;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.range.Range;
import dev.rollczi.litecommands.reflect.ReflectUtil;
import java.util.function.Supplier;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JDAParseableInput extends AbstractJDAInput<JDAParseableInput.JDAInputMatcher> implements ParseableInput<JDAParseableInput.JDAInputMatcher> {

    private final JDACommandTranslator.JDALiteCommand command;

    JDAParseableInput(List<String> routes, Map<String, OptionMapping> arguments, JDACommandTranslator.JDALiteCommand command) {
        super(routes, arguments);
        this.command = command;
    }

    @Override
    public JDAInputMatcher createMatcher() {
        return new JDAInputMatcher();
    }

    class JDAInputMatcher extends AbstractJDAInput<JDAInputMatcher>.AbstractJDAMatcher implements ParseableInputMatcher<JDAInputMatcher> {
        private final Set<String> consumedArguments = new HashSet<>();

        JDAInputMatcher() {}

        JDAInputMatcher(int routePosition) {
            this.routePosition = routePosition;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <SENDER, T> ParseResult<T> nextArgument(Invocation<SENDER> invocation, Argument<T> argument, Supplier<Parser<SENDER, T>> parserProvider, ParseResultAccessor accessor) {
            OptionMapping optionMapping = arguments.get(argument.getName());

            if (optionMapping == null) {
                Parser<SENDER, T> parser = parserProvider.get();
                Range range = parser.getRange(argument);
                ParseResult<T> defaultResult = argument.getDefaultValue()
                    .orElseGet(() -> ParseResult.failure(InvalidUsage.Cause.MISSING_ARGUMENT));

                if (range.getMin() == 0) {
                    return parser.parse(invocation, argument, RawInput.of())
                        .mapFailure(failure -> defaultResult);
                }

                return defaultResult;
            }

            Class<T> type = argument.getType().getRawType();
            Object input = command.mapArgument(toRoute(argument.getName()), optionMapping, invocation);

            consumedArguments.add(argument.getName());

            if (ReflectUtil.instanceOf(input, type)) {
                return ParseResult.success((T) input);
            }

            Parser<SENDER, T> parser = parserProvider.get();

            return parser.parse(invocation, argument, RawInput.of(optionMapping.getAsString().split(" ")));
        }

        private JDACommandTranslator.JDARoute toRoute(String argumentName) {
            if (routes.isEmpty()) {
                return new JDACommandTranslator.JDARoute("", "", argumentName);
            }

            if (routes.size() == 1) {
                return new JDACommandTranslator.JDARoute("", routes.get(0), argumentName);
            }

            if (routes.size() == 2) {
                return new JDACommandTranslator.JDARoute(routes.get(0), routes.get(1), argumentName);
            }

            throw new IllegalArgumentException("Cannot convert to route");
        }

        @Override
        public JDAInputMatcher copy() {
            return new JDAInputMatcher(routePosition);
        }

        @Override
        public EndResult endMatch(boolean isStrict) {
            if (consumedArguments.size() != arguments.size() && isStrict) {
                return EndResult.failed(InvalidUsage.Cause.MISSING_ARGUMENT);
            }

            return EndResult.success();
        }

    }

}
