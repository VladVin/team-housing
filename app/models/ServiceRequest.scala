package models

import org.joda.time.DateTime
import scalikejdbc._

case class ServiceRequest (
    id: Long,
    description: String,
    rating: Option[Int],
    creationDate: DateTime,
    status: Option[Int]
)

object ServiceRequest extends SQLSyntaxSupport[ServiceRequest] {
    def apply(srName: ResultName[ServiceRequest])(rs: WrappedResultSet): ServiceRequest = new ServiceRequest(
        id = rs.get(srName.id),
        description = rs.get(srName.description),
        rating = rs.get(srName.rating),
        creationDate = rs.get(srName.creationDate),
        status = rs.get(srName.status)
    )

    def apply(sr: SyntaxProvider[ServiceRequest])(rs: WrappedResultSet): ServiceRequest = {
        apply(sr.resultName)(rs)
    }

    val sr = ServiceRequest.syntax("sr")

    def create(flatId: Int,
               description: String)
              (implicit session: DBSession = autoSession): ServiceRequest = {
        val id =
            sql"""INSERT INTO ${table}
                  (${column.description}, ${column.rating}, ${column.creationDate}, ${column.status})
                 VALUES (${description}, ${0}, ${DateTime.now}, ${0})
               """
            .updateAndReturnGeneratedKey.apply()

        RequestToFlat.create(id, flatId)

        sql"""SELECT ${sr.result.*} FROM ${ServiceRequest as sr}
             WHERE ${sr.id} = ${id}
           """
            .map(ServiceRequest(sr)).single().apply().get
    }

    def update(requestId: Long, description: String, rating: Int, status: Int)(implicit session: DBSession = autoSession): Unit = {
        sql"""
              UPDATE ${ServiceRequest as sr}
              SET description=${description}, rating=${rating}, status=${status}
              WHERE ${sr.id} = ${requestId}
              """.update().apply()
    }

    def get(requestId: Long)(implicit session: DBSession = autoSession) : ServiceRequest = {
      sql"""select ${sr.result.*} from ${ServiceRequest as sr} where ${sr.id} = ${requestId}"""
        .map(ServiceRequest(sr)).single().apply().get
    }

    def findAllForFlat(flatId: Long)
                      (implicit session: DBSession = autoSession): List[ServiceRequest] = {
        import RequestToFlat.rtf
        sql"""
           SELECT ${sr.result.*}
           FROM ${ServiceRequest as sr}
           INNER JOIN ${RequestToFlat as rtf}
           ON ${sr.id} = ${rtf.requestId}
           WHERE ${rtf.flatId} = ${flatId}
           """
            .map(ServiceRequest(sr)).list().apply()
    }

    def findAllActiveForFlat(flatId: Long)
                     (implicit session: DBSession = autoSession): List[ServiceRequest] = {
        import RequestToFlat.rtf
        sql"""
             SELECT ${sr.result.*}
             FROM ${ServiceRequest as sr}
             INNER JOIN ${RequestToFlat as rtf}
             ON ${sr.id} = ${rtf.requestId}
             WHERE ${rtf.flatId} = ${flatId}
             AND ${sr.status} <= 2
           """
            .map(ServiceRequest(sr)).list().apply()
    }


    def findNextVisitDate(requestId: Long)
                         (implicit session: DBSession = autoSession): Option[DateTime] = {
        import Visited.v
        // Walk through only by IN_PROGRESS service requests
        sql"""
           SELECT ${v.result.scheduleTime}
           FROM ${Visited as v}
           INNER JOIN ${ServiceRequest as sr}
           ON ${v.serviceRequsetId} = ${sr.id}
           WHERE ${sr.id} = ${requestId} AND ${sr.status} = 1
           ORDER BY ${v.scheduleTime} DESC
           LIMIT 1
           """
              .map(rs => rs.jodaDateTime(v.resultName.scheduleTime)).single().apply()
    }
}
