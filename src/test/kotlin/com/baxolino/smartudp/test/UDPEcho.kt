import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun main() {
  val port = 1234 // The port to listen on
  val bufferSize = 1024 // 1 KB buffer

  DatagramSocket(port).use { socket ->
    println("Listening for UDP packets on port $port...")

    val buffer = ByteArray(bufferSize)
    val packet = DatagramPacket(buffer, buffer.size)

    while (true) {
      // Receive the incoming packet
      socket.receive(packet)

      // Extract the received message as a string
      val receivedData = String(packet.data, 0, packet.length)
      println("Received: $receivedData")

      // Echo the received packet back to the sender
      val senderAddress: InetAddress = packet.address
      val senderPort = packet.port
      val echoPacket = DatagramPacket(packet.data, packet.length, senderAddress, senderPort)
      socket.send(echoPacket)

      println("Echoed packet back to ${senderAddress.hostAddress}:${senderPort}")
    }
  }
}
