package test.revolut.DAO

trait SynchronizationDAO  extends AbstractDAO {
   
    def getSyncObjectByUuids (uuids: List[String]): List[Object]
    def syncOn(on:List[Object], action: ()=>Unit)
    
}