package controllers

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import org.json4s.DefaultFormats

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Base actor definition for other actors in the scodash app to extend from
  */
trait AbstractBaseActor extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  implicit val timeout: Timeout = 5.seconds
  implicit lazy val formats = DefaultFormats

  //PF to be used with the .recover combinator to convert an exception on a failed Future into a
  //Failure ServiceResult
  private val toFailure:PartialFunction[Throwable, ServiceResult[Nothing]] = {
    case ex => Failure(FailureType.Service, ServiceResult.UnexpectedFailure, Some(ex))
  }

  /**
    * Pipes the response from a request to a service actor back to the sender, first
    * converting to a ServiceResult per the contract of communicating with a bookstore service
    * @param f The Future to map the result from into a ServiceResult
    */
  def pipeResponse[T](f:Future[T]):Unit =
    f.
      map{
        case o:Option[_] => ServiceResult.fromOption(o)
        case f:Failure => f
        case other => FullResult(other)
      }.
      recover(toFailure).
      pipeTo(sender())
}
