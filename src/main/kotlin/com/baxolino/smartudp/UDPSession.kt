package com.baxolino.smartudp

import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.util.Collections
import java.util.WeakHashMap

class UDPSession(
  address: InetSocketAddress,
) : DatagramSocket(address) {

  companion object {
    private const val SO_RCVBUF = 4096
  }

  private val buffer = ByteArray(SO_RCVBUF)
  val packetCallback = Collections.synchronizedMap(WeakHashMap<String, (InetAddress, ByteArray) -> ByteArray?>())

  init {
    trafficClass = 0x04 or 0x08 // reliability + speed
    receiveBufferSize = SO_RCVBUF
    soTimeout = 2048 //2sec
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun beginReceiving() {
    while (!Thread.currentThread().isInterrupted) {
      val packet = DatagramPacket(buffer, buffer.size)
      try {
        receive(packet)
      } catch (_: SocketTimeoutException) {
        continue
      }
      val uidOffset = buffer[0] + 1
      val uid = String(buffer.copyOfRange(1, uidOffset))
      val reply = try {
        packetCallback[uid]?.invoke(
          packet.address,
          buffer.copyOfRange(uidOffset, packet.length)
        )
      } catch (t: Throwable) {
        t.printStackTrace()
        null
      }
      try {
        reply?.let { sendWithUid(packet.address, packet.port, uid, reply) }
      } catch (t: Throwable) {
        println("Error while replying to uid $uid")
        t.printStackTrace()
      }
    }
  }

  fun sendWithUid(address: InetAddress, port: Int, uid: String, message: ByteArray) {
    val combined = ByteArrayOutputStream().apply {
      val uidBytes = uid.toByteArray()
      write(uidBytes.size)
      write(uidBytes)
      write(message)
    }.toByteArray()
    send(DatagramPacket(combined, 0, combined.size, address, port))
  }
}