package dev.rollczi.litecommands.requirement;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.DependencyParser;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.parser.ParseResultAccessor;
import dev.rollczi.litecommands.argument.parser.Parser;
import dev.rollczi.litecommands.argument.parser.ParserRegistry;
import dev.rollczi.litecommands.argument.parser.input.ParseableInputMatcher;
import dev.rollczi.litecommands.bind.BindRegistry;
import dev.rollczi.litecommands.bind.BindRequirement;
import dev.rollczi.litecommands.bind.BindResult;
import dev.rollczi.litecommands.command.executor.CommandExecutor;
import dev.rollczi.litecommands.context.ContextRegistry;
import dev.rollczi.litecommands.context.ContextRequirement;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.meta.Meta;
import dev.rollczi.litecommands.scheduler.Scheduler;
import dev.rollczi.litecommands.shared.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class ScheduledRequirementResolver<SENDER> {

    private final ContextRegistry<SENDER> contextRegistry;
    private final ParserRegistry<SENDER> parserRegistry;
    private final BindRegistry bindRegistry;
    private final Scheduler scheduler;

    ScheduledRequirementResolver(ContextRegistry<SENDER> contextRegistry, ParserRegistry<SENDER> parserRegistry, BindRegistry bindRegistry, Scheduler scheduler) {
        this.contextRegistry = contextRegistry;
        this.parserRegistry = parserRegistry;
        this.bindRegistry = bindRegistry;
        this.scheduler = scheduler;
    }

    @NotNull
    <MATCHER extends ParseableInputMatcher<MATCHER>> List<ScheduledRequirement<?>> prepareRequirements(CommandExecutor<SENDER> executor, Invocation<SENDER> invocation, MATCHER matcher, ParseResultAccessor accessor) {
        List<ScheduledRequirement<?>> requirements = new ArrayList<>();

        for (Argument<?> argument : executor.getArguments()) {
            Parser<SENDER, ?> parser = parserRegistry.getParser(argument);
            if (parser instanceof DependencyParser) {
                argument.meta().put(Meta.PARSER_DEPEND_RESULT_KEY, ((DependencyParser) parser).depends());
            }
            requirements.add(toScheduled(argument, () -> matchArgument(argument, invocation, matcher, accessor)));
        }

        for (ContextRequirement<?> contextRequirement : executor.getContextRequirements()) {
            requirements.add(toScheduled(contextRequirement, () -> matchContext(contextRequirement, invocation)));
        }

        for (BindRequirement<?> bindRequirement : executor.getBindRequirements()) {
            requirements.add(toScheduled(bindRequirement, () -> matchBind(bindRequirement)));
        }

        return requirements;
    }

    private ScheduledRequirement<?> toScheduled(Requirement<?> requirement, ThrowingSupplier<RequirementFutureResult<?>, Throwable> resultSupplier) {
        return new ScheduledRequirement<>(requirement, () -> scheduler.supply(requirement.meta().get(Meta.POLL_TYPE), resultSupplier));
    }

    private <T, MATCHER extends ParseableInputMatcher<MATCHER>> RequirementFutureResult<T> matchArgument(Argument<T> argument, Invocation<SENDER> invocation, MATCHER matcher, ParseResultAccessor accessor) {
        return matcher.nextArgument(invocation, argument, () -> parserRegistry.getParser(argument), accessor);
    }

    private <T> RequirementFutureResult<T> matchContext(ContextRequirement<T> contextRequirement, Invocation<SENDER> invocation) {
        return contextRegistry.provideContext(contextRequirement.getType().getRawType(), invocation);
    }

    private <T> RequirementFutureResult<?> matchBind(BindRequirement<T> bindRequirement) {
        Class<T> rawType = bindRequirement.getType().getRawType();
        BindResult<T> instance = bindRegistry.getInstance(rawType);

        if (instance.isOk()) {
            return ParseResult.success(instance.getSuccess());
        }

        return ParseResult.failure(instance.getError());
    }

}
