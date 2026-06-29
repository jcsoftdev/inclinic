package com.inclinic.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform