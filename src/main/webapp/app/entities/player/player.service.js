(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Player', Player);

    Player.$inject = ['$resource'];

    function Player ($resource) {
        var resourceUrl =  'api/players/:id';

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
                url: 'api/players/tournaments/:id',
                isArray: true
            },
            'getTeams': {
                method: 'GET',
                url: 'api/players/teams/:id',
                isArray: true
            }
        });
    }
})();
