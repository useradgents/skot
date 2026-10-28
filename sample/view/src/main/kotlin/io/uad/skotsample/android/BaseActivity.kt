package io.uad.skotsample.android

import io.uad.skotsample.SkotsampleInitializer
import tech.skot.core.components.SKActivity
import tech.skot.core.di.`get`

public open class BaseActivity : SKActivity() {
  public override val featureInitializer: SkotsampleInitializer = get()
}
