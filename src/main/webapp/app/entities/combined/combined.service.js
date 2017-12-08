(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Combined', Combined);

    Combined.$inject = ['$resource', 'DateUtils'];

    function Combined ($resource, DateUtils) {
        var resourceUrl =  'api/combined/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.created = DateUtils.convertDateTimeFromServer(data.created);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'generatePlayoff': {method: 'PUT'}
        });
    }
})();
