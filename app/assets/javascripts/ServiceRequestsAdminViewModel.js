/**
 * Created by VladVin on 22.11.2016.
 */

var ServiceRequestsAdminViewModel = function (requestsInfoList) {
    var self = this;

    self.requestsInfoList = ko.observable(requestsInfoList
        .map(function (ri) {
            return {
                flatNumber: ri.flatNumber,
                serviceRequests: ri.sreqs
                    .map(function (sr) {
                        return {
                            description: sr.description,
                            nextVisitDate: sr.nextVisitDate,
                            totalCost: sr.totalCost,
                            creationDate: sr.creationDate,
                            rating: sr.rating
                        }
                    })
            }
        })
    )
};
