(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Elimination', Elimination);

    Elimination.$inject = ['$resource'];

    function Elimination ($resource) {
        var resourceUrl =  'api/eliminations/:id';

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
