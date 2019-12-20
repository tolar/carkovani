package controllers

import com.google.inject.Inject
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import scalaoauth2.provider.{AuthorizationCode, AuthorizationRequest, ClientCredential, ClientCredentials, DataHandler, OAuth2Provider, OAuthGrantType, Password, RefreshToken, TokenEndpoint}

import scala.concurrent.Future

class OAuthController @Inject()
  (cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) with OAuth2Provider {

  override val tokenEndpoint = new TokenEndpoint {
    override val handlers = Map(
      OAuthGrantType.AUTHORIZATION_CODE -> new AuthorizationCode(),
      OAuthGrantType.REFRESH_TOKEN -> new RefreshToken(),
      OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials(),
      OAuthGrantType.PASSWORD -> new Password()
    )
  }

  def accessToken = Action.async { implicit request =>
    issueAccessToken(new MyDataHandler())
  }

  case class MyDataHandler() extends DataHandler[] {

    // common

    override def validateClient(maybeCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = DB.readOnly { implicit session =>
      Future.successful((for {
        clientCredential <- maybeCredential
      } yield OauthClient.validate(clientCredential.clientId, clientCredential.clientSecret.getOrElse(""), request.grantType)).contains(true))
    }

    override def getStoredAccessToken(authInfo: AuthInfo[Account]): Future[Option[AccessToken]] = DB.readOnly { implicit session =>
      Future.successful(OauthAccessToken.findByAuthorized(authInfo.user, authInfo.clientId.getOrElse("")).map(toAccessToken))
    }

    override def createAccessToken(authInfo: AuthInfo[Account]): Future[AccessToken] = DB.localTx { implicit session =>
      val clientId = authInfo.clientId.getOrElse(throw new InvalidClient())
      val oauthClient = OauthClient.findByClientId(clientId).getOrElse(throw new InvalidClient())
      val accessToken = OauthAccessToken.create(authInfo.user, oauthClient)
      Future.successful(toAccessToken(accessToken))
    }

    private val accessTokenExpireSeconds = 3600
    private def toAccessToken(accessToken: OauthAccessToken) = {
      AccessToken(
        accessToken.accessToken,
        Some(accessToken.refreshToken),
        None,
        Some(accessTokenExpireSeconds),
        accessToken.createdAt.toDate
      )
    }

    override def findUser(maybeCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[Account]] = DB.readOnly { implicit session =>
      request match {
        case request: PasswordRequest =>
          Future.successful(Account.authenticate(request.username, request.password))
        case request: ClientCredentialsRequest =>
          Future.successful {
            for {
              clientCredential <- maybeCredential
              account <- OauthClient.findClientCredentials(
                clientCredential.clientId,
                clientCredential.clientSecret.getOrElse("")
              )
            } yield account
          }
        case _ =>
          Future.successful(None)
      }
    }

  }

}
