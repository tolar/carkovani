package utils.auth


import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  def retrieve(id: UUID): Future[Option[User]]
}
