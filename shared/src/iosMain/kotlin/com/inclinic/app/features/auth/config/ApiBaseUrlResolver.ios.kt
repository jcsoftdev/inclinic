package com.inclinic.app.features.auth.config

/** iOS simulator shares the host network, so `localhost` reaches the dev machine. */
actual val platformLoopbackHost: String = "localhost"
