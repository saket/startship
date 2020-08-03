package nevam.network

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.XML

object XmlToJson {
  fun convert(xml: String): String {
    val jsonObject = XML.toJSONObject(xml)
    return jsonObject.toString()
  }
}

object XmlToJsonBodyInterceptor : Interceptor {
  override fun intercept(chain: Chain): Response {
    val xmlHeader = chain.request().headers.firstOrNull { it == "Death-To-Xml" to "true" }
    val response = chain.proceed(chain.request())

    return if (xmlHeader != null && response.isSuccessful) {
      // Loading the entire response in memory is a terrible
      // idea, but should be okay for this specific endpoint.
      val xml = response.body!!.string()
      val json = XmlToJson.convert(xml)

      response.newBuilder()
          .body(json.toResponseBody("application/json".toMediaType()))
          .build()

    } else {
      response
    }
  }
}
