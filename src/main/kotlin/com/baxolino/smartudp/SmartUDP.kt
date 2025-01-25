package com.baxolino.smartudp

import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class SmartUDP: AutoCloseable {

  private val executor = Executors.newSingleThreadExecutor()

  private val _session = AtomicReference<UDPSession>()
  private val session get() = _session.get()

  fun create(port: Int): SmartUDP {
    executor.submit { _session.set(UDPSession(InetSocketAddress(InetAddress.getByName("0.0.0.0"), port))) }.get()
    executor.submit { session.beginReceiving() }
    return this
  }

  fun message(address: InetAddress, port: Int, message: ByteArray, uid: String? = null): SmartUDP {
    if (uid != null) {
      session.sendWithUid(address, port, uid, message)
    } else {
      session.send(DatagramPacket(message, 0, message.size, address, port))
    }
    return this
  }

  fun expectResponse(uid: String, timeout: Long): ByteArray? {
    val result = AtomicReference<ByteArray?>(null)
    route(uid) { address, message ->
      result.set(message)
      null
    }
    Thread.sleep(timeout)
    removeRoute(uid)
    return result.get()
  }

  fun route(uid: String, callback: (InetAddress, ByteArray) -> ByteArray?) {
    session.packetCallback[uid] = callback
  }

  fun removeRoute(uid: String) {
    session.packetCallback.remove(uid)
  }

  override fun close() {
    executor.shutdownNow()
    session.close()
  }

}