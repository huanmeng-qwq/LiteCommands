plugins {
    `litecommands-java`
    `litecommands-java-8`
    `litecommands-unit-test`
    `litecommands-repositories`
    `litecommands-publish`
    `litecommands-compile-variables`
}

dependencies {
    api("org.jetbrains:annotations:${Versions.JETBRAINS_ANNOTATIONS}")
    api("org.ow2.asm:asm-tree:9.7.1")
}

litecommandsPublish {
    artifactId = "litecommands-core"
}