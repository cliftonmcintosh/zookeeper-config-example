package com.sysgears.example.config

import com.typesafe.config.ConfigFactory

/**
 * Base Configuration.
 */
trait BaseConfiguration {

  /**
   * Local configuration (application.conf, reference.conf, JVM settings).
   */
  val config = ConfigFactory.load()

  /**
   * Environment to use configuration settings for.
   */
  val Environment = config.getString("environment")
}
