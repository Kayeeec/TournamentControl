(function () {
    'use strict';
    angular
            .module('tournamentControlApp')
            .factory('Game', Game);

    Game.$inject = ['$resource'];

    function Game($resource) {
        var resourceUrl = 'api/games/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {method: 'PUT'},
            'addSet': {method: 'POST',
                params: {id: '@id'},
                url: 'api/games/:id/sets',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'updateSetSettings': {method: 'POST',
                url: 'api/game-sets/set-settings/update',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'removeSet': {method: 'DELETE',
                params: {id: '@id'},
                url: 'api/game-sets/:id',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'getGamesByTournament':{
                method: 'GET', 
                isArray: true,
                params: {tournamentId: '@tournamentId'},
                url: 'api/games-by-tournament/:tournamentId'
            }
            
            
        });
    }
})();
