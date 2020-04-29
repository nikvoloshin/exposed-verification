package com.github.nikvoloshin.exposed.verification.extensions

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Schema

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MultiDatabaseTestExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
annotation class MultiDatabaseTest

