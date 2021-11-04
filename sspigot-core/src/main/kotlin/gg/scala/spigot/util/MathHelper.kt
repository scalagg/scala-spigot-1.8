package gg.scala.spigot.util

import net.jafama.FastMath
import java.util.*

/**
 * @author GrowlyX
 * @since 11/4/2021
 */
object MathHelper
{
    @JvmStatic
    fun sin(var0: Float): Double
    {
        return FastMath.sin(var0.toDouble())
    }

    @JvmStatic
    fun cos(var0: Float): Double
    {
        return FastMath.cos(var0.toDouble())
    }

    @JvmStatic
    fun c(var0: Float): Double
    {
        return FastMath.sqrt(var0.toDouble())
    }

    @JvmStatic
    fun sqrt(var0: Double): Double
    {
        return FastMath.sqrt(var0)
    }

    @JvmStatic
    fun d(var0: Float): Int
    {
        val var1 = var0.toInt()
        return if (var0 < var1.toFloat()) var1 - 1 else var1
    }

    @JvmStatic
    fun floor(var0: Double): Double
    {
        return FastMath.floor(var0)
    }

    @JvmStatic
    fun d(var0: Double): Long
    {
        val var2 = var0.toLong()
        return if (var0 < var2.toDouble()) var2 - 1L else var2
    }

    @JvmStatic
    fun abs(var0: Float): Float
    {
        return if (var0 >= 0.0f) var0 else -var0
    }

    @JvmStatic
    fun a(var0: Int): Int
    {
        return if (var0 >= 0) var0 else -var0
    }

    @JvmStatic
    fun f(var0: Float): Int
    {
        val var1 = var0.toInt()
        return if (var0 > var1.toFloat()) var1 + 1 else var1
    }

    @JvmStatic
    fun f(var0: Double): Int
    {
        val var2 = var0.toInt()
        return if (var0 > var2.toDouble()) var2 + 1 else var2
    }

    @JvmStatic
    fun a(var0: Int, var1: Int, var2: Int): Int
    {
        return if (var0 < var1) var1 else FastMath.min(var0, var2)
    }

    @JvmStatic
    fun a(var0: Float, var1: Float, var2: Float): Float
    {
        return if (var0 < var1) var1 else FastMath.min(var0, var2)
    }

    @JvmStatic
    fun a(var0: Double, var2: Double, var4: Double): Double
    {
        return if (var0 < var2) var2 else FastMath.min(var0, var4)
    }

    @JvmStatic
    fun b(var0: Double, var2: Double, var4: Double): Double
    {
        return if (var4 < 0.0)
        {
            var0
        } else
        {
            if (var4 > 1.0) var2 else var0 + (var2 - var0) * var4
        }
    }

    @JvmStatic
    fun a(var0: Double, var2: Double): Double
    {
        var var1 = var0
        var var3 = var2

        if (var0 < 0.0)
        {
            var1 = -var0
        }

        if (var2 < 0.0)
        {
            var3 = -var2
        }

        return FastMath.max(var1, var3)
    }

    fun nextInt(var0: Random, var1: Int, var2: Int): Int
    {
        return if (var1 >= var2) var1 else var0.nextInt(var2 - var1 + 1) + var1
    }

    fun a(var0: Random, var1: Float, var2: Float): Float
    {
        return if (var1 >= var2) var1 else var0.nextFloat() * (var2 - var1) + var1
    }

    fun a(var0: Random, var1: Double, var3: Double): Double
    {
        return if (var1 >= var3) var1 else var0.nextDouble() * (var3 - var1) + var1
    }

    fun a(var0: LongArray): Double
    {
        var var1 = 0L
        for (var6 in var0)
        {
            var1 += var6
        }
        return var1.toDouble() / var0.size.toDouble()
    }

    fun g(var0: Float): Float
    {
        var var0 = var0
        var0 %= 360.0f
        if (var0 >= 180.0f)
        {
            var0 -= 360.0f
        }
        if (var0 < -180.0f)
        {
            var0 += 360.0f
        }
        return var0
    }

    fun g(var0: Double): Double
    {
        var var0 = var0
        var0 %= 360.0
        if (var0 >= 180.0)
        {
            var0 -= 360.0
        }
        if (var0 < -180.0)
        {
            var0 += 360.0
        }
        return var0
    }

    fun a(var0: String, var1: Int): Int
    {
        var var2 = var1
        try
        {
            var2 = var0.toInt()
        } catch (ignored: Throwable)
        {
        }
        return var2
    }

    fun a(var0: String, var1: Int, var2: Int): Int
    {
        var var3 = var1
        try
        {
            var3 = var0.toInt()
        } catch (ignored: Throwable)
        {
        }
        if (var3 < var2)
        {
            var3 = var2
        }
        return var3
    }

    fun a(var0: String, var1: Double): Double
    {
        var var3 = var1
        try
        {
            var3 = var0.toDouble()
        } catch (ignored: Throwable)
        {
        }
        return var3
    }

    fun a(var0: String, var1: Double, var3: Double): Double
    {
        var var5 = var1
        try
        {
            var5 = var0.toDouble()
        } catch (ignored: Throwable)
        {
        }
        if (var5 < var3)
        {
            var5 = var3
        }
        return var5
    }
}
