package com.jirnyak.aim

object Config {
    @Volatile var aimOn = true
    @Volatile var cueOn = true
    @Volatile var targetOn = true
    @Volatile var bounces = 3
    @Volatile var tableW = 1920f
    @Volatile var tableH = 1080f
    @Volatile var ballR = 18f
}
