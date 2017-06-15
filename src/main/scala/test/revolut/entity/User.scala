package test.revolut.entity

case class User(uuid: String, fullName: String, var accounts: Set[Account]) extends Entity {
  override def hashCode: Int = {
    uuid.hashCode()
  }
  def equals(that: User): Boolean = this.uuid.equals(that.uuid)
  def ==(that: User) = this equals that
}