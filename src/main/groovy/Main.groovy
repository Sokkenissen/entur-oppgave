static void main(String[] args) {

  def R10 = new Rute("R10")

  def lillehammer = new Stoppested("Lillehammer", Kommune.LILLEHAMMER)
  def moelv = new Stoppested("Moelv", Kommune.RINGSAKER)
  def brumunddal = new Stoppested("Brumunddal", Kommune.RINGSAKER)
  def stange = new Stoppested("Stange", Kommune.STANGE)
  def tangen = new Stoppested("Tangen", Kommune.STANGE)
  def OSL = new Stoppested("Oslo Lufthavn", Kommune.ULLENSAKER)
  def oslos = new Stoppested("Oslo S", Kommune.OSLO)

  R10.leggTilStoppested(lillehammer, moelv, 30)
  R10.leggTilStoppested(moelv, brumunddal, 30)
  R10.leggTilStoppested(brumunddal, stange, 20)
  R10.leggTilStoppested(stange, tangen, 20)
  R10.leggTilStoppested(tangen, OSL, 20)
  R10.leggTilStoppested(OSL, oslos, 30)

  println R10

  println R10.finnReisetid(lillehammer, brumunddal)

}