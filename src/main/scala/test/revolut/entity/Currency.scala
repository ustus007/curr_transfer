package test.revolut.entity

case class Currency(uuid: String, shortName: String, fullName: String) extends Entity {
  override def hashCode: Int = {
    uuid.hashCode()
  }
  def equals(that: Account): Boolean = this.uuid.equals(that.uuid)
  def ==(that: Account) = this equals that
}