@file:Suppress("unused")

package io.uad.skotsample.screens

import tech.skot.core.test.SKViewModelTester

public class TabScreenTester(
  component: TabScreen,
) : SKViewModelTester<TabScreenViewMock, Unit>(component)
