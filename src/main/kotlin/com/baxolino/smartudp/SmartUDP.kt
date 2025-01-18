package com.baxolino.smartudp

import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

object SmartUDP {

  private val executor = Executors.newSingleThreadExecutor()

  private val _session = AtomicReference<UDPSession>()
  private val session get() = _session.get()

  fun create(port: Int): SmartUDP {
    executor.submit { _session.set(UDPSession(InetSocketAddress(port))) }.get()
    executor.submit { session.beginReceiving() }
    return this
  }

  fun message(address: InetAddress, port: Int, message: ByteArray, uid: String): SmartUDP {
    session.sendWithUid(address, port, uid, message)
    return this
  }

  fun expectResponse(uid: String, timeout: Long): ByteArray? {
    val result = AtomicReference<ByteArray?>(null)
    handleResponse(uid) {
      result.set(it)
      null
    }
    Thread.sleep(timeout)
    removeResponseHandler(uid)
    return result.get()
  }

  fun handleResponse(uid: String, callback: (ByteArray) -> ByteArray?) {
    session.packetCallback[uid] = callback
  }

  fun removeResponseHandler(uid: String) {
    session.packetCallback.remove(uid)
  }
}