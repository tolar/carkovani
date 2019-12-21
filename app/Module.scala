
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.{DummyAuthenticator, DummyAuthenticatorService}
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import controllers.actors.Scodash
import controllers.{DashboardView, DashboardViewBuilder}
import play.api.libs.concurrent.AkkaGuiceSupport
import utils.auth.{ConfigAuthInfoRepository, ConfigUserServiceImpl, DefaultEnv, UserService}

// use scala guice binding
import net.codingwell.scalaguice.{ ScalaModule, ScalaPrivateModule }

import scala.concurrent.ExecutionContext.Implicits.global


class Module extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[DashboardViewBuilder](DashboardViewBuilder.Name, _ => DashboardViewBuilder.props)

    // authentication - silhouette bindings
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[RequestProvider].to[BasicAuthProvider].asEagerSingleton()

    bind[UserService].to[ConfigUserServiceImpl]
    bind[PasswordHasherRegistry].toInstance(PasswordHasherRegistry(
      current = new BCryptPasswordHasher(),
      // if you want
      deprecated = Seq()
    ))
    bind[AuthenticatorService[DummyAuthenticator]].toInstance(new DummyAuthenticatorService)

    // configure a single username/password in play config
    bind[AuthInfoRepository].to[ConfigAuthInfoRepository].asEagerSingleton()
  }

  @Provides
  def provideEnvironment(
                          userService:          UserService,
                          authenticatorService: AuthenticatorService[DummyAuthenticator],
                          eventBus:             EventBus,
                          requestProvider:      RequestProvider
                        ): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(requestProvider),
      eventBus
    )
  }

  import akka.actor.{ActorRef, ActorSystem}
  import com.google.inject.Provides

  @Provides
  @Named(DashboardView.Name) def dashboardViewActorRef(system: ActorSystem): ActorRef = system.actorOf(DashboardView.props)

  @Provides
  @Named(Scodash.Name) def scodashActorRef(system: ActorSystem): ActorRef = system.actorOf(Scodash.props)




}

