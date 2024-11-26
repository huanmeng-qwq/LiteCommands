package dev.rollczi.litecommands.argument.parser;

import dev.rollczi.litecommands.LiteCommandsException;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.programmatic.LiteCommand;
import dev.rollczi.litecommands.unit.TestSender;
import dev.rollczi.litecommands.unit.annotations.LiteTest;
import dev.rollczi.litecommands.unit.annotations.LiteTestSpec;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

@LiteTest
public class DependencyParserTest extends LiteTestSpec {
    static LiteTestConfig config = builder -> builder
        .argument(User.class, new UserParser())
        .argument(SomeClass.class, new SomeClassParser())
        .commands(new Command());

    static class SomeClass {
        private final User user;
        private final int id;

        public SomeClass(User user, int id) {
            this.user = user;
            this.id = id;
        }
    }

    static class User {
        private final String name;
        private final int age;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class UserParser extends ArgumentResolver<TestSender, User> {

        @Override
        protected ParseResult<User> parse(Invocation<TestSender> invocation, Argument<User> context, String argument) {
            return ParseResult.success(new User(argument, 1));
        }
    }

    public static class SomeClassParser extends ArgumentResolver<TestSender, SomeClass> implements DependencyParser {
        @Override
        public void configure(Builder builder) {
            builder.depends(User.class);
        }

        @Override
        protected ParseResult<SomeClass> parse(Invocation<TestSender> invocation, Argument<SomeClass> context, String argument) {
            @Nullable User user = accessor().firstByType(User.class);
            if (user == null) {
                return ParseResult.failure("user not found");
            }
            return ParseResult.success(new SomeClass(user, Integer.parseInt(argument)));
        }
    }

    static class Command extends LiteCommand<TestSender> {
        public Command() {
            super("test");
            register();
        }

        void register() {
            withoutExecutor().subcommands(sub1(), sub2(), sub3());
        }

        LiteCommand<TestSender> sub1() {
            LiteCommand<TestSender> command = new LiteCommand<>("sub1");
            command.argument("user", User.class);
            command.argument("arg2", SomeClass.class);
            command.executeReturn(liteContext -> {
                User user = liteContext.argument("user", User.class);
                SomeClass someClass = liteContext.argument("arg2", SomeClass.class);
                return "user: " + user.name + " someClass: " + someClass.id;
            });
            return command;
        }

        LiteCommand<TestSender> sub2() {
            LiteCommand<TestSender> command = new LiteCommand<>("sub2");
            command.argument("arg2", SomeClass.class);
            command.executeReturn(liteContext -> {
                SomeClass someClass = liteContext.argument("arg2", SomeClass.class);
                return "someClass: " + someClass.id;
            });
            return command;
        }

        LiteCommand<TestSender> sub3() {
            LiteCommand<TestSender> command = new LiteCommand<>("sub3");
            command.argument("arg2", SomeClass.class);
            command.argument("user", User.class);
            command.executeReturn(liteContext -> {
                SomeClass someClass = liteContext.argument("arg2", SomeClass.class);
                return "user: " + someClass.user.name;
            });
            return command;
        }
    }

    @Test
    void testSub1() {
        platform.execute("test sub1 Dev 1")
            .assertSuccess("user: Dev someClass: 1");
    }

    @Test
    void testSub2() {
        platform.execute("test sub2 1")
            .assertFailedAs(LiteCommandsException.class);
    }

    @Test
    void testSub3() {
        platform.execute("test sub3 1 Dev")
            .assertThrows(NumberFormatException.class); // For input string: "Dev"
    }
}
