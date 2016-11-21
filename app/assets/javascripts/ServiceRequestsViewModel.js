/**
 * Created by VladVin on 17.11.2016.
 */

var ServiceRequestsViewModel = function(requestsInfo) {
    var self = this;

    self.flatNumber = requestsInfo.flatNumber >=0 ?
            requestsInfo.flatNumber :
            "unknown";

    var sreqs = requestsInfo.sreqs;
    self.serviceRequests = ko.observableArray(
        _.map(sreqs,
            function(sreq) {
                return {
                    id: sreq.id,
                    description: ko.observable(sreq.description),
                    status: ko.observable(sreq.status),
                    nextVisitDate: ko.observable(moment(sreq.nextVisitDate).format('MMM Do YYYY')),
                    totalCost: ko.observable(sreq.totalCost),
                    creationDate: ko.observable(moment(sreq.creationDate).format('MMM Do YYYY')),
                    rating: ko.observable(sreq.rating)
                }
            }
        )
    );
};
