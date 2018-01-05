package com.mason.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class TimeCountProxyHandle(private val proxied: Any) : InvocationHandler {

  @Throws(Throwable::class)
  override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any {
    val begin = System.currentTimeMillis()
    val result: Any?
    if (args != null) result = method.invoke(proxied, *args)
    else result = method.invoke(proxied)
    val end = System.currentTimeMillis()
    println(method.name + "耗时:" + (end - begin) + "ms")
    return result
  }
}
