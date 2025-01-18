package com.baxolino.smartudp.test

import com.baxolino.smartudp.SmartUDP
import java.net.InetAddress

object Main {
  @JvmStatic
  fun main(args: Array<String>) {
    SmartUDP
      .create(2244)
      .message(InetAddress.getByName("localhost"), 1234, "hello world".toByteArray(), "uwu")
      .handleResponse("uwu") {
        println("Received reply: ${String(it)}")
        null
      }
  }
}