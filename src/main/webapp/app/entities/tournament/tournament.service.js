(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Tournament', Tournament);

    Tournament.$inject = ['$resource', 'DateUtils'];

    function Tournament ($resource, DateUtils) {
        var resourceUrl =  'api/tournaments/:id';

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
            'getSeeding':{
                method: 'GET',
                url: 'api/tournaments/seeding/:id',
                isArray: true
                
            }
        });
    }
})();
