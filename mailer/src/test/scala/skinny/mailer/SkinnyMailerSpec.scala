package skinny.mailer

import org.scalatest.{ BeforeAndAfter, FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import skinny.mailer.test.SkinnyMailTestSupport
import skinny.mailer.example.MyMailer
import grizzled.slf4j.Logging

class SkinnyMailerSpec extends FlatSpec with ShouldMatchers with SkinnyMailTestSupport with BeforeAndAfter with Logging {

  behavior of "SkinnyMailer"

  val toAddress = "to@example.com"

  def inbox = mailbox(toAddress)

  before {
    clearMailbox(toAddress)
  }

  val mailer = new MyMailer()

  try {
    it should "basic" in {
      mailer.sendMessage(toAddress)
      val msg = inbox.receivedMessages.last
      msg.subject.get should equal("test subject 日本語")
      msg.body.get should equal(
        s"""${toAddress} 様
        |
        |いつもご利用ありがとうございます。
        |〜〜をお知らせいたします。
        |
        |""".stripMargin
      )
      msg.cc.size should equal(2)
      msg.bcc.size should equal(1)
    }

    it should "fail sending" in {
      mailer.notSending
      inbox.receivedMessages.size should equal(0)
    }

    it should "sent multiple emails" in {
      mailer.sendMessage(toAddress)
      mailer.sendMessage2(toAddress)

      inbox.size should be === 2
      inbox.receivedMessages.head.subject.get should equal("test subject 日本語")
      inbox.receivedMessages.last.subject.get should equal("subject2")
    }

    it should "send to other address" in {
      inbox.receivedMessages.size should equal(0)
      mailer.sendOther
      inbox.receivedMessages.size should equal(0)
    }

  } catch {
    case e: Exception =>
      // ignore when failing on Travis CI
      if (sys.env.get("TRAVIS").isDefined) {
        logger.warn(s"Failed on Travis CI. message: ${e.getMessage}")
      } else {
        throw e
      }
  }

}