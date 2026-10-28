package io.uad.skotsample.screens

import tech.skot.di.modelFrameworkModule
import tech.skot.model.test.SKTestModel
import tech.skot.model.test.network.mockHttp

public abstract class TestModel : SKTestModel(modelFrameworkModule/*add here model's modules*/)
