package models

import scalikejdbc._

/** Created by a.kiselev on 30/10/2016. */
case class Flat(flatId: Int,
                area: Double,
                flatNumber: Int,
                balance: Double,
                cladrId: Int,
                buildingId: Int) {

  def listMeters = Meter.listByFlatId(flatId)

}


object Flat extends SQLSyntaxSupport[Flat] {

  def apply(f: ResultName[Flat])(rs: WrappedResultSet): Flat =
    new Flat(
      flatId = rs.get(f.flatId),
      area = rs.get(f.area),
      flatNumber = rs.get(f.flatNumber),
      balance = rs.get(f.balance),
      cladrId = rs.get(f.cladrId),
      buildingId = rs.get(f.buildingId)
    )


  def apply(f: SyntaxProvider[Flat])(rs: WrappedResultSet): Flat =
    apply(f.resultName)(rs)

  val f = Flat.syntax("f")

  def all()
         (implicit session: DBSession = autoSession): List[Flat] = {
    sql"""
         SELECT ${f.result.*}
         FROM ${Flat as f}
       """
        .map(Flat(f)).list().apply()
  }

  def findById(flatId: Int)(implicit session: DBSession = autoSession): Option[Flat] =
    sql"""select ${f.result.*} from ${Flat as f}
          where ${f.flatId} = ${flatId}
      """.map(Flat(f)).single().apply()

  def listByPersonId(personId: Int)(implicit session: DBSession = autoSession): Seq[Flat] = {
    import DwellerLivesInFlat.df
    sql"""SELECT ${f.result.*}, ${df.result.*}
          FROM ${Flat as f} NATURAL JOIN ${DwellerLivesInFlat as DwellerLivesInFlat.df}
          WHERE ${df.personId} = ${personId}
       """.map(Flat(f)).list().apply()
  }


}
