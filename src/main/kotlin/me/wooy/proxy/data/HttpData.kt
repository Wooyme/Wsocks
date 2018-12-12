package me.wooy.proxy.data

import io.vertx.core.buffer.Buffer

inline class HttpData(private val buffer: Buffer) {
  private val uuidLen get() = buffer.getIntLE(Int.SIZE_BYTES)
  val uuid get() = buffer.getString(Int.SIZE_BYTES*2,Int.SIZE_BYTES*2+uuidLen)
  val port get() = buffer.getIntLE(Int.SIZE_BYTES*2+uuidLen)
  private val hostLen get() = buffer.getIntLE(Int.SIZE_BYTES*3+uuidLen)
  val host get() = buffer.getString(Int.SIZE_BYTES*4+uuidLen, Int.SIZE_BYTES*4+hostLen+uuidLen)
  val data get() = buffer.getBuffer(Int.SIZE_BYTES*4+hostLen+uuidLen,buffer.length())
  fun toBuffer() = buffer
  companion object {
    fun create(uuid:String,port:Int,host:String,data:Buffer) = HttpData(Buffer.buffer()
      .appendIntLE(Flag.HTTP.ordinal)
      .appendIntLE(uuid.length)
      .appendString(uuid)
      .appendIntLE(port)
      .appendIntLE(host.length)
      .appendString(host).appendBuffer(data))
  }
}