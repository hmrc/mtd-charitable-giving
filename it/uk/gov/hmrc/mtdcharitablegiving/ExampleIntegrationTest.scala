package uk.gov.hmrc.mtdcharitablegiving

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.integration.ServiceSpec

class ExampleIntegrationTest extends WordSpec with Matchers with ServiceSpec  {

  def externalServices: Seq[String] = Seq("datastream", "auth")

  override def additionalConfig: Map[String, _] = Map("auditing.consumer.baseUri.port" -> externalServicePorts("datastream"))


  "This integration test" should {
    "return true" in {

      !false shouldBe true

    }
  }
}
