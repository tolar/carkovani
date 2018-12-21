package controllers

trait DashboardHashes {
  def writeHash: String
  def readonlyHash: String
}
