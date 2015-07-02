package pk.auth

import com.typesafe.config.Config
import play.api.inject.Module
import play.api.{Environment, Configuration}

class CBModule extends Module {
  def bindings(env: Environment, cfg: Configuration) = Seq(
    bind[Config].toInstance(cfg.underlying)
  )
}
