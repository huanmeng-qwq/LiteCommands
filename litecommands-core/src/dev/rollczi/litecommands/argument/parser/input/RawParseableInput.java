package dev.rollczi.litecommands.argument.parser.input;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.parser.ParseResultAccessor;
import dev.rollczi.litecommands.argument.parser.Parser;
import dev.rollczi.litecommands.input.raw.RawInputAnalyzer;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;

import dev.rollczi.litecommands.priority.PriorityLevel;
import dev.rollczi.litecommands.shared.FailedReason;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

class RawParseableInput implements ParseableInput<RawParseableInput.RawInputMatcher> {

    private final List<String> rawArguments;

    public RawParseableInput(List<String> rawArguments) {
        this.rawArguments = new ArrayList<>(rawArguments);
    }

    @Override
    public RawInputMatcher createMatcher() {
        return new RawInputMatcher();
    }

    @Override
    public List<String> asList() {
        return Collections.unmodifiableList(this.rawArguments);
    }

    public class RawInputMatcher implements ParseableInputMatcher<RawInputMatcher> {

        private final RawInputAnalyzer rawInputAnalyzer = new RawInputAnalyzer(rawArguments);

        public RawInputMatcher() {}

        public RawInputMatcher(int pivotPosition) {
            this.rawInputAnalyzer.setPivotPosition(pivotPosition);
        }

        @Override
        public <SENDER, PARSED> ParseResult<PARSED> nextArgument(Invocation<SENDER> invocation, Argument<PARSED> argument, Supplier<Parser<SENDER, PARSED>> parserProvider, ParseResultAccessor accessor) {
            RawInputAnalyzer.Context<SENDER, PARSED> context = rawInputAnalyzer.toContext(argument, parserProvider.get());

            if (context.isMissingFullArgument()) {
                Optional<ParseResult<PARSED>> optional = argument.getDefaultValue();

                return optional
                    .orElseGet(() -> ParseResult.failure(InvalidUsage.Cause.MISSING_ARGUMENT));
            }

            if (context.isMissingPartOfArgument()) {
                return ParseResult.failure(InvalidUsage.Cause.MISSING_PART_OF_ARGUMENT);
            }

            return context.parseArgument(invocation);
        }

        @Override
        public boolean hasNextRoute() {
            return rawInputAnalyzer.hasNextRoute();
        }

        @Override
        public String showNextRoute() {
            return rawInputAnalyzer.showNextRoute();
        }

        @Override
        public String nextRoute() {
            return rawInputAnalyzer.nextRoute();
        }

        @Override
        public RawInputMatcher copy() {
            return new RawInputMatcher(rawInputAnalyzer.getPivotPosition());
        }

        @Override
        public EndResult endMatch(boolean isStrict) {

            if (rawInputAnalyzer.getPivotPosition() < rawArguments.size() && isStrict) {
                return EndResult.failed(FailedReason.of(InvalidUsage.Cause.TOO_MANY_ARGUMENTS, PriorityLevel.LOW));
            }

            return EndResult.success();
        }

    }

}
