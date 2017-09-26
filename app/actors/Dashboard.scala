package actors

import actors.Dashboard.Command
import actors.Dashboard.Command._
import actors.Dashboard.Event.DashboardCreated
import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import common._
import org.slf4j.LoggerFactory

import scala.collection.mutable

case class ItemFO(var name: String, var score: Int) {

  def this(name: String ) {
    this(name, 0)
  }

  def increment(): Unit = this.score = this.score + 1

  def decrement(): Unit = if (this.score > 0) this.score = this.score - 1


}

object DashboardFO {
  def empty = DashboardFO("", "", "", "", mutable.Map.empty[String, ItemFO], "", "", "", "")
}

case class DashboardFO(id: String,
                       name: String,
                       description: String,
                       style: String,
                       items: mutable.Map[String, ItemFO] = mutable.Map(),
                       ownerName: String,
                       ownerEmail: String,
                       readonlyHash: String,
                       writeHash: String,
                       deleted: Boolean = false
                    ) extends EntityFieldsObject[String, DashboardFO] {
  /**
    * Assigns an id to the fields object, returning a new instance
    *
    * @param id The id to assign
    */
  override def assignId(id: String) = this.copy(id = id)
  override def markDeleted = this.copy(deleted = false)
}

object Dashboard {

  val EntityType = "dashboard"

  object Command {
    case class CreateDashboard(dashboard: DashboardFO)
    case class Watch()
    case class Unwatch()
    case class Data(items: mutable.Map[String, ItemFO])
    case class IncrementItem(name: String)
    case class DecrementItem(name: String)
    case class AddItem(name: String)
    case class RemoveItem(name: String)
    case class GetWriteHash()
    case class GetReadonlyHash()
    case class GetName()
    case class GetDashboard()
  }

  object Event {
    trait DasboardEvent extends EntityEvent{override def entityType: String = EntityType}
    case class DashboardCreated(dashboard: DashboardFO) extends DasboardEvent
  }



}

class Dashboard(id: String) extends PersistentEntity[DashboardFO](id) {

  def initialState = DashboardFO.empty
  final private val watchers: Set[ActorRef] = Set();


  override def additionalCommandHandling: Receive = {
    case CreateDashboard(dashboard) =>
      // don't allow if not in initial state
      if (state != initialState) {
        sender() ! Failure(FailureType.Validation, DashboardAlreadyCreated)
      } else {
        persist(DashboardCreated(dashboard))(handleEventAndRespond())
      }
  }

  def handleEvent(event:EntityEvent):Unit = event match {
    case DashboardCreated(dashboard) =>
      state = dashboard
  }

//  private def handleIncrementItemCommand(command: IncrementItem): Unit = {
//    val item: ItemFO = dashboard.items(command.name)
//    if (item != null) {
//      item.increment()
//    }
//    notifyWatchers()
//  }
//
//  private def handleDecremenItemCommand (command: DecrementItem): Unit = {
//    val item: ItemFO = dashboard.items(command.name)
//    if (item != null) {
//      item.decrement ()
//    }
//    notifyWatchers ()
//  }
//
//  private def handleWatchCommand (command: Watch): Unit = {
//    val data: DashboardFO.Data = new DashboardFO.Data(dashboard.items)
//    sender.tell (data, self)
//    watchers.+(sender)
//  }
//
//  private def handleDataCommand (command: Data): Unit = {
//    val data: DashboardFO.Data = new DashboardFO.Data (dashboard.items)
//    sender.tell (data, self)
//  }
//
//  private def handleUnwatchCommand (command: Unwatch): Unit = {
//    watchers.-(sender())
//  }
//
//  private def handleGetWriteHashCommand (command: GetWriteHash): Unit = {
//    sender().tell (dashboard.writeHash, self)
//  }
//
//  private def handleGetReadonlyHashCommand (command: GetReadonlyHash): Unit = {
//    sender().tell (dashboard.readonlyHash, self)
//  }
//
//  private def handleGetNameCommand (command: GetName): Unit = {
//    sender().tell (dashboard.name, self)
//  }
//
//  private def handlerGetDashboard(command: GetDashboard): Unit = {
//    sender().tell(dashboard, self)
//  }
//
//  private def handleAddItemCommand (command: AddItem): Unit = {
//    val addItem: DashboardFO.AddItem = command.asInstanceOf[DashboardFO.AddItem]
//    dashboard.items + addItem.name
//    notifyWatchers ()
//  }
//
//  private def handleRemoveItemCommand (command: RemoveItem): Unit = {
//    val removeItem: DashboardFO.RemoveItem = command.asInstanceOf[DashboardFO.RemoveItem]
//    dashboard.items - removeItem.name
//    notifyWatchers ()
//  }
//
//  private def notifyWatchers (): Unit = {
//    for (watcher:ActorRef <- watchers) {
//      watcher.tell(new DashboardFO.Data (dashboard.items), self);
//    }
//  }
//
//  override def receiveRecover: Receive = {
//    case SnapshotOffer(_, snapshot: DashboardFO) =>
//      dashboard = snapshot
//  }
//
//  override def receiveCommand: Receive = {
//    case cmd:DashboardFO.RemoveItem => handleRemoveItemCommand(cmd)
//    case cmd:DashboardFO.AddItem => handleAddItemCommand(cmd)
//    case cmd:DashboardFO.DecrementItem => handleDecremenItemCommand(cmd)
//    case cmd:DashboardFO.IncrementItem => handleIncrementItemCommand(cmd)
//    case cmd:DashboardFO.Data => handleDataCommand(cmd)
//    case cmd:DashboardFO.GetName => handleGetNameCommand(cmd)
//    case cmd:DashboardFO.GetReadonlyHash => handleGetReadonlyHashCommand(cmd)
//    case cmd:DashboardFO.GetWriteHash => handleGetWriteHashCommand(cmd)
//    case cmd:DashboardFO.GetDashboard => handlerGetDashboard(cmd)
//
//    saveSnapshot(dashboard)
//  }
//
//  override def persistenceId: String = dashboard.writeHash

  def props(id: String) = Props(classOf[Dashboard], id)

  val DashboardAlreadyCreated = ErrorMessage("dashboard.alreadyexists", Some("This dashboard has already been created and can not handle another CreateDashboard request"))
}