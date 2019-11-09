package nevam.network

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi.Builder
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nevam.nexus.network.MavenMetadata
import org.json.XML
import org.junit.Test

class XmlToJsonTest {

  @Test fun foo() {
    val xml = """
      <metadata>
        <groupId>me.saket</groupId>
        <artifactId>flick</artifactId>
        <versioning>
          <latest>1.8.0-SNAPSHOT</latest>
          <release>1.7.0</release>
          <versions>
            <version>1.6.0</version>
            <version>1.7.0</version>
          </versions>
          <lastUpdated>20191108065802</lastUpdated>
        </versioning>
      </metadata>
    """.trimIndent()

    val jsonObject = XML.toJSONObject(xml)
    val json: String = jsonObject.toString()

    val moshiAdapter = Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(MavenMetadata::class.java)
    val parsed = moshiAdapter.fromJson(json)

    assertThat(parsed).isEqualTo(
        MavenMetadata(
            MavenMetadata.Data(
                groupId = "me.saket",
                artifactId = "flick",
                versions = MavenMetadata.Versions(
                    release = "1.7.0",
                    lastUpdatedDate = "20191108065802"
                )
            )
        )
    )
  }
}
