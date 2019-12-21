package utils.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class ConfigUserServiceImpl @Inject() (conf: Configuration)(implicit ex:ExecutionContext) extends UserService {

  override def retrieve(id: UUID): Future[Option[User]] = Future(Some(User(id.toString)))

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = Future(Some(User(loginInfo.providerKey)))
}
