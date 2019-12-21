package utils.auth

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

/**
  * This class retrieves credentials from play's config (application.conf)
  *
  * Since this is simple authentication, the only method we need is find, the other
  * ones don't even need to be implemented.
  */
class ConfigAuthInfoRepository @Inject()(conf: Configuration) extends AuthInfoRepository {
  val hasher = BCryptPasswordHasher.ID
  val username = conf.get[String]("security.simple.username")
  val password = conf.get[String]("security.simple.password")

  override def add[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = ???

  override def update[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = ???

  override def save[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = ???

  override def remove[T <: AuthInfo](loginInfo: LoginInfo)(implicit tag: ClassTag[T]): Future[Unit] = Future {}

  override def find[T <: AuthInfo](loginInfo: LoginInfo)(implicit tag: ClassTag[T]): Future[Option[T]] = {
    Future {
      // only one user for now.
      if (loginInfo.providerKey == username)
        Some(PasswordInfo(hasher, password).asInstanceOf[T])
      else None
    }
  }}
