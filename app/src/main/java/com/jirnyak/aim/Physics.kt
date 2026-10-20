package com.jirnyak.aim

import kotlin.math.*

data class Vec(val x: Float, val y: Float) {
    operator fun plus(o: Vec) = Vec(x + o.x, y + o.y)
    operator fun minus(o: Vec) = Vec(x - o.x, y - o.y)
    operator fun times(k: Float) = Vec(x * k, y * k)
    fun len() = hypot(x, y)
    fun norm() = this * (1f / len().coerceAtLeast(1e-6f))
}

class Physics(val tableW: Float, val tableH: Float, val ballR: Float, val maxBounces: Int) {

    fun contactPoint(c0: Vec, dir: Vec, target: Vec): Vec? {
        val d = dir.norm()
        val toT = target - c0
        val proj = toT.x * d.x + toT.y * d.y
        if (proj <= 0) return null
        val perp2 = toT.len().pow(2) - proj.pow(2)
        val sumR = 2 * ballR
        if (perp2 > sumR * sumR) return null
        val back = sqrt((sumR * sumR - perp2).coerceAtLeast(0f))
        return c0 + d * (proj - back)
    }

    fun trace(start: Vec, dir: Vec, bounces: Int): List<Vec> {
        val pts = mutableListOf(start)
        var p = start; var d = dir.norm()
        var left = bounces
        while (left-- > 0) {
            val tx = when {
                d.x > 0 -> (tableW - ballR - p.x) / d.x
                d.x < 0 -> (ballR - p.x) / d.x
                else -> Float.MAX_VALUE
            }
            val ty = when {
                d.y > 0 -> (tableH - ballR - p.y) / d.y
                d.y < 0 -> (ballR - p.y) / d.y
                else -> Float.MAX_VALUE
            }
            val t = min(tx, ty)
            if (t <= 0 || t == Float.MAX_VALUE) break
            p = p + d * t
            pts.add(p)
            if (tx < ty) d = Vec(-d.x, d.y) else d = Vec(d.x, -d.y)
        }
        return pts
    }

    data class Result(val aim: List<Vec>, val cue: List<Vec>, val target: List<Vec>?)

    fun predict(cue: Vec, aimDir: Vec, target: Vec?): Result {
        val aimRay = trace(cue, aimDir, maxBounces)
        if (target == null) return Result(aimRay, emptyList(), null)
        val cp = contactPoint(cue, aimDir, target) ?: return Result(aimRay, emptyList(), null)
        val cDir = (target - cp).norm()
        val cueDir = Vec(-cDir.x, -cDir.y)
        return Result(aimRay, trace(cp, cueDir, maxBounces), trace(target, cDir, maxBounces))
    }
}
