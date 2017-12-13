(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Team', Team);

    Team.$inject = ['$resource'];

    function Team ($resource) {
        var resourceUrl =  'api/teams/:id';

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
            'update': { method:'PUT' },
            'getAllTournaments': {
                method: 'GET',
                url: 'api/teams/tournaments/:id',
                isArray: true
            },
            'getAllCombinedTournaments': {
                method: 'GET',
                url: 'api/teams/combined/:id',
                isArray: true
            }
        });
    }
})();
