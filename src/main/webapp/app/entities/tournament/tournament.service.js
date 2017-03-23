(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Tournament', Tournament);

    Tournament.$inject = ['$resource'];

    function Tournament ($resource) {
        var resourceUrl =  'api/tournaments/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
